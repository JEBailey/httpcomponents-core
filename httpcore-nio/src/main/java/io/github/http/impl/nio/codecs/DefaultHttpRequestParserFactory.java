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

package io.github.http.impl.nio.codecs;

import io.github.http.HttpRequest;
import io.github.http.HttpRequestFactory;
import io.github.http.config.MessageConstraints;
import io.github.http.impl.DefaultHttpRequestFactory;
import io.github.http.message.BasicLineParser;
import io.github.http.HttpRequest;
import io.github.http.HttpRequestFactory;
import io.github.http.annotation.ThreadingBehavior;
import io.github.http.annotation.Contract;
import io.github.http.config.MessageConstraints;
import io.github.http.impl.DefaultHttpRequestFactory;
import io.github.http.message.BasicLineParser;
import io.github.http.message.LineParser;
import io.github.http.nio.NHttpMessageParser;
import io.github.http.nio.NHttpMessageParserFactory;
import io.github.http.nio.reactor.SessionInputBuffer;

/**
 * Default factory for request message parsers.
 *
 * @since 4.3
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpRequestParserFactory implements NHttpMessageParserFactory<HttpRequest> {

    public static final DefaultHttpRequestParserFactory INSTANCE = new DefaultHttpRequestParserFactory();

    private final LineParser lineParser;
    private final HttpRequestFactory requestFactory;

    public DefaultHttpRequestParserFactory(final LineParser lineParser,
            final HttpRequestFactory requestFactory) {
        super();
        this.lineParser = lineParser != null ? lineParser : BasicLineParser.INSTANCE;
        this.requestFactory = requestFactory != null ? requestFactory
                : DefaultHttpRequestFactory.INSTANCE;
    }

    public DefaultHttpRequestParserFactory() {
        this(null, null);
    }

    @Override
    public NHttpMessageParser<HttpRequest> create(final SessionInputBuffer buffer,
            final MessageConstraints constraints) {
        return new DefaultHttpRequestParser(buffer, lineParser, requestFactory, constraints);
    }

}