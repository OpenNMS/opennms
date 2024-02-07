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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;


public class CategoriesBoxIT extends OpenNMSSeleniumIT {

    @Before
    public void before() {
        frontPage();
    }

    @Test
    public void testCategoryLink() throws Exception {
        // Hit the default "Network Interfaces" link on the startpage
        findElementByLink("Network Interfaces").click();
        // check for correct url...
        wait.until(ExpectedConditions.urlContains("/opennms/rtc/category.jsp"));
        // ...and header cell
        findElementByXpath("//th[text()='24hr Availability']");
    }
}
