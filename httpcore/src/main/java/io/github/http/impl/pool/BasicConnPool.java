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
import io.github.http.HttpHost;
import io.github.http.annotation.Contract;
import io.github.http.annotation.ThreadingBehavior;
import io.github.http.config.ConnectionConfig;
import io.github.http.config.SocketConfig;
import io.github.http.pool.AbstractConnPool;
import io.github.http.pool.ConnFactory;
import io.github.http.pool.ConnPool;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A very basic {@link ConnPool} implementation that
 * represents a pool of blocking {@link HttpClientConnection} connections
 * identified by an {@link HttpHost} instance. Please note this pool
 * implementation does not support complex routes via a proxy cannot
 * differentiate between direct and proxied connections.
 *
 * @see HttpHost
 * @since 4.2
 */
@Contract(threading = ThreadingBehavior.SAFE_CONDITIONAL)
public class BasicConnPool extends AbstractConnPool<HttpHost, HttpClientConnection, BasicPoolEntry> {

    private static final AtomicLong COUNTER = new AtomicLong();

    public BasicConnPool(final ConnFactory<HttpHost, HttpClientConnection> connFactory) {
        super(connFactory, 2, 20);
    }

    /**
     * @since 4.3
     */
    public BasicConnPool(final SocketConfig sconfig, final ConnectionConfig cconfig) {
        super(new BasicConnFactory(sconfig, cconfig), 2, 20);
    }

    /**
     * @since 4.3
     */
    public BasicConnPool() {
        super(new BasicConnFactory(SocketConfig.DEFAULT, ConnectionConfig.DEFAULT), 2, 20);
    }

    @Override
    protected BasicPoolEntry createEntry(
            final HttpHost host,
            final HttpClientConnection conn) {
        return new BasicPoolEntry(Long.toString(COUNTER.getAndIncrement()), host, conn);
    }

    @Override
    protected boolean validate(final BasicPoolEntry entry) {
        return !entry.getConnection().isStale();
    }

}
