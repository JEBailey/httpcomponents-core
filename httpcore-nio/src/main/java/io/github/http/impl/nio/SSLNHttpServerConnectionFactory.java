/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package io.github.http.impl.nio;

import javax.net.ssl.SSLContext;

import io.github.http.HttpRequest;
import io.github.http.HttpResponse;
import io.github.http.config.ConnectionConfig;
import io.github.http.entity.ContentLengthStrategy;
import io.github.http.impl.ConnSupport;
import io.github.http.nio.NHttpServerConnection;
import io.github.http.nio.reactor.ssl.SSLIOSession;
import io.github.http.nio.reactor.ssl.SSLMode;
import io.github.http.nio.reactor.ssl.SSLSetupHandler;
import io.github.http.ssl.SSLContexts;
import io.github.http.HttpRequest;
import io.github.http.HttpRequestFactory;
import io.github.http.HttpResponse;
import io.github.http.annotation.ThreadingBehavior;
import io.github.http.annotation.Contract;
import io.github.http.config.ConnectionConfig;
import io.github.http.entity.ContentLengthStrategy;
import io.github.http.impl.ConnSupport;
import io.github.http.impl.DefaultHttpRequestFactory;
import io.github.http.impl.nio.codecs.DefaultHttpRequestParserFactory;
import io.github.http.nio.NHttpConnectionFactory;
import io.github.http.nio.NHttpMessageParserFactory;
import io.github.http.nio.NHttpMessageWriterFactory;
import io.github.http.nio.reactor.IOSession;
import io.github.http.nio.reactor.ssl.SSLIOSession;
import io.github.http.nio.reactor.ssl.SSLMode;
import io.github.http.nio.reactor.ssl.SSLSetupHandler;
import io.github.http.nio.util.ByteBufferAllocator;
import io.github.http.nio.util.HeapByteBufferAllocator;
import io.github.http.params.HttpParams;
import io.github.http.ssl.SSLContexts;
import io.github.http.util.Args;

/**
 * Default factory for SSL encrypted, non-blocking
 * {@link NHttpServerConnection}s.
 *
 * @since 4.2
 */
@SuppressWarnings("deprecation")
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class SSLNHttpServerConnectionFactory
    implements NHttpConnectionFactory<DefaultNHttpServerConnection> {

    private final SSLContext sslContext;
    private final SSLSetupHandler sslHandler;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;
    private final NHttpMessageParserFactory<HttpRequest> requestParserFactory;
    private final NHttpMessageWriterFactory<HttpResponse> responseWriterFactory;
    private final ByteBufferAllocator allocator;
    private final ConnectionConfig cconfig;



    /**
     * @since 4.3
     */
    public SSLNHttpServerConnectionFactory(
            final SSLContext sslContext,
            final SSLSetupHandler sslHandler,
            final ContentLengthStrategy incomingContentStrategy,
            final ContentLengthStrategy outgoingContentStrategy,
            final NHttpMessageParserFactory<HttpRequest> requestParserFactory,
            final NHttpMessageWriterFactory<HttpResponse> responseWriterFactory,
            final ByteBufferAllocator allocator,
            final ConnectionConfig cconfig) {
        super();
        this.sslContext = sslContext != null ? sslContext : SSLContexts.createSystemDefault();
        this.sslHandler = sslHandler;
        this.incomingContentStrategy = incomingContentStrategy;
        this.outgoingContentStrategy = outgoingContentStrategy;
        this.requestParserFactory = requestParserFactory;
        this.responseWriterFactory = responseWriterFactory;
        this.allocator = allocator;
        this.cconfig = cconfig != null ? cconfig : ConnectionConfig.DEFAULT;
    }

    /**
     * @since 4.3
     */
    public SSLNHttpServerConnectionFactory(
            final SSLContext sslContext,
            final SSLSetupHandler sslHandler,
            final NHttpMessageParserFactory<HttpRequest> requestParserFactory,
            final NHttpMessageWriterFactory<HttpResponse> responseWriterFactory,
            final ByteBufferAllocator allocator,
            final ConnectionConfig cconfig) {
        this(sslContext, sslHandler,
                null, null, requestParserFactory, responseWriterFactory, allocator, cconfig);
    }

    /**
     * @since 4.3
     */
    public SSLNHttpServerConnectionFactory(
            final SSLContext sslContext,
            final SSLSetupHandler sslHandler,
            final NHttpMessageParserFactory<HttpRequest> requestParserFactory,
            final NHttpMessageWriterFactory<HttpResponse> responseWriterFactory,
            final ConnectionConfig cconfig) {
        this(sslContext, sslHandler,
                null, null, requestParserFactory, responseWriterFactory, null, cconfig);
    }

    /**
     * @since 4.3
     */
    public SSLNHttpServerConnectionFactory(
            final SSLContext sslContext,
            final SSLSetupHandler sslHandler,
            final ConnectionConfig config) {
        this(sslContext, sslHandler, null, null, null, null, null, config);
    }

    /**
     * @since 4.3
     */
    public SSLNHttpServerConnectionFactory(final ConnectionConfig config) {
        this(null, null, null, null, null, null, null, config);
    }

    /**
     * @since 4.3
     */
    public SSLNHttpServerConnectionFactory() {
        this(null, null, null, null, null, null, null, null);
    }

    /**
     * @since 4.3
     */
    protected SSLIOSession createSSLIOSession(
            final IOSession ioSession,
            final SSLContext sslContext,
            final SSLSetupHandler sslHandler) {
        final SSLIOSession sslioSession = new SSLIOSession(ioSession, SSLMode.SERVER,
                sslContext, sslHandler);
        return sslioSession;
    }

    @Override
    public DefaultNHttpServerConnection createConnection(final IOSession ioSession) {
        final SSLIOSession sslioSession = createSSLIOSession(ioSession, this.sslContext, this.sslHandler);
        ioSession.setAttribute(SSLIOSession.SESSION_KEY, sslioSession);
        return new DefaultNHttpServerConnection(sslioSession,
                this.cconfig.getBufferSize(),
                this.cconfig.getFragmentSizeHint(),
                this.allocator,
                ConnSupport.createDecoder(this.cconfig),
                ConnSupport.createEncoder(this.cconfig),
                this.cconfig.getMessageConstraints(),
                this.incomingContentStrategy,
                this.outgoingContentStrategy,
                this.requestParserFactory,
                this.responseWriterFactory);
    }

}
