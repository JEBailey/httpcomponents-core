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
package io.github.http.impl.nio.pool;

import java.io.IOException;

import javax.net.ssl.SSLContext;

import io.github.http.HttpHost;
import io.github.http.HttpRequest;
import io.github.http.HttpResponse;
import io.github.http.config.ConnectionConfig;
import io.github.http.impl.nio.SSLNHttpClientConnectionFactory;
import io.github.http.nio.NHttpClientConnection;
import io.github.http.nio.NHttpConnectionFactory;
import io.github.http.nio.NHttpMessageParserFactory;
import io.github.http.nio.NHttpMessageWriterFactory;
import io.github.http.nio.pool.NIOConnFactory;
import io.github.http.nio.reactor.IOEventDispatch;
import io.github.http.nio.reactor.IOSession;
import io.github.http.nio.reactor.ssl.SSLSetupHandler;
import io.github.http.nio.util.ByteBufferAllocator;
import io.github.http.util.Args;
import io.github.http.HttpHost;
import io.github.http.HttpRequest;
import io.github.http.HttpResponse;
import io.github.http.HttpResponseFactory;
import io.github.http.annotation.ThreadingBehavior;
import io.github.http.annotation.Contract;
import io.github.http.config.ConnectionConfig;
import io.github.http.impl.DefaultHttpResponseFactory;
import io.github.http.impl.nio.DefaultNHttpClientConnectionFactory;
import io.github.http.impl.nio.SSLNHttpClientConnectionFactory;
import io.github.http.nio.NHttpClientConnection;
import io.github.http.nio.NHttpConnectionFactory;
import io.github.http.nio.NHttpMessageParserFactory;
import io.github.http.nio.NHttpMessageWriterFactory;
import io.github.http.nio.pool.NIOConnFactory;
import io.github.http.nio.reactor.IOEventDispatch;
import io.github.http.nio.reactor.IOSession;
import io.github.http.nio.reactor.ssl.SSLSetupHandler;
import io.github.http.nio.util.ByteBufferAllocator;
import io.github.http.nio.util.HeapByteBufferAllocator;
import io.github.http.params.HttpParams;
import io.github.http.util.Args;

/**
 * A basic {@link NIOConnFactory} implementation that creates
 * {@link NHttpClientConnection} instances given a {@link HttpHost} instance.
 *
 * @since 4.2
 */
@SuppressWarnings("deprecation")
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class BasicNIOConnFactory implements NIOConnFactory<HttpHost, NHttpClientConnection> {

    private final NHttpConnectionFactory<? extends NHttpClientConnection> plainFactory;
    private final NHttpConnectionFactory<? extends NHttpClientConnection> sslFactory;

    public BasicNIOConnFactory(
            final NHttpConnectionFactory<? extends NHttpClientConnection> plainFactory,
            final NHttpConnectionFactory<? extends NHttpClientConnection> sslFactory) {
        super();
        Args.notNull(plainFactory, "Plain HTTP client connection factory");
        this.plainFactory = plainFactory;
        this.sslFactory = sslFactory;
    }

    public BasicNIOConnFactory(
            final NHttpConnectionFactory<? extends NHttpClientConnection> plainFactory) {
        this(plainFactory, null);
    }


    /**
     * @since 4.3
     */
    public BasicNIOConnFactory(
            final SSLContext sslContext,
            final SSLSetupHandler sslHandler,
            final NHttpMessageParserFactory<HttpResponse> responseParserFactory,
            final NHttpMessageWriterFactory<HttpRequest> requestWriterFactory,
            final ByteBufferAllocator allocator,
            final ConnectionConfig config) {
        this(new DefaultNHttpClientConnectionFactory(
                    responseParserFactory, requestWriterFactory, allocator, config),
                new SSLNHttpClientConnectionFactory(
                        sslContext, sslHandler, responseParserFactory, requestWriterFactory,
                        allocator, config));
    }

    /**
     * @since 4.3
     */
    public BasicNIOConnFactory(
            final SSLContext sslContext,
            final SSLSetupHandler sslHandler,
            final ConnectionConfig config) {
        this(sslContext, sslHandler, null, null, null, config);
    }

    /**
     * @since 4.3
     */
    public BasicNIOConnFactory(final ConnectionConfig config) {
        this(new DefaultNHttpClientConnectionFactory(config), null);
    }

    @Override
    public NHttpClientConnection create(final HttpHost route, final IOSession session) throws IOException {
        final NHttpClientConnection conn;
        if (route.getSchemeName().equalsIgnoreCase("https")) {
            if (this.sslFactory == null) {
                throw new IOException("SSL not supported");
            }
            conn = this.sslFactory.createConnection(session);
        } else {
            conn = this.plainFactory.createConnection(session);
        }
        session.setAttribute(IOEventDispatch.CONNECTION_KEY, conn);
        return conn;
    }

}
