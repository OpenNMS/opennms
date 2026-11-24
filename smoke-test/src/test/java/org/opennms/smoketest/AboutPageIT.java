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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AboutPageIT extends OpenNMSSeleniumIT {

    @Before
    public void setUp() throws Exception {
        driver.get(getBaseUrlInternal() + "opennms/ui/index.html#/about");
    }

    @Test
    public void hasAllPanels() throws Exception {
        for (final String title : new String[] { "System details", "License and copyright", "Built with" }) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[text()='" + title + "']")));
        }
    }

    @Test
    public void hasServerDetails() {
        // the Database Type row only appears once /api/v2/about has answered
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-test='version-details']//dt[text()='Database Type']")));
        assertEquals("PostgreSQL", driver.findElement(By.xpath("//div[@data-test='version-details']//dt[text()='Database Type']/following-sibling::dd[1]")).getText());
    }

    @Test
    public void legacyUrlRedirects() {
        driver.get(getBaseUrlInternal() + "opennms/about/index.jsp");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[text()='System details']")));
    }
}
