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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.elasticsearch.common.Strings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint;
import org.opennms.smoketest.containers.GrafanaContainer;
import org.opennms.smoketest.rest.GrafanaEndpointRestIT;
import org.opennms.smoketest.ui.framework.Button;
import org.opennms.smoketest.ui.framework.TextInput;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GrafanaEndpointPageIT extends UiPageTest  {


    private static final Logger LOG = LoggerFactory.getLogger(GrafanaEndpointPageIT.class);

    private Page uiPage;

    private static OkHttpClient client;
    private static String token;
    private static ObjectMapper objectMapper;
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String VIEWER= "Viewer";
    private static final String NAME="my-service-account";
    private static GrafanaContainer grafanaContainer;
    private static final String GRAFANA_URL = " http://localhost";

    @Before
    public void setUp() throws Exception {
        // Delete all endpoints
        sendDelete("rest/endpoints/grafana");

        uiPage = new Page(getBaseUrlInternal());
        uiPage.open();
        objectMapper = new ObjectMapper();
        client = new OkHttpClient();
        grafanaContainer = new GrafanaContainer();
        grafanaContainer.start();
        String serviceAccount = createServiceAccount();
        token = createToken(serviceAccount);
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

    public String createServiceAccount() {

        String credential = Credentials.basic("admin", "admin");
        Integer port = grafanaContainer.getWebPort();
        String json = "{\"name\":\"" + "my-service-account" + "\",\"role\":\"" + VIEWER + "\"}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json);
        Request request = new Request.Builder()
                .url(GRAFANA_URL + ":" + port +"/api/serviceaccounts")
                .post(body)
                .addHeader("Authorization", credential)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOG.info("Unexpected code " + response);
            }
            JsonNode jsonNode = objectMapper.readTree(response.body().string());
            return jsonNode.get("id").asText();

        } catch (IOException e) {
            LOG.info("Exception" ,e);
        }
        return null;
    }

    public String createToken(String serviceAccount) {

        String credential = Credentials.basic("admin", "admin");
        Integer port = grafanaContainer.getWebPort();
        System.out.println("port: " + port);
        String json = "{\"name\":\"" + "my-service-account-token" +"\"}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json);
        Request request = new Request.Builder()
                .url(GRAFANA_URL + ":" + port +"/api/serviceaccounts/"+Integer.parseInt(serviceAccount)+"/tokens")
                .post(body)
                      .addHeader("Authorization", credential)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOG.info("Unexpected code " + response);
            }
            System.out.println("res" + response);
            JsonNode jsonNode = objectMapper.readTree(response.body().string());
            return jsonNode.get("key").asText();

        } catch (IOException e) {
            LOG.info("Exception" ,e);
        }
        return null;
    }

    @Test
    public void verifyGrafanaConnection() throws JsonProcessingException {

        final GrafanaEndpoint endpoint = createEndpointConnection();
        System.out.println(endpoint.getApiKey());
        System.out.println(getBaseUrlInternal());
        System.out.println(GRAFANA_URL + ":" +grafanaContainer.getWebPort());
        final EndpointModal modal = uiPage.newModal().setInput(endpoint);
        new Button(getDriver(), "verify-endpoint").click();

        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));

        execute(() -> {
            pageContainsText("Connected");
            return null;
        });


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
    public static GrafanaEndpoint createEndpointConnection() throws JsonProcessingException {

        Integer port = grafanaContainer.getWebPort();

        final GrafanaEndpoint endpoint = new GrafanaEndpoint();
        endpoint.setId(200L);
        endpoint.setUid("7775ad83-4393-4803-9895-7d50dc292b4f");
        endpoint.setApiKey(token);
        endpoint.setUrl("http://localhost" + ":" + port +"/");
        endpoint.setDescription("dummy description");
        endpoint.setReadTimeout(3000);
        endpoint.setConnectTimeout(3000);
        return endpoint;
    }
}