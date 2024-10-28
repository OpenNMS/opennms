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

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.elasticsearch.common.Strings;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint;
import org.opennms.smoketest.rest.GrafanaEndpointRestIT;
import org.opennms.smoketest.ui.framework.Button;
import org.opennms.smoketest.ui.framework.TextInput;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GrafanaEndpointPageIT extends UiPageTest  {

    private Page uiPage;

    @Before
    public void setUp() throws IOException, InterruptedException {
        // Delete all endpoints
        sendDelete("rest/endpoints/grafana");

        uiPage = new Page(getBaseUrlInternal());
        uiPage.open();
    }

    @Test
    public void verifyCRUD() {
        assertThat(uiPage.getEndpoints().size(), is(0));

        // Create dummy Endpoint
        final GrafanaEndpoint dummyEndpoint = GrafanaEndpointRestIT.createDummyEndpoint();
        uiPage.newModal().setInput(dummyEndpoint).save();

        // Verify creation
        final List<UIGrafanaEndpoint> endpoints = uiPage.getEndpoints();
        assertThat(endpoints, hasSize(1));
        final UIGrafanaEndpoint uiEndpoint = endpoints.get(0);
        dummyEndpoint.setId(uiEndpoint.getId());
        assertEquals(dummyEndpoint, uiEndpoint);

        // Edit endpoint
        final GrafanaEndpoint modifiedEndpoint = new GrafanaEndpoint();
        modifiedEndpoint.setId(uiEndpoint.getId());
        modifiedEndpoint.setApiKey("new API Key");
        modifiedEndpoint.setConnectTimeout(1000);
        modifiedEndpoint.setReadTimeout(2000);
        modifiedEndpoint.setDescription("New Description");
        modifiedEndpoint.setUrl("https://url.new.org");
        modifiedEndpoint.setUid("NEW_UID");
        uiPage.editModal(uiEndpoint.getId()).setInput(modifiedEndpoint).save();

        // Verify edit worked
        final UIGrafanaEndpoint uiModifiedEndpoint = uiPage.getEndpoints().get(0);
        assertEquals(modifiedEndpoint, uiModifiedEndpoint);

        // Edit, but cancel
        uiPage.editModal(uiEndpoint.getId()).setInput(dummyEndpoint).cancel();

        // Verify edit was not performed
        assertEquals(modifiedEndpoint, uiPage.getEndpoints().get(0));

        // Delete endpoint
        uiPage.deleteEndpoint(modifiedEndpoint);
        assertThat(uiPage.getEndpoints(), hasSize(0));
    }

    @Test
    public void verifyTestConnection() {
        final GrafanaEndpoint dummyEndpoint = GrafanaEndpointRestIT.createDummyEndpoint();
        final EndpointModal modal = uiPage.newModal().setInput(dummyEndpoint);
        new Button(getDriver(), "verify-endpoint").click();
        execute(() -> {
            pageContainsText("Could not connect");
            return null;
        });
        modal.cancel();
    }

    @Test
    public void testVerifyEndpoint() {
        // Create a GrafanaEndpoint object
        GrafanaEndpoint grafanaEndpoint = new GrafanaEndpoint();
        grafanaEndpoint.setApiKey("testApiKey");
        grafanaEndpoint.setUrl("https://test.url.com");
        // Add any other necessary properties to the grafanaEndpoint

        // Send POST request to /verify
        given()
                .contentType("application/json")
                .body(grafanaEndpoint)
                .when()
                .post("/verify")
                .then()
                .statusCode(200);
        // Check for success status code
    }


    @Test
    public void verifyUniqueUid() {
        // Create endpoint
        final GrafanaEndpoint dummyEndpoint = GrafanaEndpointRestIT.createDummyEndpoint();
        uiPage.newModal().setInput(dummyEndpoint).save();
        assertThat(uiPage.getEndpoints(), hasSize(1));

        // Verify 2nd creation does not work
        final EndpointModal modal = uiPage.newModal().setInput(dummyEndpoint);

        // Manually click the save button
        findElementById("save-endpoint").click();

        // Ensure dialogue is still open
        assertThat(findElementById("endpointModal").isDisplayed(), is(true));
        pageContainsText("An endpoint with uid '" + dummyEndpoint.getUid() + "' already exists.");
        modal.cancel();
    }

    @Test
    public void verifyOnlyOneEndpointIsDeleted() {
        // Create first dummy
        final GrafanaEndpoint dummyEndpoint = GrafanaEndpointRestIT.createDummyEndpoint();
        uiPage.newModal().setInput(dummyEndpoint).save();

        // Create 2nd dummy
        dummyEndpoint.setUid("ANOTHER_UID");
        uiPage.newModal().setInput(dummyEndpoint).save();

        // Verify creation
        assertThat(uiPage.getEndpoints(), hasSize(2));

        // Now delete second element
        final GrafanaEndpoint uiEndpoint = uiPage.getEndpoints().get(1);
        uiPage.deleteEndpoint(uiEndpoint);

        // Verify deletion
        assertThat(uiPage.getEndpoints(), hasSize(1));
    }

    private class Page {
        private final String url;

        public Page(String baseUrl) {
            this.url = Objects.requireNonNull(baseUrl) + "opennms/admin/endpoint/index.jsp";
        }

        public Page open() {
            driver.get(url);
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(pageContainsText("Grafana Endpoints"));
            return this;
        }

        public List<UIGrafanaEndpoint> getEndpoints() {
            return execute(() -> driver.findElements(By.xpath("//table/tbody/tr"))
                    .stream()
                    .map(row -> {
                        final List<WebElement> columns = row.findElements(By.xpath("./td"));
                        final String id = row.getAttribute("data-id");
                        final String uid = columns.get(0).getText();
                        final String url = columns.get(1).getText();
                        final String description = columns.get(3).getText();
                        final String connectTimeout = columns.get(4).getText();
                        final String readTimeout = columns.get(5).getText();

                        // Click reveal to get the API KEY and afterwards click again to hide
                        new Button(getDriver(), "action.revealApiKey." + id).click();
                        new WebDriverWait(driver, Duration.ofSeconds(5)).until(webDriver -> !row.findElements(By.xpath("./td")).get(2).getText().contains("****"));
                        final String apiKey = columns.get(2).getText();
                        new Button(getDriver(), "action.revealApiKey." + id).click();

                        final UIGrafanaEndpoint grafanaEndpoint = new UIGrafanaEndpoint();
                        grafanaEndpoint.setId(Long.parseLong(id));
                        grafanaEndpoint.setApiKey(apiKey);
                        grafanaEndpoint.setDescription(description);
                        grafanaEndpoint.setUid(uid);
                        grafanaEndpoint.setUrl(url);
                        if (!Strings.isNullOrEmpty(connectTimeout)) {
                            grafanaEndpoint.setConnectTimeout(Integer.parseInt(connectTimeout));
                        }
                        if (!Strings.isNullOrEmpty(readTimeout)) {
                            grafanaEndpoint.setReadTimeout(Integer.parseInt(readTimeout));
                        }
                        return grafanaEndpoint;
                    }).collect(Collectors.toList()));
        }

        public EndpointModal newModal() {
            return new EndpointModal()
                    .open(() -> {
                        findElementById("action.addGrafanaEndpoint").click(); // Click add button
                        new WebDriverWait(driver, Duration.ofSeconds(5)).until(pageContainsText("Add Grafana Endpoint"));
                    });
        }

        public EndpointModal editModal(Long endpointId) {
            return new EndpointModal().open(() -> {
                findElementById("action.edit." + endpointId).click();
                new WebDriverWait(driver, Duration.ofSeconds(5)).until(pageContainsText("Edit Grafana Endpoint"));
            });
        }

        public void deleteEndpoint(GrafanaEndpoint endpoint) {
            execute(() -> {
                // Click Delete
                findElementById("action.delete." + endpoint.getId()).click();
                // Wait for confirm popover
                new WebDriverWait(driver, Duration.ofSeconds(5)).until(pageContainsText("Delete Endpoint"));
                // Click Yes in popover
                final String confirmButtonXpath = String.format("//div[@class='popover-content']//p[contains(text(), \"UID '%s'\")]/..//button[text() = 'Yes']", endpoint.getUid());
                final WebElement confirmElement = findElementByXpath(confirmButtonXpath);
                assertThat(confirmElement.isDisplayed(), is(true));
                confirmElement.click();
                return null;
            });
            verifyElementNotPresent(By.xpath("//table/tbody/tr[@data-id='" + endpoint.getId() + "']"));
        }
    }

    private class UIGrafanaEndpoint extends org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint {

    }

    private class EndpointModal {

        public EndpointModal open(Runnable openModal) {
            Objects.requireNonNull(openModal);
            execute(() -> {
                openModal.run();
                return null;
            });
            return this;
        }

        public EndpointModal setInput(GrafanaEndpoint endpoint) {
            // Input form
            new TextInput(getDriver(), "endpoint.uid").setInput(endpoint.getUid());
            new TextInput(getDriver(), "endpoint.apiKey").setInput(endpoint.getApiKey());
            new TextInput(getDriver(), "endpoint.description").setInput(endpoint.getDescription());
            new TextInput(getDriver(), "endpoint.url").setInput(endpoint.getUrl());
            new TextInput(getDriver(), "endpoint.readTimeout").setInput(endpoint.getReadTimeout());
            new TextInput(getDriver(), "endpoint.connectTimeout").setInput(endpoint.getConnectTimeout());
            return this;
        }

        // Save or update
        public void save() {
            execute(() -> {
                findElementById("save-endpoint").click();
                return null;
            });
            ensureClosed();
        }

        // Close dialog
        public void cancel() {
            execute(() -> {
                findElementById("cancel-endpoint").click();
                return null;
            });
            ensureClosed();
        }

        // Ensure dialog closes
        private void ensureClosed() {
            execute(() -> new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.numberOfElementsToBe(By.id("endpointModal"), 0)));
        }
    }

}
