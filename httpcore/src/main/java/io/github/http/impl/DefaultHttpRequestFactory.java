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

package io.github.http.impl;

import io.github.http.HttpRequest;
import io.github.http.HttpRequestFactory;
import io.github.http.MethodNotSupportedException;
import io.github.http.RequestLine;
import io.github.http.annotation.Contract;
import io.github.http.annotation.ThreadingBehavior;
import io.github.http.message.BasicHttpEntityEnclosingRequest;
import io.github.http.message.BasicHttpRequest;
import io.github.http.util.Args;

/**
 * Default factory for creating {@link HttpRequest} objects.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class DefaultHttpRequestFactory implements HttpRequestFactory {

    public static final DefaultHttpRequestFactory INSTANCE = new DefaultHttpRequestFactory();

    private static final String[] RFC2616_COMMON_METHODS = {
        "GET"
    };

    private static final String[] RFC2616_ENTITY_ENC_METHODS = {
        "POST",
        "PUT"
    };

    private static final String[] RFC2616_SPECIAL_METHODS = {
        "HEAD",
        "OPTIONS",
        "DELETE",
        "TRACE",
        "CONNECT"
    };

    private static final String[] RFC5789_ENTITY_ENC_METHODS = {
        "PATCH"
    };

    public DefaultHttpRequestFactory() {
        super();
    }

    private static boolean isOneOf(final String[] methods, final String method) {
        for (final String method2 : methods) {
            if (method2.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public HttpRequest newHttpRequest(final RequestLine requestline)
            throws MethodNotSupportedException {
        Args.notNull(requestline, "Request line");
        final String method = requestline.getMethod();
        if (isOneOf(RFC2616_COMMON_METHODS, method)) {
            return new BasicHttpRequest(requestline);
        } else if (isOneOf(RFC2616_ENTITY_ENC_METHODS, method)) {
            return new BasicHttpEntityEnclosingRequest(requestline);
        } else if (isOneOf(RFC2616_SPECIAL_METHODS, method)) {
            return new BasicHttpRequest(requestline);
        } else if (isOneOf(RFC5789_ENTITY_ENC_METHODS, method)) {
            return new BasicHttpEntityEnclosingRequest(requestline);
        } else {
            throw new MethodNotSupportedException(method + " method not supported");
        }
    }

    @Override
    public HttpRequest newHttpRequest(final String method, final String uri)
            throws MethodNotSupportedException {
        if (isOneOf(RFC2616_COMMON_METHODS, method)) {
            return new BasicHttpRequest(method, uri);
        } else if (isOneOf(RFC2616_ENTITY_ENC_METHODS, method)) {
            return new BasicHttpEntityEnclosingRequest(method, uri);
        } else if (isOneOf(RFC2616_SPECIAL_METHODS, method)) {
            return new BasicHttpRequest(method, uri);
        } else if (isOneOf(RFC5789_ENTITY_ENC_METHODS, method)) {
            return new BasicHttpEntityEnclosingRequest(method, uri);
        } else {
            throw new MethodNotSupportedException(method
                    + " method not supported");
        }
    }

}
