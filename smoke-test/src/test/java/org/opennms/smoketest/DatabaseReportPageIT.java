/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.containers.WebhookEndpointContainer;
import org.opennms.smoketest.ui.framework.Button;
import org.opennms.smoketest.ui.framework.CheckBox;
import org.opennms.smoketest.ui.framework.DeleteAllButton;
import org.opennms.smoketest.ui.framework.Element;
import org.opennms.smoketest.ui.framework.Page;
import org.opennms.smoketest.ui.framework.Select;
import org.opennms.smoketest.ui.framework.TextInput;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.com.google.common.collect.Lists;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;

import io.restassured.RestAssured;

public class DatabaseReportPageIT extends UiPageTest {

    private static Logger LOG = LoggerFactory.getLogger(DatabaseReportPageIT.class);

    @ClassRule
    public static WebhookEndpointContainer webhookEndpointContainer = new WebhookEndpointContainer();

    private DatabaseReportPage page;

    @Before
    public void before() {
        page = new DatabaseReportPage(getDriver(), getBaseUrlInternal());
        page.open();

        LOG.debug("Delete all previously existing report schedules");
        new ScheduledReportsTab(getDriver()).open().deleteAll();
        LOG.debug("Delete all previously existing persisted reports");
        new PersistedReportsTab(getDriver()).open().deleteAll();
        LOG.debug("Cleanup done. Running test now...");
    }

    @Test
    public void verifyAdhocReport() {
        // Run Report
        new ReportTemplateTab(getDriver()).open()
                .select(EarlyMorningReport.name)
                .format(Formats.PDF)
                .createReport();

        // Verify Creation
        final File downloadedFile = new File(getDownloadsFolder(), EarlyMorningReport.filename(Formats.PDF));
        await().atMost(2, MINUTES).pollInterval(5, SECONDS).until(() -> {
            LOG.debug("Expecting file '{}' to exist: {}", downloadedFile, downloadedFile.exists());
            return downloadedFile.exists();
        });
    }

    @Test
    public void verifyDeliverReport() {
        // Trigger Delivery
        new ReportTemplateTab(getDriver()).open()
                .select(EarlyMorningReport.name)
                .deliverReport(DeliveryOptions.DEFAULTS);

        // Verify delivery
        new PersistedReportsTab(getDriver()).open();
        await().atMost(2, MINUTES).pollInterval(5, SECONDS)
                .until( () -> {
                        new Button(driver, "action.refresh").click();
                        final Optional<PersistedReportElement> any = new PersistedReportsTab(getDriver())
                            .open()
                            .getPersistedReports().stream()
                            .filter((input) -> input.title.equals(EarlyMorningReport.id + " admin"))
                            .findAny();
                        return any.isPresent();
                    }
                );
    }

    @Test
    public void verifyScheduleReport() {
        // Define Schedule
        final String cronExpression = "0 0 0/5 * * ?";
        new ReportTemplateTab(getDriver()).open()
                .select(EarlyMorningReport.name)
                .scheduleReport(DeliveryOptions.DEFAULTS, cronExpression);

        // Verify Schedule
        final Optional<ReportScheduleElement> any = new ScheduledReportsTab(getDriver())
                .open()
                .getScheduledReports().stream()
                .filter((input) -> input.templateName.equals(EarlyMorningReport.id) && input.cronExpression.equals(cronExpression))
                .findAny();
        assertThat(any.isPresent(), is(true));
        assertThat(any.get().cronExpression, is(cronExpression));
    }

