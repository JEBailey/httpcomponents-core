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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.github.http.HttpHost;
import io.github.http.HttpRequest;
import io.github.http.HttpResponse;
import io.github.http.config.ConnectionConfig;
import io.github.http.HttpHost;
import io.github.http.HttpRequest;
import io.github.http.HttpResponse;
import io.github.http.OoopsieRuntimeException;
import io.github.http.concurrent.FutureCallback;
import io.github.http.config.ConnectionConfig;
import io.github.http.impl.nio.DefaultHttpClientIODispatch;
import io.github.http.impl.nio.DefaultNHttpClientConnection;
import io.github.http.impl.nio.DefaultNHttpClientConnectionFactory;
import io.github.http.impl.nio.pool.BasicNIOConnPool;
import io.github.http.impl.nio.pool.BasicNIOPoolEntry;
import io.github.http.impl.nio.reactor.DefaultConnectingIOReactor;
import io.github.http.impl.nio.reactor.ExceptionEvent;
import io.github.http.nio.NHttpClientConnection;
import io.github.http.nio.NHttpClientEventHandler;
import io.github.http.nio.pool.NIOConnFactory;
import io.github.http.nio.protocol.BasicAsyncRequestProducer;
import io.github.http.nio.protocol.BasicAsyncResponseConsumer;
import io.github.http.nio.protocol.HttpAsyncRequestExecutor;
import io.github.http.nio.protocol.HttpAsyncRequestProducer;
import io.github.http.nio.protocol.HttpAsyncRequester;
import io.github.http.nio.protocol.HttpAsyncResponseConsumer;
import io.github.http.nio.reactor.ConnectingIOReactor;
import io.github.http.nio.reactor.IOEventDispatch;
import io.github.http.nio.reactor.IOReactorExceptionHandler;
import io.github.http.nio.reactor.IOReactorStatus;
import io.github.http.nio.reactor.IOSession;
import io.github.http.nio.reactor.SessionRequest;
import io.github.http.protocol.HttpContext;
import io.github.http.protocol.HttpCoreContext;
import io.github.http.protocol.HttpProcessor;
import io.github.http.protocol.ImmutableHttpProcessor;
import io.github.http.protocol.RequestConnControl;
import io.github.http.protocol.RequestContent;
import io.github.http.protocol.RequestExpectContinue;
import io.github.http.protocol.RequestTargetHost;
import io.github.http.protocol.RequestUserAgent;

public class HttpClientNio {

    public static final HttpProcessor DEFAULT_HTTP_PROC = new ImmutableHttpProcessor(
            new RequestContent(),
            new RequestTargetHost(),
            new RequestConnControl(),
            new RequestUserAgent("TEST-CLIENT/1.1"),
            new RequestExpectContinue(true));

    private final DefaultConnectingIOReactor ioReactor;
    private final BasicNIOConnPool connpool;

    private volatile HttpProcessor httpProcessor;
    private volatile HttpAsyncRequester executor;
    private volatile IOReactorThread thread;
    private volatile int timeout;

