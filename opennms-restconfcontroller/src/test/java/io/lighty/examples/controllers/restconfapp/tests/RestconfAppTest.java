/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v10.html
 */

package io.lighty.examples.controllers.restconfapp.tests;

import io.lighty.examples.controllers.restconfapp.Main;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test starts lighty.io RESTCONF / NETCONF application.
 * RESTCONF API is available at http://localhost:8888/restconf
 * is used by REST client to test access to global data store.
 * This is integration test and requires free port 8888 on localhost.
 * This test is roughly same as single-feature test in OpenDaylight which starts:
 * feature:install odl-netconf-all
 */
public class RestconfAppTest {

    private static final Logger LOG = LoggerFactory.getLogger(RestconfAppTest.class);
    public static final long SLEEP_AFTER_SHUTDOWN_TIMEOUT_MILLIS = 3_000;

    private static Main restconfApp;
    private static RestClient restClient;

    @BeforeClass
    public static void init() {
        restconfApp = new Main();
        restconfApp.start();
        restClient = new RestClient("http://localhost:8888/");
    }

    /**
     * Perform basic GET operations via RESTCONF.
     */
    // @ Test
    public void simpleApplicationTest() throws TimeoutException, ExecutionException, InterruptedException {
        ContentResponse operations = null;
        operations = restClient.GET("restconf/operations");
        Assert.assertEquals(operations.getStatus(), 200);
        operations = restClient.GET("restconf/data/network-topology:network-topology?content=config");
        Assert.assertEquals(operations.getStatus(), 200);
        operations = restClient.GET("restconf/data/network-topology:network-topology?content=nonconfig");
        Assert.assertEquals(operations.getStatus(), 200);
    }

    /**
     * Check if Swagger service and UI is responding.
     */
    // @ Test
    public void swaggerURLsTest() {
        ContentResponse operations = null;
        try {
            operations = restClient.GET("apidoc/openapi3/18/apis/single");
            Assert.assertEquals(operations.getStatus(), 200);
            operations = restClient.GET("apidoc/explorer/index.html");
            Assert.assertEquals(operations.getStatus(), 200);
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            Assert.fail();
        }
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    @AfterClass
    public static void shutdown() {
        restconfApp.shutdown();
        try {
            restClient.close();
            Thread.sleep(SLEEP_AFTER_SHUTDOWN_TIMEOUT_MILLIS);
        } catch (Exception e) {
            LOG.error("Shutdown of restClient failed", e);
        }
    }

}
