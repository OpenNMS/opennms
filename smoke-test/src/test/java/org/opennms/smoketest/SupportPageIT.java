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

        // checkboxes are selected by default
        final WebElement allCheckbox = driver.findElement(By.cssSelector("input[type=checkbox][name=all]"));
        assertThat(driver.findElement(By.cssSelector("input[type=checkbox][name=plugins][value=Java]")).isSelected(), is(true));

        // deselect the "all" checkbox
        allCheckbox.click();
        assertThat(driver.findElement(By.cssSelector("input[type=checkbox][name=plugins][value=Java]")).isSelected(), is(false));
    }
}
