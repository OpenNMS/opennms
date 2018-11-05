/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest.rest;

import static com.jayway.awaitility.Awaitility.await;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.netmgt.daemon.DaemonReloadState;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class DaemonRestServiceIT extends OpenNMSSeleniumTestCase {

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.MINUTES);

    @Before
    public void setUp() {
        RestAssured.baseURI = getBaseUrl();
        RestAssured.port = getServerHttpPort();
        RestAssured.basePath = "/opennms/rest/daemons";
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    @After
    public void tearDown() {
        RestAssured.reset();
    }

    @Test
    public void verifyDaemonList() {
        given().get().then()
                .assertThat().statusCode(200)
                .assertThat().contentType(ContentType.JSON)

                .assertThat().body("", hasSize(32))

                .assertThat()
                .body("[0].name", equalTo("Manager"))
                .body("[0].internal", equalTo(true))
                .body("[0].enabled", equalTo(true))
                .body("[0].reloadable", equalTo(false))

                .body("[1].name", equalTo("TestLoadLibraries"))
                .body("[1].internal", equalTo(true))
                .body("[1].enabled", equalTo(true))
                .body("[1].reloadable", equalTo(false))

                .body("[2].name", equalTo("Eventd"))
                .body("[2].internal", equalTo(false))
                .body("[2].enabled", equalTo(true))
                .body("[2].reloadable", equalTo(true))

                .body("[3].name", equalTo("Alarmd"))
                .body("[3].internal", equalTo(false))
                .body("[3].enabled", equalTo(true))
                .body("[3].reloadable", equalTo(true))

                .body("[4].name", equalTo("Bsmd"))
                .body("[4].internal", equalTo(false))
                .body("[4].enabled", equalTo(true))
                .body("[4].reloadable", equalTo(true))

                .body("[5].name", equalTo("Ticketer"))
                .body("[5].internal", equalTo(false))
                .body("[5].enabled", equalTo(true))
                .body("[5].reloadable", equalTo(false))

                .body("[6].name", equalTo("Correlator"))
                .body("[6].internal", equalTo(false))
                .body("[6].enabled", equalTo(false))
                .body("[6].reloadable", equalTo(false))

                // ...

                .body("[16].name", equalTo("SnmpPoller"))
                .body("[16].internal", equalTo(false))
                .body("[16].enabled", equalTo(false))
                .body("[16].reloadable", equalTo(true))

                // ...

                .body("[31].name", equalTo("Telemetryd"))
                .body("[31].internal", equalTo(false))
                .body("[31].enabled", equalTo(true))
                .body("[31].reloadable", equalTo(true));
    }

    @Test
    public void verifyDaemonReload() {
        // Reload a non Valid DaemonName
        given().body("").post("/reload/WrongDaemonName/").then()
                .assertThat().statusCode(404);
        given().get("/reload/WrongDaemonName/").then()
                .assertThat().statusCode(404);

        // Reload of a non reloadable and internal Daemon
        given().body("").post("/reload/manager/").then()
                .assertThat().statusCode(400);

        // Verify reload state is unknown, as the daemon is not reloadable
        await().atMost(5, TimeUnit.SECONDS)
               .pollDelay(1, TimeUnit.SECONDS)
                .until(() ->  given().get("/reload/manager/").then()
                .assertThat().statusCode(200)
                .assertThat()
                .body("reloadState", equalTo(DaemonReloadState.Unknown.toString())));

        // Reload a reloadable and enabled Daemon
        given().body("").post("/reload/eventd/").then()
                .assertThat().statusCode(204);

        // Verify reloading worked
        await().atMost(5, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .until(() -> given().get("/reload/eventd/").then()
                        .assertThat().statusCode(200)
                        .assertThat()
                        .body("reloadState", equalTo(DaemonReloadState.Success.toString())));

        // Reload a reloadable but not enabled Daemon
        given().body("").post("/reload/snmppoller/").then()
                .assertThat().statusCode(400);
        sleep(1000);
        given().get("/reload/snmppoller/").then()
                .assertThat().statusCode(200)
                .assertThat()
                .body("reloadState", equalTo(DaemonReloadState.Unknown.toString()));

        // Reload of a non reloadable, not enabled Daemon
        given().body("").post("/reload/correlator/").then()
                .assertThat().statusCode(400);
        sleep(1000);
        given().get("/reload/correlator/").then()
                .assertThat().statusCode(200)
                .assertThat()
                .body("reloadState", equalTo(DaemonReloadState.Unknown.toString()));

        // Reload of a non reloadable but enabled Daemon
        given().body("").post("/reload/ticketer/").then()
                .assertThat().statusCode(400);
        sleep(1000);
        given().get("/reload/ticketer/").then()
                .assertThat().statusCode(200)
                .assertThat()
                .body("reloadState", equalTo(DaemonReloadState.Unknown.toString()));

        // TODO SG verify daemon state via event (FAILED)
        // TODO SG: optinally verify reloading

    }

    @Test
    public void verifyAcceptHeader() {

        // JSON
        given().param("format", "json").get().then()
                .assertThat().statusCode(200)
                .assertThat().contentType(ContentType.JSON);
        // XML
        given().header("Accept", ContentType.XML).get().then()
                .assertThat().statusCode(200)
                .assertThat().contentType(ContentType.XML);
        // Atom XML
        given().header("Accept", "application/atom+xml").get().then()
                .assertThat().statusCode(200)
                .assertThat().contentType(ContentType.XML);
        // Default JSON
        given().get().then()
                .assertThat().statusCode(200)
                .assertThat().contentType(ContentType.JSON);
    }
}
