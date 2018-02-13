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
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.flows.rest.classification.ClassificationRequestDTO;
import org.opennms.netmgt.flows.rest.classification.RuleDTO;
import org.opennms.netmgt.flows.rest.classification.RuleDTOBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class ClassificationRulePageIT extends OpenNMSSeleniumTestCase {

    private interface Tabs {
        String SETTINGS = "settings";
        String USER_DEFINED = "user-defined";
        String PRE_DEFINED = "pre-defined";
    }

    private ArrayList<Tab> expectedTabs;

    private Page uiPage;

    @Before
    public void setUp() {
        uiPage = new Page(getBaseUrl());
        expectedTabs = Lists.newArrayList(
                new Tab(uiPage, Tabs.SETTINGS, "Settings", 0, false),
                new Tab(uiPage, Tabs.USER_DEFINED, "User-defined Rules", 0, true),
                new Tab(uiPage, Tabs.PRE_DEFINED, "Pre-defined Rules", 6248, false)
        );
        uiPage.open();
    }

    @Test
    public void verifyTabs() {
        // Verify expectation
        for (Tab expectedTab : expectedTabs) {
            Tab actualTab = uiPage.getTab(expectedTab.getName());
            assertThat(expectedTab.getCount(), is(actualTab.getCount()));
            assertThat(expectedTab.getName(), is(actualTab.getName()));
            assertThat(expectedTab.getLabel(), is(actualTab.getLabel()));
            assertThat(expectedTab.isActive(), is(actualTab.isActive()));
        }

        assertThat(uiPage.getActiveTab().getName(), is(Tabs.USER_DEFINED));

        // Ensure clicking works as well
        for (Tab tab : expectedTabs) {
            uiPage.getTab(tab.getName()).click();
        }
    }

    @Test
    public void verifySettingsTab() {
        // Load tab
        final SettingsTab settings = new SettingsTab(uiPage).click();

        // Verify 2 groups are shown (atm only 2 are supported)
        assertThat(settings.getGroups(), hasSize(2));

        // Verify user-defined group
        final Group userDefinedGroup = settings.getGroup(Tabs.USER_DEFINED);
        assertThat(userDefinedGroup.isEditable(), is(true));
        assertThat(userDefinedGroup.isEnabled(), is(true));

        // Verify pre-defined group
        final Group preDefinedGroup = settings.getGroup(Tabs.PRE_DEFINED);
        assertThat(preDefinedGroup.isEditable(), is(false));
        assertThat(preDefinedGroup.isEnabled(), is(true));

        // Verify disable groups
        userDefinedGroup.setEnabled(false);
        preDefinedGroup.setEnabled(false);
        assertThat(userDefinedGroup.isEnabled(), is(false));
        assertThat(preDefinedGroup.isEnabled(), is(false));
        assertThat(uiPage.getTabs(), hasSize(1));

        // Verify enable groups
        userDefinedGroup.setEnabled(true);
        preDefinedGroup.setEnabled(true);
        assertThat(userDefinedGroup.isEnabled(), is(true));
        assertThat(preDefinedGroup.isEnabled(), is(true));

        // Verify refresh
        settings.refresh();
        assertThat(settings.getGroups(), hasSize(2));
        assertThat(uiPage.getTabs(), hasSize(expectedTabs.size()));
    }

    @Test
    public void verifyRuleCRUD() {
        final GroupTab groupTab = new GroupTab(this.uiPage, Tabs.USER_DEFINED).click();
        assertThat(groupTab.isEmpty(), is(true));

        // Create dummy group
        groupTab.addNewRule(new RuleDTOBuilder()
                .withName("http")
                .withPort("80,8080")
                .withProtocol("udp,tcp").build());
        assertThat(groupTab.isEmpty(), is(false));

        // Verify that one group has been created
        final List<RuleData> rules = groupTab.getRules();
        assertThat(rules, hasSize(1));
        final RuleData rule = rules.get(0);
        assertThat(rule.getName(), is("http"));
        assertThat(rule.getPort(), is("80,8080"));
        assertThat(rule.getProtocol(), is("TCP,UDP"));
        assertThat(rule.getIpAddress(), is(""));

        // Edit rule
        groupTab.editRule(rule.getPosition(),
                createFrom(rule)
                        .withIpAddress("127.0.0.1")
                        .withName("OpenNMS")
                        .withPort("8980")
                        .withProtocol("tcp").build());

        // Edit, but cancel
        groupTab.editModal(0).setInput(createFrom(rule).build()).cancel();

        // Verify edit made it through, but cancel did not
        final RuleData modifiedRule = groupTab.getRuleData(0);
        assertThat(modifiedRule.getName(), is("OpenNMS"));
        assertThat(modifiedRule.getPort(), is("8980"));
        assertThat(modifiedRule.getIpAddress(), is("127.0.0.1"));
        assertThat(modifiedRule.getProtocol(), is("TCP"));

        // Delete rule
        groupTab.deleteGroup(rule.getPosition());
        assertThat(groupTab.getRules(), hasSize(0));
    }

    @Test
    public void verifyRulePaginationAndDeleteAll() {
        // Navigate to group
        final GroupTab groupTab = new GroupTab(this.uiPage, Tabs.USER_DEFINED).click();
        assertThat(groupTab.isEmpty(), is(true));
        assertThat(groupTab.isEditable(), is(true));

        // Insert dummy rules
        final int NUMBER_OF_RULES = 42;
        for (int i=0; i<NUMBER_OF_RULES; i++) {
            final RuleDTO rule = new RuleDTOBuilder()
                    .withName("http" + i)
                    .withPort(Integer.toString(i))
                    .withProtocol("tcp,udp").build();
            groupTab.addNewRule(rule);
        }

        // Iterate through pages
        final int ITEMS_PER_PAGE = 20; //defined in ui
        final int NUMBER_OF_PAGES = NUMBER_OF_RULES / ITEMS_PER_PAGE;
        final int ITEMS_LAST_PAGE = NUMBER_OF_RULES - (ITEMS_PER_PAGE * NUMBER_OF_PAGES);
        for (int i=0; i<=NUMBER_OF_PAGES; i++) {
            int position = i * ITEMS_PER_PAGE;
            groupTab.navigateToPage(i+1);

            // Only verify position of first element on page
            final RuleData ruleData = groupTab.getRuleData(0);
            assertThat(position, is(ruleData.getPosition()));

            // each page should have ITEMS_PER_PAGE items, last page should have ITEMS_LAST_PAGE
            assertThat(groupTab.getRules(), hasSize(i < NUMBER_OF_PAGES ? ITEMS_PER_PAGE : ITEMS_LAST_PAGE));
        }

        // Delete all afterwards
        groupTab.deleteAll();
        assertThat(groupTab.isEmpty(), is(true));
        assertThat(groupTab.getRules(), hasSize(0));
    }

    @Test
    public void verifySearch() {
        final GroupTab tab = new GroupTab(uiPage, Tabs.PRE_DEFINED).click();
        tab.search("icmpd");

        final List<RuleData> rules = tab.getRules();
        assertThat(rules, hasSize(1));

        final RuleData rule = rules.get(0);
        assertThat(6170, is(rule.getPosition()));
        assertThat("icmpd", is(rule.getName()));
        assertThat("5813", is(rule.getPort()));
        assertThat("tcp,udp", is(rule.getProtocol()));
        assertThat("", is(rule.getIpAddress()));

        tab.search(""); // clear Search afterwards
    }

    @Test
    public void verifyClassification() {
        final ClassificationRequestDTO classificationRequestDTO = new ClassificationRequestDTO();
        classificationRequestDTO.setIpAddress("127.0.0.1");
        classificationRequestDTO.setPort("80");
        classificationRequestDTO.setProtocol("tcp");

        // try http
        assertThat("http", is(uiPage.classify(classificationRequestDTO)));

        // try http-alt
        classificationRequestDTO.setPort("8080");
        assertThat("http-alt", is(uiPage.classify(classificationRequestDTO)));

        // try no mapping found
        classificationRequestDTO.setPort("12");
        assertThat("No mapping found", is(uiPage.classify(classificationRequestDTO)));
    }

    @Test
    public void verifyReadOnlyGroup() {
        // Navigate to pre-defined rules, which are readonly by default
        final GroupTab groupTab = new GroupTab(uiPage, Tabs.PRE_DEFINED).click();

        // verify that it is actually read only and has at least 1 rule defined
        assertThat(groupTab.isEditable(), is(false));
        assertThat(groupTab.isEmpty(), is(false));

        // iterate over existing rules, but not more than one and verify it is actually not editable
        int ruleCount = groupTab.getRules().size();
        for (int i=0; i < Math.min(5, ruleCount); i++) {
            final int index = i;
            assertThat(execute(() -> m_driver.findElement(By.id("action." + Integer.toString(index) + ".delete"))).isDisplayed(), is(false));
            assertThat(execute(() -> m_driver.findElement(By.id("action." + Integer.toString(index) + ".edit"))).isDisplayed(), is(false));
        }
    }

    private class Page {
        private final String url;

        public Page(String baseUrl) {
            this.url = Objects.requireNonNull(baseUrl) + "opennms/admin/classification/index.jsp";
        }

        public Page open() {
            m_driver.get(url);
            new WebDriverWait(m_driver, 5).until((Predicate<WebDriver>) (driver) -> getTabs().size() == expectedTabs.size());
            return this;
        }

        public Tab getActiveTab() {
            return getTabs().stream()
                    .filter(tab -> tab.isActive())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No tab active. This should not be possible"));
        }

        public Tab getTab(final String name) {
            return getTabs().stream()
                    .filter(tab -> name.equalsIgnoreCase(tab.getName()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Cannot find tab with name " + name));
        }

        public List<Tab> getTabs() {
            return execute(() -> {
                final List<WebElement> tabElements = m_driver.findElements(By.xpath("//ul[@id='tabs']//li/a[@data-name]"));
                return tabElements.stream().map(eachTab -> {
                    final String name = eachTab.getAttribute("data-name");
                    final List<WebElement> spanElements = m_driver.findElements(By.xpath("//ul[@id='tabs']//li/a[@data-name='" + name + "']/span"));
                    final int count = spanElements.isEmpty() ? 0 : Integer.parseInt(spanElements.get(0).getText());
                    final String label = spanElements.isEmpty() ? eachTab.getText() : eachTab.getText().replace(spanElements.get(0).getText(), "");
                    boolean isSelected = eachTab.findElement(By.xpath("..")).getAttribute("class").contains("active");
                    return new Tab(this, name.trim(), label.trim(), count, isSelected);
                }).collect(Collectors.toList());
            });
        }

        public String classify(ClassificationRequestDTO classificationRequestDTO) {
            // get the classification input
            if (!execute(() -> findElementById("classification-tab")).isDisplayed()) {
                execute(() -> findElementById("action.classification.toggle")).click();
            }

            // Input fields
            setInput("classify-ipAddress", classificationRequestDTO.getIpAddress());
            setInput("classify-port", classificationRequestDTO.getPort());
            setInput("classify-protocol", classificationRequestDTO.getProtocol(), true);

            // Submit form
            execute(() -> findElementById("classification-submit")).click();

            // Fiddle result out of UI
            return execute(() -> findElementById("classification-response")).getText();
        }

        private void setInput(String id, String text, boolean withEnter) {
            final WebElement element = execute(() -> findElementById(id));
            element.clear();
            element.sendKeys(text);
            if (withEnter) {
                element.sendKeys(Keys.ENTER);
            }
        }

        private void setInput(String id, String text) {
            setInput(id, text, false);
        }
    }

    private class Tab {

        private final Page page;
        private final String label;
        private final int count;
        private final boolean selected;
        private final String name;

        public Tab(Page page, String name, String label, int count, boolean isSelected) {
            this.page = Objects.requireNonNull(page);
            this.label = Objects.requireNonNull(label);
            this.name = Objects.requireNonNull(name);
            this.count = count;
            this.selected = isSelected;
        }

        public void click() {
            getElement().click();
            new WebDriverWait(m_driver, 10).until((ExpectedCondition<Boolean>) input -> page.getTab(name).isActive());
        }

        public WebElement getElement() {
            return execute(() -> m_driver.findElement(By.xpath("//ul[@id='tabs']//li/a[@data-name='" + name + "']")));
        }

        public boolean isActive() {
            return selected;
        }

        public int getCount() {
            return count;
        }

        public String getLabel() {
            return label;
        }

        public String getName() {
            return name;
        }
    }

    private class SettingsTab {
        private final Page page;

        public SettingsTab(Page page) {
            this.page = Objects.requireNonNull(page);
        }

        public SettingsTab click() {
            page.getTab(Tabs.SETTINGS).click();
            return this;
        }

        public List<Group> getGroups() {
            return execute(() -> m_driver.findElements(By.xpath("//table/tbody/tr[@data-row]"))
                    .stream()
                    .map(row -> {
                        final List<WebElement> columns = row.findElements(By.xpath("./td"));
                        final String name = columns.get(1).getText();
                        final Group group = new Group(this, name);
                        return group;
                    }).collect(Collectors.toList()));
        }

        public Group getGroup(String groupName) {
            return getGroups().stream()
                    .filter(g -> g.getName().equalsIgnoreCase(groupName))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No group with name " + groupName + " found"));
        }

        public void refresh() {
            m_driver.findElement(By.id("action.refresh")).click();
            sleep(2000);
        }
    }

    private class Group {
        private final SettingsTab tab;
        private final String name;

        public Group(SettingsTab tab, String name) {
            this.tab = Objects.requireNonNull(tab);
            this.name = name;
        }

        public int getPriority() {
            return getGroupData().getPriority();
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return getGroupData().getDescription();
        }

        public void setEnabled(boolean enabled) {
            if (isEnabled() != enabled) {
                execute(() -> {
                    final WebElement toggleElement = m_driver.findElement(By.id(String.format("action.%s.toggle", name)));
                    Actions actions = new Actions(m_driver);
                    actions.moveToElement(toggleElement);
                    actions.click();
                    actions.perform();
                    new WebDriverWait(m_driver, 10)
                            .until((Predicate<WebDriver>) input -> {
                                final Group group = tab.getGroup(name);
                                return enabled == group.isEnabled();
                            });
                    return null;
                });
            }
        }

        private GroupData getGroupData() {
            return execute(() -> {
                final List<WebElement> columns = m_driver.findElements(By.xpath("//table/tbody/tr[@data-row='" + this.name + "']/td"));
                final int priority = Integer.parseInt(columns.get(0).getText());
                final String name = columns.get(1).getText();
                final String description = columns.get(2).getText();
                final boolean editable = Boolean.valueOf(columns.get(3).getText());
                final boolean enabled = columns.get(4).findElements(By.xpath(".//toggle/div[contains(@class, 'off')]")).isEmpty(); // the off class indicates the toggle is off
                return new GroupData(name, priority, description, editable, enabled);
            });
        }

        public boolean isEditable() {
            return getGroupData().isEditable();
        }

        public boolean isEnabled() {
            return getGroupData().isEnabled();
        }
    }

    private class GroupData {
        private final String name;
        private final String description;
        private final int priority;
        private final boolean editable;
        private final boolean enabled;

        private GroupData(String name, int priority, String description, boolean editable, boolean enabled) {
            this.name = Objects.requireNonNull(name);
            this.priority = priority;
            this.description = Objects.requireNonNull(description);
            this.editable = editable;
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isEditable() {
            return editable;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    private class RuleModal {

        public RuleModal open(Runnable openModal) {
            Objects.requireNonNull(openModal);
            execute(() -> {
                openModal.run();
                return null;
            });
            return this;
        }

        public RuleModal setInput(RuleDTO rule) {
            // Input form
            setInput("rule.name", rule.getName());
            setInput("rule.ipAddress", rule.getIpAddress());
            setInput("rule.port", rule.getPort());

            // remove all protocols
            execute(() -> {
                m_driver.findElements(By.xpath("//span/a[@name='remove-protocol']")).forEach(element -> element.click());
                return null;
            });

            // Manually add protocols afterwards
            if (!rule.getProtocols().isEmpty()) {
                for (String eachProtocol : rule.getProtocols()) {
                    execute(() -> {
                        final WebElement protocolInput = findElementById("rule.protocol");
                        protocolInput.clear();
                        protocolInput.sendKeys(eachProtocol);
                        protocolInput.sendKeys(Keys.RETURN);
                        return null;
                    });
                }
            }
            return this;
        }

        // Save or update
        public void save() {
            execute(() -> {findElementById("save-rule").click(); return null; });
            ensureClosed();
        }

        // Close dialog
        public void cancel() {
            execute(() -> { findElementById("cancel-rule").click(); return null; });
            ensureClosed();
        }

        // Ensure dialog closes
        private void ensureClosed() {
            execute(() -> new WebDriverWait(m_driver, 5).until(ExpectedConditions.numberOfElementsToBe(By.id("ruleModal"), 0)));
        }

        private void setInput(String id, String input) {
            Objects.requireNonNull(id);
            if (input != null) {
                execute(() -> {
                    final WebElement webElement = findElementById(id);
                    webElement.clear();
                    webElement.sendKeys(input);
                    return null;
                });
            }
        }

    }

    private class GroupTab {
        private final Page page;

        private final String groupName;

        public GroupTab(Page page, String groupName) {
            this.page = Objects.requireNonNull(page);
            this.groupName = groupName;
        }

        public GroupTab click() {
            page.getTab(groupName).click();
            return this;
        }

        public boolean isEditable() {
            return execute(() -> m_driver.findElement(By.id("action.addRule"))).isDisplayed();
        }

        public void addNewRule(RuleDTO rule) {
           newModal()
                .setInput(rule)
                .save();
        }

        public RuleModal newModal() {
            return new RuleModal()
                    .open(() -> {
                        // Click add rule button
                        findElementById("action.addRule").click();
                        new WebDriverWait(m_driver, 5).until(pageContainsText("Create Classification Rule"));
                    });
        }

        public RuleModal editModal(int position) {
            return new RuleModal()
                    .open(() -> {
                        // click edit button
                        findElementById("action." + position + ".edit").click();
                        new WebDriverWait(m_driver, 5).until(pageContainsText("Edit Classification Rule"));
                    });
        }

        public void editRule(int position, RuleDTO newValues) {
            editModal(position)
                .setInput(newValues)
                .save();
        }

        public boolean isEmpty() {
            return execute(() -> findElementByXpath("//div//div/pre[contains(text(), 'No rules defined.')]").isDisplayed(), 5);
        }

        public List<RuleData> getRules() {
            return execute(() -> {
                    final List<WebElement> rows = m_driver.findElements(By.xpath("//div//table/tbody/tr"));
                    return IntStream.range(0, rows.size()).mapToObj(index -> getRuleData(index)).collect(Collectors.toList());
            });
        }

        public RuleData getRuleData(int index) {
            return execute(() -> {
                final WebElement ruleElement = findElementByXpath("(//div//table/tbody/tr)[" + (index + 1) + "]");
                final List<WebElement> columns = ruleElement.findElements(By.xpath("./td"));
                final int position = Integer.parseInt(columns.get(0).getText());
                final String name = columns.get(1).getText();
                final String ipAddress = columns.get(2).getText();
                final String port = columns.get(3).getText();
                final String protocol = columns.get(4).findElements(By.xpath("./span")).stream().map(webElement -> webElement.getText()).collect(Collectors.joining(","));
                return new RuleData(position, name, ipAddress, port, protocol);
            });
        }

        public void deleteGroup(int position) {
            execute(() -> {
                final String deleteActionId = "action." + position + ".delete";
                findElementById(deleteActionId).click();
                new WebDriverWait(m_driver, 5).until(ExpectedConditions.numberOfElementsToBe(By.id(deleteActionId), 0));
                return null;
            });
        }

        public void navigateToPage(int page) {
            execute(() -> findElementByXpath("//li[contains(@class, 'pagination-page')]/a[contains(text(), '" + page + "')]")).click();
            execute(() -> new WebDriverWait(m_driver, 5).until(
                            ExpectedConditions.visibilityOf(
                                    findElementByXpath("//li[contains(@class, 'active')]/a[contains(text(), '" + page + "')]"))));
        }

        public void deleteAll() {
            execute(() -> findElementById("action.deleteAll")).click();
            execute(() -> findElementByXpath("//div[contains(@class,'popover')]//button[contains(text(), 'Yes')]")).click();
            sleep(5000);
        }

        public void search(String search) {
            final WebElement searchElement = execute(() -> findElementById("action.search"));
            searchElement.clear();
            searchElement.sendKeys(search);
            sleep(5000);
        }
    }

    private class RuleData {
        private final int position;
        private final String name;
        private final String ipAddress;
        private final String port;
        private final String protocol;

        public RuleData(int position, String name, String ipAddress, String port, String protocol) {
            this.position = position;
            this.name = Objects.requireNonNull(name);
            this.ipAddress = Objects.requireNonNull(ipAddress);
            this.port = Objects.requireNonNull(port);
            this.protocol = Objects.requireNonNull(protocol);
        }

        public int getPosition() {
            return position;
        }

        public String getName() {
            return name;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getPort() {
            return port;
        }

        public String getProtocol() {
            return protocol;
        }
    }

    private <X> X execute(Supplier<X> supplier) {
       return execute(supplier, 1);
    }

    private <X> X execute(Supplier<X> supplier, int implicitWaitInSeconds) {
        try {
            this.setImplicitWait(implicitWaitInSeconds, TimeUnit.SECONDS);
            return supplier.get();
        } finally {
            this.setImplicitWait();
        }
    }

    private static RuleDTOBuilder createFrom(RuleData rule) {
        return new RuleDTOBuilder()
                .withName(rule.getName())
                .withPort(rule.getPort())
                .withProtocol(rule.getProtocol())
                .withIpAddress(rule.getIpAddress());
    }
}
