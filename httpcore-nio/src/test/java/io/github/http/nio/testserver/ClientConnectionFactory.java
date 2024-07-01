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
package io.github.http.nio.testserver;

import javax.net.ssl.SSLContext;

import io.github.http.nio.reactor.ssl.SSLIOSession;
import io.github.http.nio.reactor.ssl.SSLMode;
import io.github.http.nio.reactor.ssl.SSLSetupHandler;
import io.github.http.impl.nio.DefaultNHttpClientConnection;
import io.github.http.nio.NHttpConnectionFactory;
import io.github.http.nio.reactor.IOSession;
import io.github.http.nio.reactor.ssl.SSLIOSession;
import io.github.http.nio.reactor.ssl.SSLMode;
import io.github.http.nio.reactor.ssl.SSLSetupHandler;

public class ClientConnectionFactory implements NHttpConnectionFactory<DefaultNHttpClientConnection> {

    private final SSLContext sslContext;
    private final SSLSetupHandler setupHandler;

    public ClientConnectionFactory(
            final SSLContext sslContext, final SSLSetupHandler setupHandler) {
        super();
        this.sslContext = sslContext;
        this.setupHandler = setupHandler;
    }

    public ClientConnectionFactory(final SSLContext sslContext) {
        this(sslContext, null);
    }

    public ClientConnectionFactory() {
        this(null, null);
    }

    @Override
    public DefaultNHttpClientConnection createConnection(final IOSession ioSession) {
        if (this.sslContext != null) {
            final SSLIOSession sslioSession = new SSLIOSession(
                    ioSession, SSLMode.CLIENT, this.sslContext, this.setupHandler);
            ioSession.setAttribute(SSLIOSession.SESSION_KEY, sslioSession);
            return new LoggingNHttpClientConnection(sslioSession);
        } else {
            return new LoggingNHttpClientConnection(ioSession);
        }
    }

}
