/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertNotEquals;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Test Class for the Index Page.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)
public class IndexPageIT extends OpenNMSSeleniumIT {

    private static final Logger LOG = LoggerFactory.getLogger(IndexPageIT.class);

    /**
     * Can render search boxes.
     *
     * @throws Exception the exception
     */
    @Test
    public void canRenderSearchBoxes() throws Exception {
        driver.get(getBaseUrlInternal() + "opennms/index.jsp");
        // The following input fields will exist on index.jsp, only if includes/search-box.jsp is rendered and processed by AngularJS
        WebElement asyncKsc = findElementByXpath("//input[@ng-model='asyncKsc']");
        Assert.assertNotNull(asyncKsc);
        WebElement asyncNode = findElementByXpath("//input[@ng-model='asyncNode']");
        Assert.assertNotNull(asyncNode);
    }

    @Test
    public void verifyStatusMap() {
        // In order to have anything show up, we have to create a node with long/lat information first
        // A interface and service which does not exist is used, in order to provoke an alarm beeing sent by opennms
        // to have a status >= Warning
        // INITIALIZE
        LOG.info("Initializing foreign source with no detectors");
        String foreignSourceXML = "<foreign-source name=\"" + OpenNMSSeleniumIT.REQUISITION_NAME + "\">\n" +
                "<scan-interval>1d</scan-interval>\n" +
                "<detectors/>\n" +
                "<policies/>\n" +
                "</foreign-source>";
        createForeignSource(REQUISITION_NAME, foreignSourceXML);
        LOG.info("Initializing node with  source with no detectors");
        String requisitionXML = "<model-import foreign-source=\"" + OpenNMSSeleniumIT.REQUISITION_NAME + "\">" +
                "   <node foreign-id=\"tests\" node-label=\"192.0.2.1\">" +
                "       <interface ip-addr=\"192.0.2.1\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"ICMP\"/>" +
                "       </interface>" +
                "       <asset name=\"longitude\" value=\"-0.075949\"/>" +
                "       <asset name=\"latitude\" value=\"51.508112\"/>" +
                "   </node>" +
                "</model-import>";
        createRequisition(REQUISITION_NAME, requisitionXML, 1);

        // try every 5 seconds, for 120 seconds, until the service on 127.0.0.2 has been detected as "down", or fail afterwards
        try {
            setImplicitWait(5, TimeUnit.SECONDS);
            new WebDriverWait(driver, Duration.ofSeconds(120)).until(input -> {
                // refresh page
                input.get(getBaseUrlInternal() + "opennms/index.jsp");

                // Wait until we have markers
                List<WebElement> markerElements = input.findElements(By.xpath("//*[contains(@class, 'leaflet-marker-icon')]"));
                return !markerElements.isEmpty();
            });
        } finally {
            setImplicitWait();
        }
    }

    private String getSessionId() {
        final Set<Cookie> cookies = driver.manage().getCookies();
        for (final Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase("JSESSIONID")) {
                return cookie.getValue().replaceAll(";.*$","");
            }
        }
        return null;
    }

    @Test
    public void testSessionFixation_NMS15310() {
        logout();
        final String preLoginSessionId = getSessionId();
        login();
        final String postLoginSessionId = getSessionId();
        assertNotEquals(preLoginSessionId, postLoginSessionId);
    }

    @Test
    public void verifyDeepLinks() {
        logout();
        driver.get(getBaseUrlInternal() + "opennms/event/detail.jsp?id=999999999");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("j_username")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("j_password")));
        wait.until(ExpectedConditions.elementToBeClickable(By.name("Login")));
        enterText(By.name("j_username"), BASIC_AUTH_USERNAME);
        enterText(By.name("j_password"), BASIC_AUTH_PASSWORD);
        clickElement(By.name("Login"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Event ID Not Found']")));
    }
}
