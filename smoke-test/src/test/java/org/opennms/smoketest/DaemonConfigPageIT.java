/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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
import static org.hamcrest.core.Is.is;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class DaemonConfigPageIT extends OpenNMSSeleniumTestCase {

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.MINUTES);

    @Before
    public void setUp() {
        // Reduces the XPath Find Element Waiting time
        setImplicitWait(5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        // Resets the FindElement Waiting Time
        setImplicitWait();
    }

    @Test
    public void verifyDaemonListIsShown() {
        final DaemonReloadPage page = new DaemonReloadPage().open();
        Assert.assertThat(page.getDaemonRows(), hasSize(16)); // Expected daemon count

        // Verify existence of some reloadable and enabled daemons
        final Daemon eventd = page.getDaemon("Eventd");
        Assert.assertThat(eventd.getName(), is("Eventd"));
        Assert.assertThat(eventd.isEnabled(), is(true));
        Assert.assertThat(eventd.isReloadable(), is(true));

        final Daemon alarmd = page.getDaemon("Alarmd");
        Assert.assertThat(alarmd.getName(), is("Alarmd"));
        Assert.assertThat(alarmd.isEnabled(), is(true));
        Assert.assertThat(alarmd.isReloadable(), is(true));

        // Switch to all
        page.showAll();
        Assert.assertThat(page.getDaemonRows(), hasSize(30)); // Expected daemon count

        // Verify existence of non reloadable and/or enabled daemons
        final Daemon snmppoller = page.getDaemon("SnmpPoller");
        Assert.assertThat(snmppoller.getName(), is("SnmpPoller"));
        Assert.assertThat(snmppoller.isEnabled(), is(false));
        Assert.assertThat(snmppoller.isReloadable(), is(false));

        final Daemon ticketer = page.getDaemon("Ticketer");
        Assert.assertThat(ticketer.getName(), is("Ticketer"));
        Assert.assertThat(ticketer.isEnabled(), is(true));
        Assert.assertThat(ticketer.isReloadable(), is(false));

        // Verify reloading
        Assert.assertThat(eventd.reload(5), is(true));
        Assert.assertThat(eventd.getCurlString(), is(createExceptedCurlString(eventd.getName())));

        Assert.assertThat(alarmd.reload(5), is(true));
        Assert.assertThat(alarmd.getCurlString(), is(createExceptedCurlString(alarmd.getName())));
    }

    private String createExceptedCurlString(String name){
        return String.format("curl --request POST -u USERNAME:PASSWORD -url %sopennms/rest/daemons/reload/%s/", getBaseUrl(), name);
    }

    private class Daemon {
        private final String daemonName;

        public Daemon(String daemonName) {
            this.daemonName = daemonName;
        }

        public String getName() {
            // Assert That Right Name is shown
            WebElement nameCell = getDaemonRowElement().findElement(By.xpath("./td[1]"));
            Assert.assertThat(nameCell.isDisplayed(), is(true));
            Assert.assertThat(nameCell.isEnabled(), is(true));
            return nameCell.getText();
        }

        public boolean isEnabled() {
            final WebElement enabledIndicator = getDaemonRowElement().findElement(By.xpath("./td[2]/i"));
            Assert.assertThat(enabledIndicator.isEnabled(), is(true));
            Assert.assertThat(enabledIndicator.isDisplayed(), is(true));
            boolean enabled = enabledIndicator.getAttribute("class").contains("text-success");
            return enabled;
        }

        public boolean isReloadable() {
            try {
                WebElement reloadButton = getReloadButton();
                return reloadButton.isDisplayed() && reloadButton.isEnabled();
            } catch (NoSuchElementException ex) {
                return false;
            }
        }

        public boolean reload(long maxReloadTimeInSeconds) {
            if (!this.isReloadable()) {
                throw new IllegalStateException("This daemon is not reloadable");
            }
            WebElement reloadButton = getReloadButton();
            Assert.assertThat(reloadButton.isDisplayed(), is(true));
            Assert.assertThat(reloadButton.isEnabled(), is(true));
            reloadButton.click();

            // Check Ui State while reloading is taking place
            reloadButton = getReloadButton();
            Assert.assertThat(reloadButton.isDisplayed(), is(true));
            Assert.assertThat(reloadButton.isEnabled(), is(false));

            WebElement resultTextElement = getResultElement();
            Assert.assertThat(resultTextElement.isDisplayed(), is(true));
            Assert.assertThat(resultTextElement.getText(), is("Reloading..."));

            // Wait for the Reload to terminate
            new WebDriverWait(m_driver, maxReloadTimeInSeconds).until((Predicate<WebDriver>) (driver) -> {
                final String reloadStateText = getResultElement().getText();
                return !reloadStateText.equals("Reloading...");
            });

            // Check Ui State after the Reload terminated and return the reloadStateText
            reloadButton = getReloadButton();
            Assert.assertThat(reloadButton.isDisplayed(), is(true));
            Assert.assertThat(reloadButton.isEnabled(), is(true));

            resultTextElement = getResultElement();
            Assert.assertThat(resultTextElement.isDisplayed(), is(true));
            return resultTextElement.getText().contains("successfully");
        }

        public String getCurlString() {
            if (!this.isReloadable()) {
                throw new IllegalStateException("This daemon is not reloadable");
            }

            // Verify dialog is not present
            try {
                getCurlModal();
                Assert.fail("Modal should not be present on the page at this point");
            } catch (NoSuchElementException ex) {
                // expected
            }

            // Find button and click it
            WebElement curlButton = getCurlButton();
            curlButton.click();

            // Ensure modal dialog is visible
            WebElement modal = getCurlModal();
            Assert.assertThat(modal.isDisplayed(), is(true));
            Assert.assertThat(modal.isEnabled(), is(true));

            // Extract text
            final String curlString = getCurlText();

            // Close dialog
            WebElement closeButton = getCurlModal().findElement(By.xpath(".//div[contains(@class, 'modal-footer')]/button"));
            closeButton.click();

            // Verify dialog was closed
            new WebDriverWait(m_driver, 5).until((Function<? super WebDriver, Boolean>) driver -> {
                try {
                    getCurlModal();
                    return false;
                } catch (NoSuchElementException ex) {
                    return true;
                }
            });

            // Return the string
            return curlString;
        }

        private WebElement getCurlModal() {
            return m_driver.findElement(By.id("curlModal"));
        }

        private WebElement getDaemonRowElement() {
            final String curlXpathExpression = String.format("//table/tbody/tr/td[contains(text(), '%s')]/..", daemonName);
            return m_driver.findElement(By.xpath(curlXpathExpression));
        }

        private WebElement getReloadButton() {
            return getDaemonRowElement().findElement(By.xpath("./td[3]//button/i[@class='fa fa-repeat fa-lg']/.."));
        }

        private WebElement getCurlButton() {
            return getDaemonRowElement().findElement(By.xpath("./td[3]//button/i[@class='fa fa-terminal fa-lg']/.."));
        }

        private WebElement getResultElement() {
            return getDaemonRowElement().findElement(By.xpath("./td[4]/span[2]"));
        }

        private String getCurlText() {
            return getCurlModal().findElement(By.xpath(".//pre")).getText();
        }
    }

    private class DaemonReloadPage {

        public DaemonReloadPage open() {
            m_driver.get(getBaseUrl() + "opennms/admin/daemons/index.jsp");
            return this;
        }

        public List<WebElement> getDaemonRows() {
            return m_driver.findElements(By.xpath("//table/tbody/tr"));
        }

        public Daemon getDaemon(String daemonName) {
            return new Daemon(daemonName);
        }

        public void showAll() {
            getFilterElement("All").click();
        }

        public void showReloadableAndEnabled() {
            getFilterElement("Reloadable").click();
        }

        private WebElement getFilterElement(String filter) {
            final String filterXpath = String.format("//div[@class='btn-group']//label[contains(text(), '%s')]", filter);
            return m_driver.findElement(By.xpath(filterXpath));
        }
    }
}
