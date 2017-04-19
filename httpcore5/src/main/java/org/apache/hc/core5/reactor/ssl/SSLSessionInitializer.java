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

package org.apache.hc.core5.reactor.ssl;

import javax.net.ssl.SSLParameters;

import org.apache.hc.core5.net.NamedEndpoint;

/**
 * Callback interface that can be used to customize various aspects of
 * the TLS/SSl protocol.
 *
 * @since 4.2
 */
public interface SSLSessionInitializer {

    /**
     * Triggered when the SSL connection is being initialized. Custom handlers
     * can use this callback to customize properties of the {@link javax.net.ssl.SSLEngine}
     * used to establish the SSL session by modifying the given
     * {@link SSLParameters}.
     *
     * @param endpoint the endpoint name for a client side session or {@code null}
     *                 for a server side session.
     * @param sslParameters the actual SSL parameters.
     */
    void initialize(NamedEndpoint endpoint, SSLParameters sslParameters);

}
