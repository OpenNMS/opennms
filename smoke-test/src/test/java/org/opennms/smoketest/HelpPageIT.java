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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HelpPageIT extends OpenNMSSeleniumIT {

    private void open(final String route) {
        driver.get(getBaseUrlInternal() + "opennms/ui/index.html#/" + route);
    }

    private static String linkXpath(final String text) {
        return "//a[@data-test='help-link'][.//span[@class='tile-label' and text()='" + text + "']]";
    }

    private void waitForLink(final String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(linkXpath(text))));
    }

    @Test
    public void documentationLinks() {
        open("documentation");
        for (final String text : new String[] { "OpenNMS Documentation", "Deployment Guide", "Operation Guide", "Development Guide", "Release Notes", "OpenAPI Documentation" }) {
            waitForLink(text);
        }
    }

    @Test
    public void commercialSupportLinks() {
        open("commercial-support");
        // the smoke tests log in as admin, so the system report link is offered too
        for (final String text : new String[] { "Support Portal", "Generate a System Report", "Why OpenNMS", "Contact Us" }) {
            waitForLink(text);
        }
    }

    @Test
    public void communityResourcesLinks() {
        open("community-resources");
        for (final String text : new String[] { "Discourse", "Web Chat", "OpenNMS on GitHub", "Issue Tracker", "Continuous Integration" }) {
            waitForLink(text);
        }
    }

    @Test
    public void systemReportFromCommercialSupport() {
        open("commercial-support");
        waitForLink("Generate a System Report");
        // clickElement waits out the page spinner fading over the tile
        clickElement(By.xpath(linkXpath("Generate a System Report")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'breadcrumbs')]//a[contains(text()[normalize-space()], 'Generate System Report')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(normalize-space(.), 'Generate System Report')]")));
        // the options come from /api/v2/system-report; a failed load would still render the button
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sr-plugin-Java")));
        assertTrue("system report options failed to load", driver.findElements(By.cssSelector("[data-test=load-error]")).isEmpty());
    }

    @Test
    public void legacyUrlsRedirect() {
        driver.get(getBaseUrlInternal() + "opennms/help/index.jsp");
        waitForLink("Deployment Guide");
        driver.get(getBaseUrlInternal() + "opennms/support/index.jsp");
        waitForLink("Support Portal");
    }
}