    public HttpClientNio(
            final NIOConnFactory<HttpHost, NHttpClientConnection> connFactory) throws IOException {
        super();
        this.ioReactor = new DefaultConnectingIOReactor();
        this.ioReactor.setExceptionHandler(new SimpleIOReactorExceptionHandler());
        this.connpool = new BasicNIOConnPool(this.ioReactor, new NIOConnFactory<HttpHost, NHttpClientConnection>() {

            @Override
            public NHttpClientConnection create(
                final HttpHost route, final IOSession session) throws IOException {
                final NHttpClientConnection conn = connFactory.create(route, session);
                conn.setSocketTimeout(timeout);
                return conn;
            }

        }, 0);
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public void setMaxTotal(final int max) {
        this.connpool.setMaxTotal(max);
    }

    public void setMaxPerRoute(final int max) {
        this.connpool.setDefaultMaxPerRoute(max);
    }

    public void setHttpProcessor(final HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
    }

    public Future<BasicNIOPoolEntry> lease(
            final HttpHost host,
            final FutureCallback<BasicNIOPoolEntry> callback) {
        return this.connpool.lease(host, null, this.timeout, TimeUnit.MILLISECONDS, callback);
    }

    public void release(final BasicNIOPoolEntry poolEntry, final boolean reusable) {
        this.connpool.release(poolEntry, reusable);
    }

    public <T> Future<T> execute(
            final HttpAsyncRequestProducer requestProducer,
            final HttpAsyncResponseConsumer<T> responseConsumer,
            final HttpContext context,
            final FutureCallback<T> callback) {
        return executor.execute(requestProducer, responseConsumer, this.connpool,
                context != null ? context : HttpCoreContext.create(), callback);
    }

    public <T> Future<List<T>> executePipelined(
            final HttpHost target,
            final List<HttpAsyncRequestProducer> requestProducers,
            final List<HttpAsyncResponseConsumer<T>> responseConsumers,
            final HttpContext context,
            final FutureCallback<List<T>> callback) {
        return executor.executePipelined(target, requestProducers, responseConsumers, this.connpool,
                context != null ? context : HttpCoreContext.create(), callback);
    }

    public Future<HttpResponse> execute(
            final HttpHost target,
            final HttpRequest request,
            final HttpContext context,
            final FutureCallback<HttpResponse> callback) {
        return execute(
                new BasicAsyncRequestProducer(target, request),
                new BasicAsyncResponseConsumer(),
                context != null ? context : HttpCoreContext.create(),
                callback);
    }

    public Future<List<HttpResponse>> executePipelined(
            final HttpHost target,
            final List<HttpRequest> requests,
            final HttpContext context,
            final FutureCallback<List<HttpResponse>> callback) {
        final List<HttpAsyncRequestProducer> requestProducers =
                new ArrayList<HttpAsyncRequestProducer>(requests.size());
        final List<HttpAsyncResponseConsumer<HttpResponse>> responseConsumers =
                new ArrayList<HttpAsyncResponseConsumer<HttpResponse>>(requests.size());
        for (final HttpRequest request: requests) {
            requestProducers.add(new BasicAsyncRequestProducer(target, request));
            responseConsumers.add(new BasicAsyncResponseConsumer());
        }
        return executor.executePipelined(target, requestProducers, responseConsumers, this.connpool,
                context != null ? context : HttpCoreContext.create(), callback);
    }

    public Future<HttpResponse> execute(
            final HttpHost target,
            final HttpRequest request,
            final HttpContext context) {
        return execute(target, request, context, null);
    }

    public Future<List<HttpResponse>> executePipelined(
            final HttpHost target,
            final List<HttpRequest> requests,
            final HttpContext context) {
        return executePipelined(target, requests, context, null);
    }

    public Future<HttpResponse> execute(
            final HttpHost target,
            final HttpRequest request) {
        return execute(target, request, null, null);
    }

    public Future<List<HttpResponse>> executePipelined(
            final HttpHost target,
            final HttpRequest... requests) {
        return executePipelined(target, Arrays.asList(requests), null, null);
    }

    private void execute(final NHttpClientEventHandler clientHandler) throws IOException {
        final IOEventDispatch ioEventDispatch = new DefaultHttpClientIODispatch(clientHandler,
            new DefaultNHttpClientConnectionFactory(ConnectionConfig.DEFAULT)) {

            @Override
            protected DefaultNHttpClientConnection createConnection(final IOSession session) {
                final DefaultNHttpClientConnection conn = super.createConnection(session);
                conn.setSocketTimeout(timeout);
                return conn;
            }

        };
        this.ioReactor.execute(ioEventDispatch);
    }

    public SessionRequest openConnection(final InetSocketAddress address, final Object attachment) {
        final SessionRequest sessionRequest = this.ioReactor.connect(address, null, attachment, null);
        sessionRequest.setConnectTimeout(this.timeout);
        return sessionRequest;
    }

    public void start() {
        this.executor = new HttpAsyncRequester(this.httpProcessor != null ? this.httpProcessor : DEFAULT_HTTP_PROC);
        this.thread = new IOReactorThread(new HttpAsyncRequestExecutor());
        this.thread.start();
    }

    public ConnectingIOReactor getIoReactor() {
        return this.ioReactor;
    }

    public IOReactorStatus getStatus() {
        return this.ioReactor.getStatus();
    }

    public List<ExceptionEvent> getAuditLog() {
        return this.ioReactor.getAuditLog();
    }

    public void join(final long timeout) throws InterruptedException {
        if (this.thread != null) {
            this.thread.join(timeout);
        }
    }

    public Exception getException() {
        if (this.thread != null) {
            return this.thread.getException();
        } else {
            return null;
        }
    }

    public void shutdown() throws IOException {
        this.connpool.shutdown(2000);
        try {
            join(500);
        } catch (final InterruptedException ignore) {
        }
    }

    private class IOReactorThread extends Thread {

        private final NHttpClientEventHandler clientHandler;

        private volatile Exception ex;

        public IOReactorThread(final NHttpClientEventHandler clientHandler) {
            super();
            this.clientHandler = clientHandler;
        }

        @Override
        public void run() {
            try {
                execute(this.clientHandler);
            } catch (final Exception ex) {
                this.ex = ex;
            }
        }

        public Exception getException() {
            return this.ex;
        }

    }

    static class SimpleIOReactorExceptionHandler implements IOReactorExceptionHandler {

        @Override
        public boolean handle(final RuntimeException ex) {
            if (!(ex instanceof OoopsieRuntimeException)) {
                ex.printStackTrace(System.out);
            }
            return false;
        }

        @Override
        public boolean handle(final IOException ex) {
            ex.printStackTrace(System.out);
            return false;
        }

    }

}