    @Test
    public void verifyEditSchedule() {
        // Define Schedule
        final String cronExpression = "0 0 0/5 * * ?";
        new ReportTemplateTab(getDriver()).open()
                .select(EarlyMorningReport.name)
                .scheduleReport(DeliveryOptions.DEFAULTS, cronExpression);

        // Verify Schedule
        LOG.debug("Checking if schedule was persisted...");
        final Optional<ReportScheduleElement> any = new ScheduledReportsTab(getDriver())
                .open()
                .getScheduledReports().stream()
                .filter((input) -> input.templateName.equals(EarlyMorningReport.id) && input.cronExpression.equals(cronExpression))
                .findAny();
        LOG.debug("One schedule matching the schedule: {}", any.isPresent());
        assertThat(any.isPresent(), is(true));

        // Edit Schedule
        final String updatedCronExpression = "1 2 0/10 ? * MON,TUE";
        new ScheduledReportsTab(getDriver())
                .open()
                .updateSchedule(EarlyMorningReport.id + " admin",
                      DeliveryOptions.DEFAULTS
                              .emailRecipients(Lists.newArrayList("opennms-test@opennms.org")), // See NMS-12432 for more details
                      updatedCronExpression);

        // Verify it actually was persisted and the UI reloaded
        LOG.debug("Checking if schedule was updated...");
        final Optional<ReportScheduleElement> findMe = new ScheduledReportsTab(getDriver())
                .open()
                .getScheduledReports().stream()
                .filter((input) -> input.templateName.equals(EarlyMorningReport.id) && input.cronExpression.equals(updatedCronExpression))
                .findAny();
        LOG.debug("Schedule was edited, now verify exactly one schedule matches: {}", findMe.isPresent());
        assertThat(findMe.isPresent(), is(true));
    }


    // Verifies that a generated Report can be send to an HTTP Endpoint
    @Test
    public void verifyWebhookDelivery() throws IOException {
        // Setup the Http Endpoint
        RestAssured.baseURI = webhookEndpointContainer.getBaseUrlExternal().toString();
        RestAssured.port = webhookEndpointContainer.getWebPort();

        // Verify nothing was posted yet
        given().basePath("/files").get()
            .then()
            .statusCode(200)
            .body("size()", is(0));

        // Trigger Delivery of the report
        new ReportTemplateTab(getDriver()).open()
            .select(EarlyMorningReport.name)
            .deliverReport(new DeliveryOptions()
                .format(Formats.PDF)
                .postToEndpoint("http://opennms-dummy-http-endpoint:8080/files?instanceId=:instanceId")
            );

        // Ensure it was posted
        await().atMost(2, MINUTES).pollInterval(10, SECONDS).until(
            () -> {
                final String response = given().basePath("/files").get()
                        .then()
                        .statusCode(200)
                        .extract().response().asString();
                final JSONArray array = new JSONArray(response);
                if (array.length() == 1) {
                    // Read PDF
                    final String filename = array.getJSONObject(0).getString("name");
                    final InputStream input = given().basePath("/files/" + filename).get()
                        .then()
                        .statusCode(200)
                        .contentType("application/pdf")
                        .extract().body().asInputStream();
                    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ByteStreams.copy(input, outputStream);;
                    final byte[] receivedBytes = outputStream.toByteArray();
                    if (receivedBytes.length > 0) {
                        return true;
                    }
                }
                return false;
            });
    }

    public interface EarlyMorningReport {
        String name = "Early morning report";
        String id = "local_Early-Morning-Report";

        static String filename(String format) {
            return id + "." + format.toLowerCase();
        }
    }

    public interface Formats {
        String PDF = "PDF";
        String CSV = "CSV";
    }

    public static class DatabaseReportPage extends Page {
        private final String url;

        public DatabaseReportPage(WebDriver driver, String baseUrl) {
            super(driver);
            this.url = Objects.requireNonNull(baseUrl) + "opennms/report/database/index.jsp";
        }

        public DatabaseReportPage open() {
            LOG.debug("Opening page '{}'", url);
            driver.get(url);
            pageContainsText("Early morning report");
            return this;
        }
    }

    public static class ReportTemplateTab extends Element {

        public ReportTemplateTab(WebDriver driver) {
            super(driver);
        }

        public ReportTemplateTab select(String reportName) {
            LOG.debug("Selecting report '{}' from list", reportName);
            final WebElement element = findElementByXpath(String.format("//a/h5[text() = '%s']", reportName));
            element.click();
            await().atMost(2, MINUTES).pollInterval(5, SECONDS).until(() -> execute(() -> getDriver().findElements(By.id("loading-bar-spinner")).isEmpty()));
            await().atMost(15, SECONDS).pollInterval(2, SECONDS).until(() -> findElementById("execute") != null);
            return this;
        }

