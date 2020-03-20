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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.smoketest.ui.framework.TextInput;
import org.opennms.smoketest.ui.framework.Toggle;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

@Ignore("Flapping. See NMS-12114")
public class GeocoderServiceConfigurationPageIT extends UiPageTest {

    private static final Logger LOG = LoggerFactory.getLogger(GeocoderServiceConfigurationPageIT.class);

    private static class Geocoder {
        private final String id;
        private final String label;

        private Geocoder(String id, String label) {
            this.id = Objects.requireNonNull(id);
            this.label = Objects.requireNonNull(label);
        }
    }

    private interface Geocoders {
        Geocoder GOOGLE = new Geocoder("google", "Google");
        Geocoder MAPQUEST = new Geocoder("mapquest", "Mapquest");
        Geocoder NOMINATIM = new Geocoder("nominatim","Nominatim");
    }

    private ArrayList<TabData> expectedTabs;

    private Page uiPage;

    @Before
    public void setUp() {
        resetConfiguration();
        uiPage = new Page(getBaseUrlInternal());
        expectedTabs = Lists.newArrayList(
                new TabData("settings", "Settings", true),
                new TabData(Geocoders.GOOGLE.id, Geocoders.GOOGLE.label, false),
                new TabData(Geocoders.MAPQUEST.id, Geocoders.MAPQUEST.label, false),
                new TabData(Geocoders.NOMINATIM.id, Geocoders.NOMINATIM.label, false)
        );
        uiPage.open();
    }

    @After
    public void tearDown() {
        try {
            resetConfiguration();
        } catch (Exception e) {
            LOG.warn("Resetting configuration failed in tear down.", e);
        }
    }

    private void resetConfiguration() {
        stack.opennms().getRestClient().resetGeocoderConfiguration();
    }

    @Test
    public void verifyTabs() {
        // Verify expectation
        for (TabData expectedTab : expectedTabs) {
            Tab actualTab = uiPage.getTab(expectedTab.name);
            assertThat(expectedTab.name, is(actualTab.getName()));
            assertThat(expectedTab.label, is(actualTab.getLabel()));
            assertThat(expectedTab.active, is(actualTab.isActive()));
        }

        assertThat(uiPage.getActiveTab().getName(), is("settings"));

        // Ensure clicking works as well
        for (TabData tab : expectedTabs) {
            uiPage.getTab(tab.name).click();
        }
    }

    @Test
    public void verifyEnableGeocoder() {
        // Ensure disabled
        final SettingsTab settingsTab = new SettingsTab(uiPage);
        settingsTab.click();
        assertNull(settingsTab.getActiveGeocoder());

        // Activate Nominatim
        settingsTab.setActiveGeocoder(Geocoders.NOMINATIM.id);
        assertThat(settingsTab.getActiveGeocoder(), is(Geocoders.NOMINATIM.id));

        // Activate Google
        settingsTab.setActiveGeocoder(Geocoders.GOOGLE.id);
        assertThat(settingsTab.getActiveGeocoder(), is(Geocoders.GOOGLE.id));

        // Activate Mapquest
        settingsTab.setActiveGeocoder(Geocoders.MAPQUEST.id);
        assertThat(settingsTab.getActiveGeocoder(), is(Geocoders.MAPQUEST.id));
    }

    @Test
    public void verifyNominatimGeocoder() {
        final SettingsTab settingsTab = new SettingsTab(uiPage);
        settingsTab.click();
        settingsTab.setActiveGeocoder(Geocoders.NOMINATIM.id);

        // Fully configure
        final NominatimTab nominatimTab = new NominatimTab(uiPage);
        assertThat(nominatimTab.isConfiguredProperly(), is(false));
        nominatimTab.click();
        assertThat(nominatimTab.isDirty(), is(false));
        nominatimTab.setAcceptUsageTerms(true);
        nominatimTab.setEmail("ulf@opennms.org");
        assertThat(nominatimTab.isDirty(), is(true));
        nominatimTab.update();
        assertThat(nominatimTab.isDirty(), is(false));
        assertThat(nominatimTab.isConfiguredProperly(), is(true));

        // Ensure Referer or User Agent are set
        nominatimTab.setUserAgent(null);
        nominatimTab.setReferer(null);
        assertThat(nominatimTab.getSaveButtonElement().isEnabled(), is(false));

        // Only Referer should work.
        nominatimTab.setReferer("Dummy");
        assertThat(nominatimTab.getSaveButtonElement().isEnabled(), is(true));
        nominatimTab.update();
        assertThat(nominatimTab.isConfiguredProperly(), is(true));

        // we skip user agent, as that is set by default and was already verified above
    }

