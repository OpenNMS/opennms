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

import org.junit.Test;

public class NRTGraphIT extends OpenNMSSeleniumIT {
    @Test
    public void canLoadGraph() {
        // Request a known graph with an invalid resource id
        driver.get(getBaseUrlInternal() + "opennms/graph/nrtg.jsp?resourceId=node[999].nodeSnmp[]&report=mib2.tcpopen");
        // The graph should be rendered
        findElementByXpath("//div[@class='flot-datatable-tabs']");
        // It won't have any data, but this is sufficient to very that all of the required
        // Javascript files have been loaded, and the AJAX call to get the graph was successful
    }
}