        public ReportTemplateTab format(String format) {
            ensureReportIsSelected();
            new Select(driver, "reportFormat").setValueByText(format);
            return this;
        }

        public ReportTemplateTab open() {
            LOG.debug("Open Report Template Tab");
            getElement().click();
            assertThat(isActive(), Matchers.is(true));
            return this;
        }

        public WebElement getElement() {
            return execute(() -> findElementByXpath("//a[@data-name='report-templates']"));
        }

        public boolean isActive() {
            return getElement().getAttribute("class").contains("active");
        }

        public ReportTemplateTab createReport() {
            ensureReportIsSelected();
            final WebElement executeButton = execute(() -> findElementById("execute"));
            assertThat(executeButton.getText(), Matchers.is("Create Report"));
            executeButton.click();
            return this;
        }

        public ReportTemplateTab deliverReport(final DeliveryOptions options) {
            ensureReportIsSelected();

            new ReportDetailsForm(getDriver()).applyDeliveryOptions(options);

            // Finally deliver the report
            final WebElement executeButton = execute(() -> findElementById("execute"));
            assertThat(executeButton.getText(), Matchers.is("Deliver Report"));
            executeButton.click();

            // Verify it was scheduled for delivery
            await().atMost(2, MINUTES)
                    .pollInterval(5, SECONDS)
                    .until(() -> findElementByXpath("//div[contains(@class, 'alert alert-success') and contains(text(), 'The report was scheduled for delivery')]") != null);
            return this;
        }

        public ReportTemplateTab scheduleReport(DeliveryOptions options, String cronExpression) {
            LOG.debug("Try scheduling report with delivery options {} and cron expression", options, cronExpression);
            ensureReportIsSelected();
            new ReportDetailsForm(getDriver())
                    .applyDeliveryOptions(options)
                    .applyCronExpression(cronExpression);
            final WebElement executeButton = execute(() -> findElementById("execute"));
            assertThat(executeButton.getText(), Matchers.is("Schedule Report"));
            executeButton.click();

            // Verify it was scheduled
            await().atMost(2, MINUTES)
                    .pollInterval(5, SECONDS)
                    .until(() -> findElementByXpath("//div[contains(@class, 'alert alert-success') and contains(text(), 'The report was scheduled')]") != null);
            LOG.debug("Report scheduled!");
            return this;
        }

        private void ensureReportIsSelected() {
            if (getSelectedTemplate() == null) {
                throw new IllegalStateException("No report has been selected.");
            }
        }

        private String getSelectedTemplate() {
            final List<WebElement> elements = getDriver().findElements(By.xpath("//div[contains(@class, 'list-group')]//a[contains(@class, 'list-group-item') and contains(@class, 'active')]/h5"));
            if (!elements.isEmpty()) {
                return elements.get(0).getText();
            }
            return null;
        }
    }

    private static class ReportDetailsForm extends Element {

        private boolean editMode;

        public ReportDetailsForm(WebDriver driver) {
            super(driver);
        }

        public ReportDetailsForm editMode(boolean value) {
            this.editMode = value;
            return this;
        }

        public ReportDetailsForm applyDeliveryOptions(DeliveryOptions options) {
            Objects.requireNonNull(options);
            LOG.debug("Apply delivery options {}", options);

            // Enable delivery
            if (!this.editMode) {
                final CheckBox deliverCheckbox = new CheckBox(getDriver(), "deliverReport");
                deliverCheckbox.setSelected(true);
            }

            // Fill values
            // Persist to Disk?
            final CheckBox persistCheckbox = new CheckBox(getDriver(), "persistToggle");
            persistCheckbox.setSelected(options.persistToDisk);

            // Send Mail?
            final CheckBox recipientCheckbox = new CheckBox(getDriver(), "sendMailToggle");
            recipientCheckbox.setSelected(!options.emailRecipients.isEmpty());
            if (!options.emailRecipients.isEmpty()) {
                final String emailRecipients = String.join(",", options.emailRecipients);
                new TextInput(getDriver(), "mailRecipient").setInput(emailRecipients);
            }

            // Post to HTTP Endpoint?
            if (!Strings.isNullOrEmpty(options.webhookEndpoint)) {
                new CheckBox(getDriver(), "webhookToggle").setSelected(true);
                new TextInput(getDriver(), "webhookUrl").setInput(options.webhookEndpoint);
            }

            // Format
            new Select(driver, "format").setValueByText(options.format);
            return this;
        }