    @Test
    public void verifyMapquestGeocoder() {
        final SettingsTab settingsTab = new SettingsTab(uiPage);
        settingsTab.click();
        settingsTab.setActiveGeocoder(Geocoders.MAPQUEST.id);

        // Fully configure
        final MapquestTab mapquestTab = new MapquestTab(uiPage);
        assertThat(mapquestTab.isConfiguredProperly(), is(false));
        mapquestTab.click();
        assertThat(mapquestTab.isDirty(), is(false));
        mapquestTab.setApiKey("ABC");
        assertThat(mapquestTab.isDirty(), is(true));
        mapquestTab.update();
        assertThat(mapquestTab.isDirty(), is(false));
        assertThat(mapquestTab.isConfiguredProperly(), is(true));
    }

    @Test
    public void verifyGoogleGeocoder() {
        final SettingsTab settingsTab = new SettingsTab(uiPage);
        settingsTab.click();
        settingsTab.setActiveGeocoder(Geocoders.GOOGLE.id);

        // Fully configure
        final GoogleTab googleTab = new GoogleTab(uiPage);
        assertThat(googleTab.isConfiguredProperly(), is(false));
        googleTab.click();
        assertThat(googleTab.isDirty(), is(false));
        googleTab.setApiKey("ABC");
        googleTab.update();
        assertThat(googleTab.isDirty(), is(false));
        assertThat(googleTab.isConfiguredProperly(), is(true));

        // Try Legacy configuration
        googleTab.setAuthMode(GoogleAuthMode.LEGACY);
        assertThat(googleTab.isDirty(), is(true));
        assertThat(googleTab.getSaveButtonElement().isEnabled(), is(false));
        googleTab.setClientId("clientId");
        assertThat(googleTab.getSaveButtonElement().isEnabled(), is(false));
        googleTab.setSignature("signature");
        assertThat(googleTab.getSaveButtonElement().isEnabled(), is(true));
        googleTab.update();
        assertThat(googleTab.isDirty(), is(false));
        assertThat(googleTab.isConfiguredProperly(), is(true));
    }

    private class Page {
        private final String url;

        public Page(String baseUrl) {
            this.url = Objects.requireNonNull(baseUrl) + "opennms/admin/geoservice/index.jsp";
        }

