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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Walks the Graph Collections pages of the Vue UI: list, create with the graph picker, view,
 * edit and delete. The former KSC editor test, moved to the SPA that replaced those pages.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GraphCollectionsIT extends OpenNMSSeleniumIT {

    private static final String TITLE = "Smoke Test Collection 1";
    private static final String RENAMED_TITLE = "Smoke Test Collection Uno";

    protected void goToList() {
        driver.get(getBaseUrlInternal() + "opennms/ui/index.html#/graph-collections");
        wait.until(pageContainsText("Graph Collections"));
    }

    @Before
    public void setUp() throws Exception {
        // a failed earlier run may have left its collection behind
        deleteTestCollections();
        // Create the test requisition, this will block until the test node is actually created
        createRequisition();
    }

    @After
    public void tearDown() throws Exception {
        deleteTestCollections();
    }

    @Test
    public void testGraphCollections() throws Exception {
        goToList();
        checkList();
        createCollection();
        goToList();
        editCollection();
        goToList();
        deleteCollection();
    }

    protected void checkList() {
        assertElementDoesNotExist(titleLink(TITLE));
        assertElementDoesNotExist(titleLink(RENAMED_TITLE));
        waitForElement(By.cssSelector("[data-test='create-collection']"));
    }

    protected void createCollection() throws Exception {
        clickElement(By.cssSelector("[data-test='create-collection']"));
        wait.until(pageContainsText("New Graph Collection"));
        waitForSpinner();
        enterText(By.cssSelector("[data-test='title']"), TITLE);

        // the picker: node, then resource, then graph type
        clickElement(By.cssSelector("[data-test='add-graph']"));
        waitForElement(By.id("picker-node"));
        enterText(By.id("picker-node"), "TestMachine1");
        clickElement(By.xpath("//li[@role='option' and contains(., 'TestMachine1')]"));
        // the response time resource appears once the poller has stored its first sample
        clickElement(By.xpath("//*[@data-test='picker-resources']//li[@role='option' and contains(., 'Response Time for 127.0.0.1')]"));
        wait.until(pageContainsText("Response Time for 127.0.0.1"));
        pickSelectOption("picker-graphtype", null);
        enterText(By.cssSelector("[data-test='picker-title']"), "Smoke Test Graph Title 1");
        clickElement(By.cssSelector("[data-test='picker-save']"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("[data-test='graph-title']"), "Smoke Test Graph Title 1"));

        // save and land on the view page, which draws the graph
        clickElement(By.cssSelector("[data-test='save']"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("[data-test='gc-view-title']"), TITLE));
        waitForElement(By.cssSelector("[data-test='gc-cell']"));
        waitForElement(By.xpath("//*[@data-test='gc-cell']//canvas"));
    }

    protected void editCollection() throws Exception {
        waitForElement(titleLink(TITLE));
        clickElement(rowButton(TITLE, "edit-collection"));
        waitForValue(By.cssSelector("[data-test='title']"), TITLE);
        waitForSpinner();

        // rename, offer the timespan override, and re-time the graph
        enterText(By.cssSelector("[data-test='title']"), RENAMED_TITLE);
        assertFalse(findElementById("gc-show-timespan").isSelected());
        // the checkbox input is visually hidden, so it is clicked through script rather than the pointer
        clickElementUsingScript(findElementById("gc-show-timespan"));
        assertTrue(findElementById("gc-show-timespan").isSelected());
        clickElement(By.cssSelector("[data-test='edit-graph']"));
        waitForElement(By.id("picker-title"));
        enterText(By.cssSelector("[data-test='picker-title']"), "Smoke Test Graph Title I");
        pickSelectOption("picker-timespan", "3 month");
        clickElement(By.cssSelector("[data-test='picker-save']"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("[data-test='graph-title']"), "Smoke Test Graph Title I"));

        clickElement(By.cssSelector("[data-test='save']"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("[data-test='gc-view-title']"), RENAMED_TITLE));
        // the override the edit switched on is now offered on the view page
        waitForElement(By.id("gc-timespan"));
    }

    protected void deleteCollection() throws Exception {
        waitForElement(titleLink(RENAMED_TITLE));
        clickElement(rowButton(RENAMED_TITLE, "delete-collection"));
        clickElement(By.xpath("//div[@role='dialog']//button[.//span[normalize-space(.)='Delete']]"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(titleLink(RENAMED_TITLE)));
        assertElementDoesNotExist(titleLink(RENAMED_TITLE));
    }

    /**
     * The editor loads under the page spinner, an overlay that fades out for half a second after
     * the load and intercepts clicks until it is gone. clickElement retries through that, but
     * enterText clicks once, so wait it out before typing into a freshly loaded page.
     */
    private void waitForSpinner() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.spinner")));
    }

    /**
     * Open a PrimeVue select by the id of its combobox and click the option with the given text,
     * or the first option when text is null. The option list is looked up through the combobox's
     * aria-controls, so another open list on the page cannot be picked from by mistake.
     */
    private void pickSelectOption(final String comboboxId, final String optionText) {
        final WebElement combobox = clickElement(By.id(comboboxId));
        final String listId = waitUntil(() -> {
            final String controls = combobox.getAttribute("aria-controls");
            if (controls == null || controls.isEmpty()) {
                throw new IllegalStateException("select " + comboboxId + " has not opened yet");
            }
            return controls;
        });
        final String option = optionText == null
                ? "//ul[@id='" + listId + "']//li[@role='option'][1]"
                : "//ul[@id='" + listId + "']//li[@role='option' and normalize-space(.)='" + optionText + "']";
        clickElement(By.xpath(option));
    }

    /** Remove the collections this test creates, by title, through the REST API. */
    private void deleteTestCollections() throws Exception {
        final String body = getRequest(new HttpGet(getBaseUrlExternal() + "opennms/api/v2/graph-collections")).getResponseText();
        if (body == null || body.isBlank()) {
            return;
        }
        final JSONArray collections = new JSONArray(body);
        for (int i = 0; i < collections.length(); i++) {
            final JSONObject collection = collections.getJSONObject(i);
            final String title = collection.optString("title");
            if (TITLE.equals(title) || RENAMED_TITLE.equals(title)) {
                sendDelete("api/v2/graph-collections/" + collection.getInt("id"), 204);
            }
        }
    }

    private static By titleLink(final String title) {
        return By.xpath("//a[@data-test='collection-title' and normalize-space(.)='" + title + "']");
    }

    private static By rowButton(final String title, final String dataTest) {
        return By.xpath("//a[@data-test='collection-title' and normalize-space(.)='" + title + "']/ancestor::tr//*[@data-test='" + dataTest + "']");
    }

    private void createRequisition() throws Exception {
        final String req = "<model-import xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\" date-stamp=\"2006-03-09T00:03:09\" foreign-source=\"" + REQUISITION_NAME + "\">" +
                "<node node-label=\"TestMachine1\" foreign-id=\"TestMachine1\">" +
                "<interface ip-addr=\"127.0.0.1\" snmp-primary=\"P\" descr=\"localhost\">" +
                "<monitored-service service-name=\"ICMP\"/>" +
                "<monitored-service service-name=\"HTTP\"/>" +
                "</interface>" +
                "</node>" +
                "</model-import>";
        createRequisition(REQUISITION_NAME, req, 1);
    }
}