        public ReportDetailsForm applyCronExpression(String cronExpression) {
            LOG.debug("Applying cron expression '{}'", cronExpression);
            if (!this.editMode) {
                new CheckBox(driver, "createSchedule").setSelected(true);
            }
            new CheckBox(driver, "scheduleTypeCustom").setSelected(true);
            new TextInput(driver, "customCronExpressionInput").setInput(cronExpression);
            await().atMost(2, MINUTES).pollInterval(5, SECONDS).until(() -> findElementById("customCronExpressionInput").getAttribute("value").equals(cronExpression));
            return this;
        }
    }

    public static class PersistedReportsTab extends Element {

        public PersistedReportsTab(WebDriver driver) {
            super(driver);
        }

        public PersistedReportsTab open() {
            LOG.debug("Open Persisted Reports Tab");
            getElement().click();
            assertThat(isActive(), Matchers.is(true));
            return this;
        }

        public WebElement getElement() {
            return execute(() -> findElementByXpath("//a[@data-name='report-persisted']"));
        }

        public boolean isActive() {
            return getElement().getAttribute("class").contains("active");
        }

        public List<PersistedReportElement> getPersistedReports() {
            final List<PersistedReportElement> results = Lists.newArrayList();
            final List<WebElement> rows = execute(() -> driver.findElements(By.xpath("//table/tbody/tr")));
            for (WebElement eachRow : rows) {
                final List<WebElement> columns = eachRow.findElements(By.xpath("./td"));
                final PersistedReportElement element = new PersistedReportElement();
                element.setTitle(columns.get(2).getText());
                element.setReportId(columns.get(3).getText());
                element.setRunDate(columns.get(4).getText());
                results.add(element);
            }
            return results;
        }

        public void deleteAll() {
            if (!getPersistedReports().isEmpty()) {
                new DeleteAllButton(driver).click();
                await().atMost(2, MINUTES).pollInterval(5, SECONDS).until(() -> getPersistedReports().isEmpty());
            }
        }
    }

    public static class ScheduledReportsTab extends Element {
        public ScheduledReportsTab(WebDriver driver) {
            super(driver);
        }

        public ScheduledReportsTab open() {
            LOG.debug("Open Scheduled Reports Tab");
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(15))
                    .pollingEvery(Duration.ofSeconds(1))
                    .ignoring(Exception.class)
                    .until(driver -> elementToBeClickable(getElement()));
            getElement().click();
            assertThat(isActive(), Matchers.is(true));
            return this;
        }

        public WebElement getElement() {
            return execute(() -> driver.findElement(By.xpath("//a[@data-name='report-schedules']")));
        }

        public boolean isActive() {
            return getElement().getAttribute("class").contains("active");
        }

        public List<ReportScheduleElement> getScheduledReports() {
            final List<ReportScheduleElement> results = Lists.newArrayList();
            final List<WebElement> rows = execute(() -> driver.findElements(By.xpath("//table/tbody/tr")));
            for (WebElement eachRow : rows) {
                final List<WebElement> columns = eachRow.findElements(By.xpath("./td"));
                final ReportScheduleElement element = new ReportScheduleElement(getDriver());
                element.setTemplateName(columns.get(1).getText());
                element.setFormat(columns.get(2).getText());
                element.setCronExpression(columns.get(6).getText());
                element.setTriggerName(columns.get(7).getText());
                results.add(element);
            }
            return results;
        }

        public void deleteAll() {
            if (!getScheduledReports().isEmpty()) {
                new DeleteAllButton(driver).click();
                await().atMost(2, MINUTES).pollInterval(5, SECONDS).until(() -> getScheduledReports().isEmpty());
            }
        }

