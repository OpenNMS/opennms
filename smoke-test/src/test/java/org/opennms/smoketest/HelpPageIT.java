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

public class HelpPageIT extends OpenNMSSeleniumIT {

    @Before
    public void setUp() throws Exception {
        driver.get(getBaseUrlInternal() + "opennms/help/index.jsp");
    }

    @Test
    public void verifyAllButtonsPresent() throws Exception {
        final String[] links = new String[] {
                "Deployment Guide",
                "Operation Guide",
                "Development Guide",
                "OpenAPI Docs",
                "Swagger UI",
                "Commercial Support",
                "Web Chat",
                "Discourse",
                "OpenNMS on GitHub",
                "Issue Tracker",
                "Continuous Integration"
        };
        assertEquals(links.length, countElementsMatchingCss("a.btn"));
        for (final String text : links) {
            assertNotNull("Link with text '" + text + "' must exist.", driver.findElement(By.linkText(text)));
        }
    }
}
