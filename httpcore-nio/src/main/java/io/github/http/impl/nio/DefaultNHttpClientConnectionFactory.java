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

import io.github.http.HttpRequest;
import io.github.http.HttpResponse;
import io.github.http.config.ConnectionConfig;
import io.github.http.entity.ContentLengthStrategy;
import io.github.http.impl.ConnSupport;
import io.github.http.nio.NHttpClientConnection;
import io.github.http.nio.NHttpConnectionFactory;
import io.github.http.nio.NHttpMessageParserFactory;
import io.github.http.nio.NHttpMessageWriterFactory;
import io.github.http.nio.reactor.IOSession;
import io.github.http.nio.util.ByteBufferAllocator;
import io.github.http.HttpRequest;
import io.github.http.HttpResponse;
import io.github.http.HttpResponseFactory;
import io.github.http.annotation.ThreadingBehavior;
import io.github.http.annotation.Contract;
import io.github.http.config.ConnectionConfig;
import io.github.http.entity.ContentLengthStrategy;
import io.github.http.impl.ConnSupport;
import io.github.http.impl.DefaultHttpResponseFactory;
import io.github.http.impl.nio.codecs.DefaultHttpResponseParserFactory;
import io.github.http.nio.NHttpConnectionFactory;
import io.github.http.nio.NHttpMessageParserFactory;
import io.github.http.nio.NHttpMessageWriterFactory;
import io.github.http.nio.reactor.IOSession;
import io.github.http.nio.util.ByteBufferAllocator;
import io.github.http.nio.util.HeapByteBufferAllocator;
import io.github.http.params.HttpParams;
import io.github.http.util.Args;

/**
 * Default factory for plain (non-encrypted), non-blocking
 * {@link NHttpClientConnection}s.
 *
 * @since 4.2
 */
@SuppressWarnings("deprecation")
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultNHttpClientConnectionFactory
    implements NHttpConnectionFactory<DefaultNHttpClientConnection> {

    public static final DefaultNHttpClientConnectionFactory INSTANCE = new DefaultNHttpClientConnectionFactory();

    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;
    private final NHttpMessageParserFactory<HttpResponse> responseParserFactory;
    private final NHttpMessageWriterFactory<HttpRequest> requestWriterFactory;
    private final ByteBufferAllocator allocator;
    private final ConnectionConfig cconfig;


    /**
     * @since 4.3
     */
    public DefaultNHttpClientConnectionFactory(
            final ContentLengthStrategy incomingContentStrategy,
            final ContentLengthStrategy outgoingContentStrategy,
            final NHttpMessageParserFactory<HttpResponse> responseParserFactory,
            final NHttpMessageWriterFactory<HttpRequest> requestWriterFactory,
            final ByteBufferAllocator allocator,
            final ConnectionConfig cconfig) {
        super();
        this.incomingContentStrategy = incomingContentStrategy;
        this.outgoingContentStrategy = outgoingContentStrategy;
        this.responseParserFactory = responseParserFactory;
        this.requestWriterFactory = requestWriterFactory;
        this.allocator = allocator;
        this.cconfig = cconfig != null ? cconfig : ConnectionConfig.DEFAULT;
    }

    /**
     * @since 4.3
     */
    public DefaultNHttpClientConnectionFactory(
            final NHttpMessageParserFactory<HttpResponse> responseParserFactory,
            final NHttpMessageWriterFactory<HttpRequest> requestWriterFactory,
            final ByteBufferAllocator allocator,
            final ConnectionConfig cconfig) {
        this(null, null, responseParserFactory, requestWriterFactory, allocator, cconfig);
    }

    /**
     * @since 4.3
     */
    public DefaultNHttpClientConnectionFactory(
            final NHttpMessageParserFactory<HttpResponse> responseParserFactory,
            final NHttpMessageWriterFactory<HttpRequest> requestWriterFactory,
            final ConnectionConfig cconfig) {
        this(null, null, responseParserFactory, requestWriterFactory, null, cconfig);
    }

    /**
     * @since 4.3
     */
    public DefaultNHttpClientConnectionFactory(final ConnectionConfig cconfig) {
        this(null, null, null, null, null, cconfig);
    }

    /**
     * @since 4.3
     */
    public DefaultNHttpClientConnectionFactory() {
        this(null, null, null, null, null, null);
    }



    @Override
    public DefaultNHttpClientConnection createConnection(final IOSession session) {
        return new DefaultNHttpClientConnection(
                session,
                this.cconfig.getBufferSize(),
                this.cconfig.getFragmentSizeHint(),
                this.allocator,
                ConnSupport.createDecoder(this.cconfig),
                ConnSupport.createEncoder(this.cconfig),
                this.cconfig.getMessageConstraints(),
                this.incomingContentStrategy,
                this.outgoingContentStrategy,
                this.requestWriterFactory,
                this.responseParserFactory);
    }

}