        public void updateSchedule(String triggerName, DeliveryOptions deliveryOptions, String cronExpression) {
            getSchedule(triggerName).edit(deliveryOptions, cronExpression);
        }

        private ReportScheduleElement getSchedule(String triggerName) {
            return getScheduledReports().stream()
                    .filter(e -> e.triggerName.equals(triggerName))
                    .findAny()
                    .orElseThrow(NoSuchElementException::new);
        }
    }

    public static class DeliveryOptions {

        private static final DeliveryOptions DEFAULTS = new DeliveryOptions()
                .format(Formats.PDF)
                .persistToDisk(true)
                .emailRecipients(Lists.newArrayList());

        private String format;
        private boolean persistToDisk;
        private List<String> emailRecipients = Lists.newArrayList();
        private String webhookEndpoint;

        private DeliveryOptions format(String format) {
            this.format = Objects.requireNonNull(format);
            return this;
        }

        private DeliveryOptions persistToDisk(boolean persistToDisk) {
            this.persistToDisk = persistToDisk;
            return this;
        }

        private DeliveryOptions emailRecipients(List<String> recipients) {
            this.emailRecipients = new ArrayList<>(recipients);
            return this;
        }

        public DeliveryOptions postToEndpoint(String webhookEndpoint) {
            this.webhookEndpoint = Objects.requireNonNull(webhookEndpoint);
            return this;
        }

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("format", format)
                    .add("persistToDisk", persistToDisk)
                    .add("emailRecipients", emailRecipients)
                    .add("webhookEndpoint", webhookEndpoint)
                    .toString();
        }
    }

    public static class PersistedReportElement {

        private String reportId;
        private String title;
        private String runDate;

        public void setTitle(String title) {
            this.title = Objects.requireNonNull(title);
        }

        public void setReportId(String reportId) {
            this.reportId = Objects.requireNonNull(reportId);
        }

        public void setRunDate(String runDate) {
            this.runDate = Objects.requireNonNull(runDate);
        }
    }

    public static class ReportScheduleElement extends Element {

        private String triggerName;
        private String templateName;
        private String format;
        private String cronExpression;

        public ReportScheduleElement(WebDriver driver) {
            super(driver);
        }

        public void setTemplateName(String templateName) {
            this.templateName = Objects.requireNonNull(templateName);
        }

        public void setFormat(String format) {
            this.format = Objects.requireNonNull(format);
        }

        public void setCronExpression(String cronExpression) {
            this.cronExpression = Objects.requireNonNull(cronExpression);
        }

        public void setTriggerName(String triggerName) {
            this.triggerName = Objects.requireNonNull(triggerName);
        }

        public void edit(DeliveryOptions deliveryOptions, String cronExpression) {
            LOG.debug("Try updating report schedule for trigger '{}' with delivery options {} and cron expression '{}'", triggerName, deliveryOptions, cronExpression);
            final WebDriverWait webDriverWait = new WebDriverWait(getDriver(), 30, 1000);
            execute(() -> findElementById("action.edit." + triggerName)).click();
            webDriverWait.until((driver) -> execute(() -> driver.findElements(By.id("loading-bar-spinner")).isEmpty())
                    && findElementById("action.update." + triggerName) != null
                    && findElementById("persistToggle").isDisplayed()
                    && findElementById("sendMailToggle").isDisplayed()
                    && org.opennms.smoketest.selenium.ExpectedConditions.pageContainsText("Edit Schedule").apply(driver));
            new ReportDetailsForm(getDriver())
                    .editMode(true)
                    .applyDeliveryOptions(deliveryOptions)
                    .applyCronExpression(cronExpression);
            execute(() -> findElementById("action.update." + triggerName)).click();
            execute(() -> webDriverWait.until(ExpectedConditions.not(org.opennms.smoketest.selenium.ExpectedConditions.pageContainsText("Edit Schedule"))));
            LOG.debug("Report schedule for trigger '{}' was updated!", triggerName);
        }
    }

}
