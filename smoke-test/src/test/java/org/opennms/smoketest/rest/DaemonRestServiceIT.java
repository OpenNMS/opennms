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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.netmgt.daemon.DaemonReloadState;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.HibernateDaoFactory;

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
        final InetSocketAddress pgsql = this.getPostgresService();
        final HibernateDaoFactory daoFactory = new HibernateDaoFactory(pgsql);
        final EventDao eventDao = daoFactory.getDao(EventDaoHibernate.class);

        // Reload a non Valid DaemonName
        reload("WrongDaemonName", 404);
        check("WrongDaemonName", 404, null);


        // Reload of a non reloadable and internal Daemon
        // Verify reload state is unknown, as the daemon is not reloadable
        reload("manager", 400);
        check("manager", 200, DaemonReloadState.Unknown);

        // Reload a reloadable but not enabled Daemon
        reload("snmppoller", 400);
        check("snmppoller", 200, DaemonReloadState.Reloading);

        // Reload of a non reloadable, not enabled Daemon
        reload("correlator", 400);
        check("correlator", 200, DaemonReloadState.Unknown);

        // Reload of a non reloadable but enabled Daemon
        reload("ticketer", 400);
        check("ticketer", 200, DaemonReloadState.Unknown);

        // Reload a reloadable and enabled Daemon
        // Verify reloading workeds
        reload("eventd", 204);
        check("eventd", 200, DaemonReloadState.Success);

        //Verify Reloading and Failure State with the reloadable+enabled Daemon
        verifyReloadingState(eventDao);
        verifyFailureState(eventDao);
    }

    private void reload(String daemonName, int reloadHTTPStatusCode) {
        given().body("").post("/reload/" + daemonName + "/").then()
                .assertThat().statusCode(reloadHTTPStatusCode);

    }

    private void check(String daemonName, int checkHTTPStatusCode, DaemonReloadState state) {
        if (state != null) {
            await().atMost(5, TimeUnit.SECONDS)
                    .pollDelay(1, TimeUnit.SECONDS)
                    .until(() -> given().get("/reload/" + daemonName + "/").then()
                            .assertThat().statusCode(checkHTTPStatusCode)
                            .assertThat()
                            .body("reloadState", equalTo(state.toString())));
        } else {
            await().atMost(5, TimeUnit.SECONDS)
                    .pollDelay(1, TimeUnit.SECONDS)
                    .until(() -> given().get("/reload/" + daemonName + "/").then()
                            .assertThat().statusCode(checkHTTPStatusCode)
                    );
        }
    }

    private void verifyReloadingState(EventDao eventDao) {
        Date time = new Date();
        // Reload a reloadable and enabled Daemon
        given().body("").post("/reload/eventd/").then()
                .assertThat().statusCode(204);

        List<String> al = new ArrayList<>();
        al.add(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI);
        List<OnmsEvent> successfulEventsAfterReload = eventDao.getEventsAfterDate(al, time);

        Assert.assertThat(successfulEventsAfterReload, hasSize(1));

        if (successfulEventsAfterReload.size() == 1) {
            eventDao.delete(successfulEventsAfterReload.get(0));
            check("eventd", 200, DaemonReloadState.Reloading);
        }
    }

    private void verifyFailureState(EventDao eventDao) {
        OnmsEvent onmsEvent = new OnmsEvent();

        OnmsDistPoller distPoller = new OnmsDistPoller(DistPollerDao.DEFAULT_DIST_POLLER_ID);
        onmsEvent.setDistPoller(distPoller);

        Date time = new Date();
        onmsEvent.setEventTime(time);
        onmsEvent.setEventCreateTime(time);

        onmsEvent.setEventLog("Y");
        onmsEvent.setEventDisplay("Y");
        onmsEvent.setEventDescr("Failed Reload Event");
        onmsEvent.setEventLogMsg("Test log message");

        onmsEvent.setEventSeverity(OnmsSeverity.MAJOR.getId());

        onmsEvent.setEventUei(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI);
        onmsEvent.setEventSource("Daemon Rest Service IT");


        OnmsEventParameter ep1 = new OnmsEventParameter(
                onmsEvent,
                EventConstants.PARM_REASON,
                "Its needed for the DaemonRestService IT test to have a Failed Reload Event",
                "string");
        OnmsEventParameter ep2 = new OnmsEventParameter(
                onmsEvent,
                EventConstants.PARM_DAEMON_NAME,
                "eventd",
                "string");


        onmsEvent.addEventParameter(ep1);
        onmsEvent.addEventParameter(ep2);

        // Add Failed Event
        eventDao.save(onmsEvent);
        eventDao.flush();

        check("eventd",200, DaemonReloadState.Failed);

        // Remove Failed Event
        eventDao.delete(onmsEvent);
        eventDao.flush();
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
