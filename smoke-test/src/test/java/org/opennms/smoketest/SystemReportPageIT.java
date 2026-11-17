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

public class SystemReportPageIT extends OpenNMSSeleniumIT {

    @Test
    public void legacySystemReportPagesRedirectToTheVuePage() {
        for (final String legacy : new String[] { "admin/support/systemReport.htm", "admin/support/systemReportList.htm" }) {
            driver.get(getBaseUrlInternal() + "opennms/" + legacy);
            wait.until(ExpectedConditions.urlContains("/ui/index.html#/system-report"));
            // the options come from /api/v2/system-report; a failed load would still render the button
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sr-plugin-Java")));
            assertTrue("system report options failed to load", driver.findElements(By.cssSelector("[data-test=load-error]")).isEmpty());
        }
    }
}
