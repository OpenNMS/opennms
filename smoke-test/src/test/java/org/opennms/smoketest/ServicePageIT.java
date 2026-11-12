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

package org.opennms.smoketest;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXB;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.smoketest.ui.NodeDetailsPage;
import org.openqa.selenium.By;

/**
 * The Delete button on the service page must only be shown to users with ROLE_ADMIN (NMS-20468).
 */
public class ServicePageIT extends OpenNMSSeleniumIT {
    private static final String NODE = "SmokeTests:ServicePageNode";
    private static final String USER_PASSWORD = "SmokeTestPassword1!";

    @Before
    public void setUp() throws Exception {
        final String node = "<node type=\"A\" label=\"ServicePageNode\" foreignSource=\"SmokeTests\" foreignId=\"ServicePageNode\">" +
                "<labelSource>H</labelSource>" +
                "<sysName>ServicePageNode</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "</node>";
        sendPost("rest/nodes", node, 201);

        final String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
                "<ipAddress>10.10.10.10</ipAddress>" +
                "</ipInterface>";
        sendPost("rest/nodes/" + NODE + "/ipinterfaces", ipInterface, 201);

        final String service = "<service status=\"A\">" +
                "<serviceType id=\"1\"><name>ICMP</name></serviceType>" +
                "</service>";
        sendPost("rest/nodes/" + NODE + "/ipinterfaces/10.10.10.10/services", service, 201);

        // USER_NAME is removed by the cleanup in AbstractOpenNMSSeleniumHelper
        addUser(USER_NAME, USER_PASSWORD, "ROLE_USER");
    }

    @After
    public void tearDown() throws Exception {
        sendDelete("rest/nodes/" + NODE, 202);
    }

    @Test
    public void testDeleteButtonVisibleForAdmin() {
        openServicePage();

        findElementByLink("Delete");
        final String action = findElementByCss("form[name='delete']").getAttribute("action");
        assertTrue("Unexpected form action: " + action, action.endsWith("admin/deleteService"));
    }

    @Test
    public void testDeleteButtonHiddenForNonAdmin() {
        logout();
        login(USER_NAME, USER_PASSWORD);

        openServicePage();

        assertElementDoesNotExist(By.linkText("Delete"));
        assertElementDoesNotExist(By.name("delete"));
    }

    private void openServicePage() {
        new NodeDetailsPage(this).open(NODE).clickService("ICMP");
        // every user sees the Meta-Data link, so the service page has loaded
        findElementByLink("Meta-Data");
    }

    private void addUser(final String username, final String password, final String... roles) throws Exception {
        final OnmsUser user = new OnmsUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setPasswordSalted(false);
        user.setRoles(Arrays.asList(roles));

        final StringWriter xml = new StringWriter();
        JAXB.marshal(user, xml);
        sendPost("rest/users?hashPassword=true", xml.toString(), 201);

        // UserFactory checks users.xml for changes at most once per second, so a new user cannot log in at once.
        await().atMost(30, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> given().auth().preemptive().basic(username, password)
                        .get(buildUrlExternal("rest/whoami")).getStatusCode() != 401);
    }
}
