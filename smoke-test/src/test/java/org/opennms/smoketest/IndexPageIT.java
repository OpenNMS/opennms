/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

/**
 * The Test Class for the Index Page.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class IndexPageIT extends OpenNMSSeleniumTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(IndexPageIT.class);

    // Verifies that one can use the node id input as node label input.
    // The result should be empty, and not BAD REQUEST. See NMS-9419
    @Test
    @Ignore("This test fails in 20.0.1 for some unknown reason - the nodeIdSearchButton can't be clicked")
    public void canSearchForNodeLabelInNodeId() throws InterruptedException {
        // Verify search. Should not result in 400 BAD REQUEST
        enterText(By.name("nodeId"), "192.0.2.1");
        clickElement(By.name("nodeIdSearchButton"));
        wait.until(pageContainsText("None found."));
    }

    /**
     * Can render search boxes.
     *
     * @throws Exception the exception
     */
    @Test
    public void canRenderSearchBoxes() throws Exception {
        m_driver.get(getBaseUrl() + "opennms/index.jsp");
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
        String foreignSourceXML = "<foreign-source name=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">\n" +
                "<scan-interval>1d</scan-interval>\n" +
                "<detectors/>\n" +
                "<policies/>\n" +
                "</foreign-source>";
        createForeignSource(REQUISITION_NAME, foreignSourceXML);
        LOG.info("Initializing node with  source with no detectors");
        String requisitionXML = "<model-import foreign-source=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">" +
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
            new WebDriverWait(m_driver, 120).until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(@Nullable WebDriver input) {
                    // refresh page
                    input.get(getBaseUrl() + "opennms/index.jsp");

                    // Wait until we have markers
                    List<WebElement> markerElements = input.findElements(By.xpath("//*[contains(@class, 'leaflet-marker-icon')]"));
                    return !markerElements.isEmpty();
                }
            });
        } finally {
            setImplicitWait();
        }
    }

}
