/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.smoketest;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.IpInterfaceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MonitoredServiceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetadataPageIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(MetadataPageIT.class);
    private static final String UNPRIVILEDGED_USERNAME = "foo";
    private static final String UNPRIVILEDGED_PASSWORD = "bar";
    private NodeDao nodeDao;
    private IpInterfaceDao ipInterfaceDao;
    private MonitoredServiceDao monitoredServiceDao;

    @Before
    public void setUp() throws Exception {
        nodeDao = stack.postgres().getDaoFactory().getDao(NodeDaoHibernate.class);
        ipInterfaceDao = stack.postgres().getDaoFactory().getDao(IpInterfaceDaoHibernate.class);
        monitoredServiceDao = stack.postgres().getDaoFactory().getDao(MonitoredServiceDaoHibernate.class);

        createUnpriviledgedUser();

        LOG.debug("Creating node...");

        final String node = "<node type=\"A\" label=\"TestNode\" foreignSource=\"SmokeTests\" foreignId=\"TestNode\">" +
                        "<labelSource>H</labelSource>" +
                        "<sysContact>Me</sysContact>" +
                        "<sysDescription>PDP-8</sysDescription>" +
                        "<sysLocation>German DevJam 2019</sysLocation>" +
                        "<sysName>TestNode</sysName>" +
                        "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                        "<createTime>2019-02-05T13:25:00.123-04:00</createTime>" +
                        "<lastCapsdPoll>2019-02-05T13:20:00.456-04:00</lastCapsdPoll>" +
                        "</node>";
        sendPost("rest/nodes", node, 201);

        LOG.debug("Node created!");
        LOG.debug("Creating an interface...");

        final String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
                "<ipAddress>10.10.10.10</ipAddress>" +
                "<hostName>test-machine1.local</hostName>" +
                "</ipInterface>";
        sendPost("rest/nodes/SmokeTests:TestNode/ipinterfaces", ipInterface, 201);

        LOG.debug("Interface created!");
        LOG.debug("Creating a service...");

        final String service = "<service status=\"A\">\n" +
                "<serviceType id=\"1\">\n" +
                "<name>ICMP</name>\n" +
                "</serviceType>\n" +
                "</service>";
        sendPost("rest/nodes/SmokeTests:TestNode/ipinterfaces/10.10.10.10/services", service, 201);

        LOG.debug("Service created!");

        final OnmsNode onmsNode = nodeDao.get("SmokeTests:TestNode");
        final List<OnmsMetaData> nodeMetadataList = new ArrayList<>();
        nodeMetadataList.add(new OnmsMetaData("contextA","keyA1","valueA1"));
        nodeMetadataList.add(new OnmsMetaData("contextA","keyA2","valueA2"));
        nodeMetadataList.add(new OnmsMetaData("contextA","keyA3","valueA3"));
        nodeMetadataList.add(new OnmsMetaData("contextB","keysecretB1","valueB1"));
        nodeMetadataList.add(new OnmsMetaData("contextB","keySecretB2","valueB2"));
        nodeMetadataList.add(new OnmsMetaData("contextB","keyB3","valueB3"));
        nodeMetadataList.add(new OnmsMetaData("contextC","keypasswordC1","valueC1"));
        nodeMetadataList.add(new OnmsMetaData("contextC","keyPasswordC2","valueC2"));
        nodeMetadataList.add(new OnmsMetaData("contextC","keyC3","valueC3"));
        onmsNode.setMetaData(nodeMetadataList);
        nodeDao.saveOrUpdate(onmsNode);

        final OnmsIpInterface onmsIpInterface = ipInterfaceDao.get(onmsNode, "10.10.10.10");
        final List<OnmsMetaData> interfaceMetadataList = new ArrayList<>();
        interfaceMetadataList.add(new OnmsMetaData("contextD","keyD1","valueD1"));
        onmsIpInterface.setMetaData(interfaceMetadataList);
        ipInterfaceDao.saveOrUpdate(onmsIpInterface);

        final OnmsMonitoredService onmsMonitoredService = monitoredServiceDao.get(onmsNode.getId(), onmsIpInterface.getIpAddress(), "ICMP");
        final List<OnmsMetaData> serviceMetadataList = new ArrayList<>();
        serviceMetadataList.add(new OnmsMetaData("contextE", "keyE1","valueE1"));
        onmsMonitoredService.setMetaData(serviceMetadataList);
        monitoredServiceDao.saveOrUpdate(onmsMonitoredService);
    }

    @After
    public void tearDown() throws Exception {
        LOG.debug("Deleting node...");
        int nodeId = nodeDao.get("SmokeTests:TestNode").getId();
        nodeDao.delete(nodeId);
        LOG.debug("Node deleted!");
        deleteUnpriviledgedUser();
    }

    protected String acceptAlert(String expectedText) {
        LOG.debug("handleAlerm: expectedText={}", expectedText);

        try {
            Alert alert = this.driver.switchTo().alert();
            String alertText = alert.getText();
            if (expectedText != null) {
                Assert.assertEquals(expectedText, alertText);
            }

            alert.accept();
            return alertText;
        } catch (NoAlertPresentException var4) {
            LOG.debug("handleAlert: no alert is active");
        } catch (TimeoutException var5) {
            LOG.debug("handleAlert: no alert was found");
        }

        return null;
    }

   // Replace your existing clickElement(final By by) in AbstractOpenNMSSeleniumHelper.java with this:

public WebElement clickElement(final By by) {
    // We rely on waitUntil to ensure the element is found immediately (not null)
    return waitUntil(new Callable<WebElement>() {
        @Override
        public WebElement call() throws Exception {
            final WebElement el = getElementImmediately(by);
            if (el == null) {
                // Return null so waitUntil can retry or time out
                return null;
            }

            // 1. Attempt Native Selenium Click (Preferred method)
            try {
                el.click();
                return el;
            } catch (final Exception e) {
                // Catch ElementNotInteractableException (and others)
                LOG.warn("Native click failed for element {}. Retrying with JavaScript click. Error: {}", by, e.getMessage());

                // 2. Fallback to JavaScript (Forces scroll and click)
                try {
                    final JavascriptExecutor js = (JavascriptExecutor) getDriver();
                    
                    // Force the element into the center of the viewport
                    js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
                    
                    // Use JS to perform the click
                    js.executeScript("arguments[0].click();", el);
                    
                    return el;
                } catch (final Exception jsException) {
                    // Re-throw the original exception if JS also fails to click
                    LOG.error("JavaScript click also failed for element {}.", by, jsException);
                    throw e; 
                }
            }
        }
    });
}

    private void createUnpriviledgedUser() {
        LOG.debug("Creating unpriviledged user...");

        // opening the admin page
        adminPage();

        // navigate to the new user page
        findElementByLink("Configure Users, Groups and On-Call Roles").click();
        findElementByLink("Configure Users").click();
        findElementByLink("Add new user").click();

        // enter user credentials
        enterText(By.id("userID"), UNPRIVILEDGED_USERNAME);
        enterText(By.id("pass1"), UNPRIVILEDGED_PASSWORD);
        enterText(By.id("pass2"), UNPRIVILEDGED_PASSWORD);
        // proceed
        findElementByXpath("//button[@type='submit' and text()='OK']").click();
        // finish
        waitUntil(pageContainsText("Finish"));
        findElementById("saveUserButton").click();

        // check for the new user
        waitUntil(pageContainsText("Add new user"));
        waitUntil(pageContainsText(UNPRIVILEDGED_USERNAME));

        LOG.debug("Unpriviledged user created!");
    }

    private void loginUnpriviledgedUser() {
        logout();
        this.driver.get(this.getBaseUrlInternal() + "opennms/login.jsp");
        this.enterText(By.name("j_username"), UNPRIVILEDGED_USERNAME);
        this.enterText(By.name("j_password"), UNPRIVILEDGED_PASSWORD);
        this.findElementByName("Login").click();
    }

    @Test
    public void testMetadataPage() throws Exception {
        // visit the node's page
        driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=SmokeTests:TestNode");

        // go to the metadata page
        findElementByLink("Meta-Data").click();

        // check for contexts
        waitUntil(pageContainsText("Context contextA"));
        waitUntil(pageContainsText("Context contextB"));
        waitUntil(pageContainsText("Context contextC"));

        // check keys
        waitUntil(pageContainsText("keyA1"));
        waitUntil(pageContainsText("keyA2"));
        waitUntil(pageContainsText("keyA3"));
        waitUntil(pageContainsText("keysecretB1"));
        waitUntil(pageContainsText("keySecretB2"));
        waitUntil(pageContainsText("keyB3"));
        waitUntil(pageContainsText("keypasswordC1"));
        waitUntil(pageContainsText("keyPasswordC2"));
        waitUntil(pageContainsText("keyC3"));

        // check values
        waitUntil(pageContainsText("valueA1"));
        waitUntil(pageContainsText("valueA2"));
        waitUntil(pageContainsText("valueA3"));
        waitUntil(pageContainsText("valueB1"));
        waitUntil(pageContainsText("valueB2"));
        waitUntil(pageContainsText("valueB3"));
        waitUntil(pageContainsText("valueC1"));
        waitUntil(pageContainsText("valueC2"));
        waitUntil(pageContainsText("valueC3"));

        // visit the node's page to click the interface 10.10.10.10
        driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=SmokeTests:TestNode");
        findElementByLink("10.10.10.10").click();
        // go to the metadata page
        findElementByLink("Meta-Data").click();
        // checking interface-level values
        waitUntil(pageContainsText("Context contextD"));
        waitUntil(pageContainsText("keyD1"));
        waitUntil(pageContainsText("valueD1"));

        // visit the node's page to click the service ICMP
        driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=SmokeTests:TestNode");
        findElementByLink("ICMP").click();
        // go to the metadata page
        findElementByLink("Meta-Data").click();
        // checking service-level values
        waitUntil(pageContainsText("Context contextE"));
        waitUntil(pageContainsText("keyE1"));
        waitUntil(pageContainsText("valueE1"));

        // login with the unpriviledged user
        loginUnpriviledgedUser();

        // visit the node's page
        driver.get(getBaseUrlInternal() + "opennms/element/node.jsp?node=SmokeTests:TestNode");

        // go to the metadata page
        findElementByLink("Meta-Data").click();

        // check for contexts
        waitUntil(pageContainsText("Context contextA"));
        waitUntil(pageContainsText("Context contextB"));
        waitUntil(pageContainsText("Context contextC"));

        // check keys
        waitUntil(pageContainsText("keyA1"));
        waitUntil(pageContainsText("keyA2"));
        waitUntil(pageContainsText("keyA3"));
        waitUntil(pageContainsText("keysecretB1")); // value for this key must be hidden
        waitUntil(pageContainsText("keySecretB2")); // value for this key must be hidden
        waitUntil(pageContainsText("keyB3"));
        waitUntil(pageContainsText("keypasswordC1")); // value for this key must be hidden
        waitUntil(pageContainsText("keyPasswordC2")); // value for this key must be hidden
        waitUntil(pageContainsText("keyC3"));

        // check for visible and hidden values
        waitUntil(pageContainsText("valueA1"));
        waitUntil(pageContainsText("valueA2"));
        waitUntil(pageContainsText("valueA3"));
        waitUntil(pageContainsText("***")); // hidden value
        waitUntil(pageContainsText("***")); // hidden value
        waitUntil(pageContainsText("valueB3"));
        waitUntil(pageContainsText("***")); // hidden value
        waitUntil(pageContainsText("***")); // hidden value
        waitUntil(pageContainsText("valueC3"));
    }
}
