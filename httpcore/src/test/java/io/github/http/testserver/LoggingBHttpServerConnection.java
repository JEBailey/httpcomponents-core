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

package io.github.http.testserver;

import io.github.http.Header;
import io.github.http.HttpRequest;
import io.github.http.HttpResponse;
import io.github.http.config.MessageConstraints;
import io.github.http.entity.ContentLengthStrategy;
import io.github.http.impl.DefaultBHttpServerConnection;
import io.github.http.io.HttpMessageParserFactory;
import io.github.http.io.HttpMessageWriterFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.concurrent.atomic.AtomicLong;

public class LoggingBHttpServerConnection extends DefaultBHttpServerConnection {

    private static final AtomicLong COUNT = new AtomicLong();

    private final String id;
    private final Log log;
    private final Log headerLog;
    private final Wire wire;

    public LoggingBHttpServerConnection(
            final int bufferSize,
            final int fragmentSizeHint,
            final CharsetDecoder charDecoder,
            final CharsetEncoder charEncoder,
            final MessageConstraints constraints,
            final ContentLengthStrategy incomingContentStrategy,
            final ContentLengthStrategy outgoingContentStrategy,
            final HttpMessageParserFactory<HttpRequest> requestParserFactory,
            final HttpMessageWriterFactory<HttpResponse> responseWriterFactory) {
        super(bufferSize, fragmentSizeHint, charDecoder, charEncoder, constraints,
                incomingContentStrategy, outgoingContentStrategy,
                requestParserFactory, responseWriterFactory);
        this.id = "http-incoming-" + COUNT.incrementAndGet();
        this.log = LogFactory.getLog(getClass());
        this.headerLog = LogFactory.getLog("io.github.http.headers");
        this.wire = new Wire(LogFactory.getLog("io.github.http.wire"), this.id);
    }

    public LoggingBHttpServerConnection(final int bufferSize) {
        this(bufferSize, bufferSize, null, null, null, null, null, null, null);
    }

    @Override
    public void close() throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(this.id + ": Close connection");
        }
        super.close();
    }

    @Override
    public void shutdown() throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(this.id + ": Shutdown connection");
        }
        super.shutdown();
    }

    @Override
    protected InputStream getSocketInputStream(final Socket socket) throws IOException {
        InputStream in = super.getSocketInputStream(socket);
        if (wire.isEnabled()) {
            in = new LoggingInputStream(in, wire);
        }
        return in;
    }

    @Override
    protected OutputStream getSocketOutputStream(final Socket socket) throws IOException {
        OutputStream out = super.getSocketOutputStream(socket);
        if (wire.isEnabled()) {
            out = new LoggingOutputStream(out, wire);
        }
        return out;
    }

    @Override
    protected void onRequestReceived(final HttpRequest request) {
        if (request != null && this.headerLog.isDebugEnabled()) {
            this.headerLog.debug(this.id + " >> " + request.getRequestLine().toString());
            final Header[] headers = request.getAllHeaders();
            for (final Header header : headers) {
                this.headerLog.debug(this.id + " >> " + header.toString());
            }
        }
    }

    @Override
    protected void onResponseSubmitted(final HttpResponse response) {
        if (response != null && this.headerLog.isDebugEnabled()) {
            this.headerLog.debug(this.id + " << " + response.getStatusLine().toString());
            final Header[] headers = response.getAllHeaders();
            for (final Header header : headers) {
                this.headerLog.debug(this.id + " << " + header.toString());
            }
        }
    }

}