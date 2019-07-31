/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
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
            new WebDriverWait(driver, 5).until(pageContainsText("Grafana Endpoints"));
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
                        new WebDriverWait(driver, 5).until(webDriver -> !row.findElements(By.xpath("./td")).get(2).getText().contains("****"));
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
                        new WebDriverWait(driver, 5).until(pageContainsText("Add Grafana Endpoint"));
                    });
        }

        public EndpointModal editModal(Long endpointId) {
            return new EndpointModal().open(() -> {
                findElementById("action.edit." + endpointId).click();
                new WebDriverWait(driver, 5).until(pageContainsText("Edit Grafana Endpoint"));
            });
        }

        public void deleteEndpoint(GrafanaEndpoint endpoint) {
            execute(() -> {
                // Click Delete
                findElementById("action.delete." + endpoint.getId()).click();
                // Wait for confirm popover
                new WebDriverWait(driver, 5).until(pageContainsText("Delete Endpoint"));
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
            execute(() -> new WebDriverWait(driver, 5).until(ExpectedConditions.numberOfElementsToBe(By.id("endpointModal"), 0)));
        }
    }

}