        public Page open() {
            driver.get(url);
            new WebDriverWait(driver, 5).until(driver -> getTabs().size() == expectedTabs.size());
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
                final List<WebElement> tabElements = driver.findElements(By.xpath("//ul[@id='tabs']//li/a[@data-name]"));
                return tabElements.stream().map(eachTab -> {
                    final String name = eachTab.getAttribute("data-name");
                    final List<WebElement> spanElements = driver.findElements(By.xpath("//ul[@id='tabs']//li/a[@data-name='" + name + "']/span"));
                    final String label = spanElements.isEmpty() ? eachTab.getText() : eachTab.getText().replace(spanElements.get(0).getText(), "");
                    return new Tab(this, name.trim(), label.trim());
                }).collect(Collectors.toList());
            });
        }
    }

    private class TabData {
        private String name;
        private String label;
        private boolean active;

        public TabData(String name, String label, boolean active) {
            this.name = Objects.requireNonNull(name);
            this.label = Objects.requireNonNull(label);
            this.active = active;
        }
    }

    private class Tab {

        protected static final String TAB_XPATH = "//ul[@id='tabs']//li/a[@data-name='%s']";

        private final Page page;
        private final String label;
        private final String name;

        public Tab(Page page, String name, String label) {
            this.page = Objects.requireNonNull(page);
            this.label = Objects.requireNonNull(label);
            this.name = Objects.requireNonNull(name);
        }

        public void click() {
            getElement().click();
            sleep(2000);
            new WebDriverWait(driver, 5).until((ExpectedCondition<Boolean>) input -> page.getTab(name).isActive());
        }

        public WebElement getElement() {
            return execute(() -> driver.findElement(By.xpath(String.format(TAB_XPATH, name))));
        }

        public boolean isActive() {
            return getElement().getAttribute("class").contains("active");
        }

        public String getLabel() {
            return label;
        }

        public String getName() {
            return name;
        }
    }

    private class SettingsTab extends Tab {

        public SettingsTab(Page page) {
            super(page, "settings", "Settings");
        }

        public void setActiveGeocoder(String newGeocoder) {
            final String activeGeocoder = getActiveGeocoder();

            // Click if value has changed or should be disabled
            if (newGeocoder == null && activeGeocoder != null) {
                new Toggle(getDriver(), getToggleId(activeGeocoder)).toggle();
            } else if (!Objects.equals(newGeocoder, activeGeocoder)){
                new Toggle(getDriver(), getToggleId(newGeocoder)).toggle();
            }
        }

        private String getToggleId(String geocoder) {
            return String.format("%s-toggle", geocoder);
        }

        public String getActiveGeocoder() {
            return execute(() -> {
                final List<WebElement> elements = driver.findElements(By.xpath("//div[contains(@class, 'toggle') and not(contains(@class,'off')) and not (contains(@class, 'toggle-group'))]/./.."));
                if (elements.isEmpty()) {
                    return null;
                }
                final String id = elements.get(0).getAttribute("id");
                return id.replace("-toggle", "");
            });
        }
    }

    private abstract class GeocoderTab extends Tab {

        public GeocoderTab(Page page, Geocoder geocoder) {
            super(page, geocoder.id, geocoder.label);
        }

        public boolean isConfiguredProperly() {
            return execute(() -> driver.findElements(By.xpath(String.format(TAB_XPATH, getName()) + "//i[@class='fa fa-exclamation-triangle']")).isEmpty());
        }

        public boolean isDirty() {
            boolean hasChanges = execute(() -> driver.findElements(By.xpath("//p[text() = 'You have unsaved changes' and contains(@class, 'text-warning')]"))).isEmpty() == false;
            return hasChanges;
        }

        public void update() {
           getSaveButtonElement().click();
           new WebDriverWait(driver, 5, 500).until(webDriver -> !isDirty());
        }

        WebElement getSaveButtonElement() {
            return execute(() -> driver.findElement(By.id("saveButton")));
        }
    }

    private class NominatimTab extends GeocoderTab {

        public NominatimTab(Page page) {
            super(page, Geocoders.NOMINATIM);
        }

        public void setAcceptUsageTerms(boolean acceptUsageTerms) {
            final Toggle acceptUsageTermsToggle = new Toggle(getDriver(), "nominatimAcceptUsageTerms");
            acceptUsageTermsToggle.setValue(acceptUsageTerms);
        }

        public void setEmail(String email) {
            new TextInput(getDriver(), "nominatimEmail").setInput(email);
        }

        public void setReferer(String referer) {
            new TextInput(getDriver(), "nominatimReferer").setInput(referer);
        }

        public void setUserAgent(String userAgent) {
            new TextInput(getDriver(), "nominatimUserAgent").setInput(userAgent);
        }
    }

    private class MapquestTab extends GeocoderTab {

        public MapquestTab(Page page) {
            super(page, Geocoders.MAPQUEST);
        }

        public void setApiKey(final String apiKey) {
            new TextInput(getDriver(), "mapquestApiKey").setInput(apiKey);
        }
    }

    private enum GoogleAuthMode {
        API_KEY,
        LEGACY
    }

    private class GoogleTab extends GeocoderTab {

        public GoogleTab(Page page) {
            super(page, Geocoders.GOOGLE);
        }

        public void setApiKey(final String apiKey) {
            setAuthMode(GoogleAuthMode.API_KEY);
            new TextInput(getDriver(), "googleApiKey").setInput(apiKey);
        }

        public void setClientId(final String clientId) {
            setAuthMode(GoogleAuthMode.LEGACY);
            new TextInput(getDriver(), "googleClientId").setInput(clientId);
        }

        public void setSignature(final String signature) {
            setAuthMode(GoogleAuthMode.LEGACY);
            new TextInput(getDriver(), "googleClientKey").setInput(signature);
        }

        public GoogleAuthMode getAuthMode() {
            if (execute(() -> driver.findElement(By.id("googleClientIdAuthentication"))).isSelected()) {
                return GoogleAuthMode.LEGACY;
            }
            return GoogleAuthMode.API_KEY;
        }

        public void setAuthMode(GoogleAuthMode authMode) {
            if (authMode != getAuthMode()) {
                execute(() -> driver.findElement(By.id("googleClientIdAuthentication"))).click();
            }
        }
    }
}
