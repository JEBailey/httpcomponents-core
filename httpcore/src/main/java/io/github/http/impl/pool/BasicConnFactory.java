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
package io.github.http.impl.pool;

import io.github.http.HttpClientConnection;
import io.github.http.HttpConnectionFactory;
import io.github.http.HttpHost;
import io.github.http.annotation.Contract;
import io.github.http.annotation.ThreadingBehavior;
import io.github.http.config.ConnectionConfig;
import io.github.http.config.SocketConfig;
import io.github.http.impl.DefaultBHttpClientConnectionFactory;
import io.github.http.pool.ConnFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A very basic {@link ConnFactory} implementation that creates
 * {@link HttpClientConnection} instances given a {@link HttpHost} instance.
 *
 * @see HttpHost
 * @since 4.2
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class BasicConnFactory implements ConnFactory<HttpHost, HttpClientConnection> {

    private final SocketFactory plainfactory;
    private final SSLSocketFactory sslfactory;
    private final int connectTimeout;
    private final SocketConfig sconfig;
    private final HttpConnectionFactory<? extends HttpClientConnection> connFactory;

    /**
     * @since 4.3
     */
    public BasicConnFactory(
            final SocketFactory plainfactory,
            final SSLSocketFactory sslfactory,
            final int connectTimeout,
            final SocketConfig sconfig,
            final ConnectionConfig cconfig) {
        super();
        this.plainfactory = plainfactory;
        this.sslfactory = sslfactory;
        this.connectTimeout = connectTimeout;
        this.sconfig = sconfig != null ? sconfig : SocketConfig.DEFAULT;
        this.connFactory = new DefaultBHttpClientConnectionFactory(
                cconfig != null ? cconfig : ConnectionConfig.DEFAULT);
    }

    /**
     * @since 4.3
     */
    public BasicConnFactory(
            final int connectTimeout, final SocketConfig sconfig, final ConnectionConfig cconfig) {
        this(null, null, connectTimeout, sconfig, cconfig);
    }

    /**
     * @since 4.3
     */
    public BasicConnFactory(final SocketConfig sconfig, final ConnectionConfig cconfig) {
        this(null, null, 0, sconfig, cconfig);
    }

    /**
     * @since 4.3
     */
    public BasicConnFactory() {
        this(null, null, 0, SocketConfig.DEFAULT, ConnectionConfig.DEFAULT);
    }

    @Override
    public HttpClientConnection create(final HttpHost host) throws IOException {
        final String scheme = host.getSchemeName();
        final Socket socket;
        if ("http".equalsIgnoreCase(scheme)) {
            socket = this.plainfactory != null ? this.plainfactory.createSocket() :
                    new Socket();
        } else if ("https".equalsIgnoreCase(scheme)) {
            socket = (this.sslfactory != null ? this.sslfactory :
                    SSLSocketFactory.getDefault()).createSocket();
        } else {
            throw new IOException(scheme + " scheme is not supported");
        }
        final String hostname = host.getHostName();
        int port = host.getPort();
        if (port == -1) {
            if (host.getSchemeName().equalsIgnoreCase("http")) {
                port = 80;
            } else if (host.getSchemeName().equalsIgnoreCase("https")) {
                port = 443;
            }
        }
        socket.setSoTimeout(this.sconfig.getSoTimeout());
        if (this.sconfig.getSndBufSize() > 0) {
            socket.setSendBufferSize(this.sconfig.getSndBufSize());
        }
        if (this.sconfig.getRcvBufSize() > 0) {
            socket.setReceiveBufferSize(this.sconfig.getRcvBufSize());
        }
        socket.setTcpNoDelay(this.sconfig.isTcpNoDelay());
        final int linger = this.sconfig.getSoLinger();
        if (linger >= 0) {
            socket.setSoLinger(true, linger);
        }
        socket.setKeepAlive(this.sconfig.isSoKeepAlive());
        // Run this under a doPrivileged to support lib users that run under a SecurityManager this allows granting connect permissions
        // only to this library
        final InetSocketAddress address = new InetSocketAddress(hostname, port);
        socket.connect(address, BasicConnFactory.this.connectTimeout);
        return this.connFactory.createConnection(socket);
    }

}