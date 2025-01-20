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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SupportPageIT extends OpenNMSSeleniumIT {
    @Before
    public void setUp() throws Exception {
        supportPage();
    }

    @Test
    public void testAllButtonsArePresent() throws Exception {
        final String[] links = new String[] {
                "OpenNMS Support Portal",
                "Generate System Report",
                "Collectd Statistics",
                "About OpenNMS",
                "Commercial Support",
                "Web Chat",
                "Discourse"
        };
        assertEquals(links.length, countElementsMatchingCss("div.card-body a"));
        for (final String text : links) {
            assertNotNull("Link with text '" + text + "' must exist.", driver.findElement(By.linkText(text)));
        }
    }

    @Test
    public void testSystemReport() {
        driver.findElement(By.linkText("Generate System Report")).click();

        final WebElement textFileReportRadio = driver.findElement(By.id("radio1"));
        final WebElement logFilesRadio = driver.findElement(By.id("radio2"));
        final WebElement javaReportCheckbox = driver.findElement(By.cssSelector("input[type=checkbox][name=plugins][value=Java]"));
        final WebElement configurationReportRadio = driver.findElement(By.cssSelector("input[type=radio][name=plugins][value=Configuration]"));
        final WebElement logsReportRadio = driver.findElement(By.cssSelector("input[type=radio][name=plugins][value=Logs]"));

        assertThat(textFileReportRadio.isSelected(), is(true));
        assertThat(logFilesRadio.isSelected(), is(false));
        assertThat(javaReportCheckbox.isSelected(), is(true));
        assertThat(configurationReportRadio.isSelected(), is(false));
        assertThat(logsReportRadio.isSelected(), is(false));

        javaReportCheckbox.click();
        assertThat(javaReportCheckbox.isSelected(), is(false));

        logFilesRadio.click();
        assertThat(textFileReportRadio.isSelected(), is(false));
        assertThat(logFilesRadio.isSelected(), is(true));
        assertThat(javaReportCheckbox.isSelected(), is(false));
        assertThat(configurationReportRadio.isSelected(), is(true));
        assertThat(logsReportRadio.isSelected(), is(false));

        logsReportRadio.click();
        assertThat(configurationReportRadio.isSelected(), is(false));
        assertThat(logsReportRadio.isSelected(), is(true));
    }

}
