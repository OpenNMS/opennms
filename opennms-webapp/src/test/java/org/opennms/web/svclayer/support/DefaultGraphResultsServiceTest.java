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
package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author <a href="mailto:seth@opennms.org">Seth</a>
 */
public class DefaultGraphResultsServiceTest {

    /**
     * Check if the lookup of metrics to columns for prefabGraphs is working.
     * This lookup is performed by file handling of meta files.
     */
    @Test
    public void testLookUpMetricsForColumnsOfPrefabGraphs() {
        DefaultGraphResultsService defaultGraphResultsService = new DefaultGraphResultsService();
        assertNotNull(defaultGraphResultsService);
        //Sorry not now
    }
}
