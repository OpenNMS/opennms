/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Test Class for the Node's assets page.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AssetsPageForNodeIT extends OpenNMSSeleniumTestCase {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AssetsPageForNodeIT.class);

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        LOG.debug("creating node");
        String node = "<node type=\"A\" label=\"TestMachine1\" foreignSource=\"SmokeTests\" foreignId=\"TestMachine1\">" +
                "<assetRecord>" +
                "<description>Right here, right now</description>" +
                "</assetRecord>" +     
                "<labelSource>H</labelSource>" +
                "<sysContact>The Owner</sysContact>" +
                "<sysDescription>" +
                "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                "</sysDescription>" +
                "<sysLocation>DevJam</sysLocation>" +
                "<sysName>TestMachine1</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "<createTime>2011-09-24T07:12:46.421-04:00</createTime>" +
                "<lastCapsdPoll>2011-09-24T07:12:46.421-04:00</lastCapsdPoll>" +
                "</node>";
        sendPost("rest/nodes", node, 201);
        LOG.debug("node created!");
        sendPut("rest/nodes/SmokeTests:TestMachine1/assetRecord", "description=Right here, Right now", 204);
        LOG.debug("asset updated!");

        m_driver.get(getBaseUrl() + "opennms/element/node.jsp?node=SmokeTests:TestMachine1");
    }

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        sendDelete("rest/nodes/SmokeTests:TestMachine1", 202);
    }

    /**
     * Test asset page.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAssetPage() throws Exception {
        findElementByLink("Asset Info").click();
        Alert alert = m_driver.switchTo().alert();
        alert.accept();

        final String descriptionXpath = "(//input[@ng-model='asset[field.model]'])[1]";
        waitForElement(By.xpath(descriptionXpath));
        WebElement description = findElementByXpath(descriptionXpath);
        Assert.assertNotNull(description);
        Assert.assertEquals("Right here, Right now", description.getAttribute("value"));
    }

}
