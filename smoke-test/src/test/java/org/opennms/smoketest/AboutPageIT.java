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
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

public class AboutPageIT extends OpenNMSSeleniumIT {

    @Before
    public void setUp() throws Exception {
        driver.get(getBaseUrlInternal() + "opennms/about/index.jsp");
    }

    @Test
    public void hasAllPanels() throws Exception {
        assertEquals(4, countElementsMatchingCss("div.card-header"));
    }

    @Test
    public void hasContent() {
        assertNotNull(driver.findElement(By.xpath("//span[text()='License and Copyright']")));
        assertNotNull(driver.findElement(By.xpath("//th[text()='Version:']")));
    }
}
