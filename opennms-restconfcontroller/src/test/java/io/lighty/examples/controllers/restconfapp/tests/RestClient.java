/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v10.html
 */

package io.lighty.examples.controllers.restconfapp.tests;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("MethodName")
public class RestClient implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

    private String baseUrl;
    private HttpClient httpClient;

    @SuppressWarnings("checkstyle:illegalCatch")
    public RestClient(String baseUrl) {
        try {
            this.baseUrl = baseUrl;
            this.httpClient = new HttpClient();
            LOG.info("initializing HTTP client");
            httpClient = new HttpClient();
            httpClient.start();
        } catch (Exception e) {
            LOG.error("RestClient init ERROR: ", e);
        }
    }

    public ContentResponse GET(String uri) throws InterruptedException, ExecutionException, TimeoutException {
        return httpClient.GET(baseUrl + uri);
    }

    public ContentResponse POST(String uri, String data)
            throws InterruptedException, ExecutionException, TimeoutException {
        Request request =  httpClient.POST(baseUrl + uri);
        request.content(new StringContentProvider(data), "application/json");
        return request.send();
    }

    public ContentResponse PUT(String uri) throws InterruptedException, ExecutionException, TimeoutException {
        Request request =  httpClient.newRequest(baseUrl + uri);
        request.method(HttpMethod.PUT);
        return request.send();
    }

    @SuppressWarnings("AbbreviationAsWordInName")
    public ContentResponse DELETE(String uri) throws InterruptedException, ExecutionException, TimeoutException {
        Request request =  httpClient.newRequest(baseUrl + uri);
        request.method(HttpMethod.DELETE);
        return request.send();
    }

    @Override
    public void close() throws Exception {
        httpClient.stop();
    }
}
