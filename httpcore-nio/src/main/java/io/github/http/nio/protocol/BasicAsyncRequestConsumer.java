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
package io.github.http.nio.protocol;

import java.io.IOException;

import io.github.http.ContentTooLongException;
import io.github.http.HttpEntity;
import io.github.http.HttpEntityEnclosingRequest;
import io.github.http.HttpRequest;
import io.github.http.entity.ContentType;
import io.github.http.util.Asserts;
import io.github.http.ContentTooLongException;
import io.github.http.HttpEntity;
import io.github.http.HttpEntityEnclosingRequest;
import io.github.http.HttpRequest;
import io.github.http.entity.ContentType;
import io.github.http.nio.ContentDecoder;
import io.github.http.nio.IOControl;
import io.github.http.nio.entity.ContentBufferEntity;
import io.github.http.nio.util.HeapByteBufferAllocator;
import io.github.http.nio.util.SimpleInputBuffer;
import io.github.http.protocol.HttpContext;
import io.github.http.util.Asserts;

/**
 * Basic implementation of {@link HttpAsyncRequestConsumer}. Please note that
 * this consumer buffers request content in memory and should be used for
 * relatively small request messages.
 *
 * @since 4.2
 */
public class BasicAsyncRequestConsumer extends AbstractAsyncRequestConsumer<HttpRequest> {

    private static final int MAX_INITIAL_BUFFER_SIZE = 256 * 1024;

    private volatile HttpRequest request;
    private volatile SimpleInputBuffer buf;

    public BasicAsyncRequestConsumer() {
        super();
    }

    @Override
    protected void onRequestReceived(final HttpRequest request) throws IOException {
        this.request = request;
    }

    @Override
    protected void onEntityEnclosed(
            final HttpEntity entity, final ContentType contentType) throws IOException {
        long len = entity.getContentLength();
        if (len > Integer.MAX_VALUE) {
            throw new ContentTooLongException("Entity content is too long: %,d", len);
        }
        if (len < 0) {
            len = 4096;
        }
        final int initialBufferSize = Math.min((int) len, MAX_INITIAL_BUFFER_SIZE);
        this.buf = new SimpleInputBuffer(initialBufferSize, new HeapByteBufferAllocator());
        ((HttpEntityEnclosingRequest) this.request).setEntity(
                new ContentBufferEntity(entity, this.buf));
    }

    @Override
    protected void onContentReceived(
            final ContentDecoder decoder, final IOControl ioControl) throws IOException {
        Asserts.notNull(this.buf, "Content buffer");
        this.buf.consumeContent(decoder);
    }

    @Override
    protected void releaseResources() {
        this.request = null;
        this.buf = null;
    }

    @Override
    protected HttpRequest buildResult(final HttpContext context) {
        return this.request;
    }

}
