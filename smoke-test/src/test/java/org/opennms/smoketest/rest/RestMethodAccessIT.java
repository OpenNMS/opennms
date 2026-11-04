/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.smoketest.containers.OpenNMSContainer;

import io.restassured.http.ContentType;

/**
 * Verifies that the REST security chains in <code>applicationContext-spring-security.xml</code>
 * deny requests that match no authorization rule (NMS-20348).
 * <p>
 * The rules match the HTTP method with an exact-case comparison. Thus a request with a lowercase method
 * (for example <code>get</code>) matches no method rule. A final <code>denyAll</code> rule in each chain
 * must deny that request. The alarm PUT rule must also require a login.
 * <p>
 * curl, RestAssured and most Java HTTP clients change the method to uppercase. Thus this test sends
 * the lowercase requests on a plain socket.
 */
public class RestMethodAccessIT extends AbstractRestIT {

    private static final String PASSWORD = "nms20348-password";
    private static final String USER = "nms20348-user";
    private static final String READONLY_USER = "nms20348-user-readonly";
    private static final String READONLY_ADMIN = "nms20348-admin-readonly";

    private static final String ALARM_UEI = "uei.opennms.org/alarms/trigger";
    private static final String ALARM_SERVICE = "nms20348";
    private static final int MISSING_ALARM_ID = 999999999;

    public RestMethodAccessIT() {
        super(Version.V1, "alarms");
    }

    @Before
    public void createUsers() {
        addUser(USER, PASSWORD, "ROLE_USER");
        addUser(READONLY_USER, PASSWORD, "ROLE_USER", "ROLE_READONLY");
        addUser(READONLY_ADMIN, PASSWORD, "ROLE_ADMIN", "ROLE_READONLY");
    }

    @After
    public void deleteUsers() {
        removeUser(USER);
        removeUser(READONLY_USER);
        removeUser(READONLY_ADMIN);
    }

    @Test
    public void lowercaseMethodWithoutLoginIsUnauthorized() throws IOException {
        for (final String path : new String[] {
                "/opennms/rest/nodes",
                "/opennms/rest/alarms",
                "/opennms/api/v2/nodes",
                "/opennms/nrt/starter" }) {
            assertEquals("get " + path, 401, rawStatus("get", path, null));
            assertEquals("GeT " + path, 401, rawStatus("GeT", path, null));
        }
        assertEquals(401, rawStatus("post", "/opennms/rest/events", null));
        assertEquals(401, rawStatus("delete", "/opennms/rest/categories/nms20348", null));
        assertEquals(401, rawStatus("put", "/opennms/rest/nodes/1", null));
    }

    @Test
    public void lowercaseMethodWithLoginIsForbidden() throws IOException {
        final String admin = OpenNMSContainer.ADMIN_USER + ":" + OpenNMSContainer.ADMIN_PASSWORD;
        for (final String path : new String[] {
                "/opennms/rest/nodes",
                "/opennms/rest/alarms",
                "/opennms/api/v2/nodes",
                "/opennms/nrt/starter" }) {
            assertEquals("get " + path, 403, rawStatus("get", path, admin));
        }
    }

    @Test
    public void uppercaseMethodWithLoginWorks() throws IOException {
        final String admin = OpenNMSContainer.ADMIN_USER + ":" + OpenNMSContainer.ADMIN_PASSWORD;
        assertEquals(200, rawStatus("GET", "/opennms/rest/nodes", admin));
        given().basePath("/opennms/api/v2/nodes").get()
                .then().log().status()
                .assertThat().statusCode(anyOf(is(200), is(204)));
    }

    @Test
    public void alarmPutWithoutLoginIsUnauthorized() {
        final int alarmId = createAlarm();

        // Both requests get the same status, so the response does not show which alarm IDs exist.
        for (final int id : new int[] { alarmId, MISSING_ALARM_ID }) {
            given().auth().none()
                    .contentType(ContentType.URLENC)
                    .formParam("ack", "true")
                    .put("/" + id)
                    .then().log().status()
                    .assertThat().statusCode(401);
        }
    }

    @Test
    public void readOnlyUserCannotAckAlarms() {
        final int alarmId = createAlarm();

        useCredentials(READONLY_USER, PASSWORD);
        given().contentType(ContentType.URLENC)
                .formParam("ack", "true")
                .put("/" + alarmId)
                .then().log().status()
                .assertThat().statusCode(403);
    }

    @Test
    public void usersCanAckAlarms() {
        final int alarmId = createAlarm();

        // ROLE_ADMIN can edit alarms also with ROLE_READONLY. This agrees with SecurityHelper and the alarm UI.
        for (final String user : new String[] { USER, READONLY_ADMIN }) {
            useCredentials(user, PASSWORD);
            given().contentType(ContentType.URLENC)
                    .formParam("ack", "true")
                    .put("/" + alarmId)
                    .then().log().status()
                    .assertThat().statusCode(204);
            given().contentType(ContentType.URLENC)
                    .formParam("ack", "false")
                    .put("/" + alarmId)
                    .then().log().status()
                    .assertThat().statusCode(204);
        }
    }

    /**
     * Send an event that creates an alarm, and return the ID of that alarm.
     */
    private int createAlarm() {
        final Event event = new Event();
        event.setUei(ALARM_UEI);
        event.setSeverity("7");
        event.setParmCollection(Collections.singletonList(new Parm("service", ALARM_SERVICE)));
        stack.opennms().getRestClient().sendEvent(event);

        return await().atMost(2, TimeUnit.MINUTES).pollInterval(2, TimeUnit.SECONDS)
                .until(this::findAlarmId, Optional::isPresent)
                .get();
    }

    private Optional<Integer> findAlarmId() {
        return stack.opennms().getRestClient().getAlarmsByEventUei(ALARM_UEI).getObjects().stream()
                .filter(alarm -> alarm.getReductionKey() != null && alarm.getReductionKey().contains(ALARM_SERVICE))
                .map(OnmsAlarm::getId)
                .findFirst();
    }

    /**
     * Send a request with the method exactly as given, and return the HTTP status code.
     * The credentials are "user:password" for Basic authentication, or null for no login.
     */
    private static int rawStatus(final String method, final String path, final String credentials) throws IOException {
        final StringBuilder request = new StringBuilder()
                .append(method).append(' ').append(path).append(" HTTP/1.1\r\n")
                .append("Host: localhost\r\n")
                .append("Content-Length: 0\r\n")
                .append("Connection: close\r\n");
        if (credentials != null) {
            request.append("Authorization: Basic ")
                    .append(Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)))
                    .append("\r\n");
        }
        request.append("\r\n");

        try (Socket socket = new Socket(stack.opennms().getBaseUrlExternal().getHost(), stack.opennms().getWebPort())) {
            socket.setSoTimeout(30_000);
            final OutputStream out = socket.getOutputStream();
            out.write(request.toString().getBytes(StandardCharsets.US_ASCII));
            out.flush();

            final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            // The status line is "HTTP/1.1 401 Unauthorized".
            final String statusLine = in.readLine();
            if (statusLine == null) {
                throw new IOException("No response to " + method + " " + path);
            }
            return Integer.parseInt(statusLine.split(" ")[1]);
        }
    }
}
