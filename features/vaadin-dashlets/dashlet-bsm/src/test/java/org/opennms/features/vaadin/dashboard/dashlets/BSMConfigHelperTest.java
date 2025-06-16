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
package org.opennms.features.vaadin.dashboard.dashlets;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.bsm.service.BusinessServiceSearchCriteriaBuilder;

public class BSMConfigHelperTest {
   
    @Test
    public void testFromMap() {
        Map<String, String> properties = new HashMap<>();
        properties.put("attributeKey", null);
        properties.put("attributeValue", null);
        properties.put("filterByAttribute", "false");
        properties.put("filterByName", "false");
        properties.put("filterBySeverity", "false");
        properties.put("nameValue", null);
        properties.put("orderAsc", "true");
        properties.put("orderBy", "Severity");
        properties.put("orderSequence", "Descending");
        properties.put("resultsLimit", "10");
        properties.put("severityCompareOperator", "GreaterOrEqual");
        properties.put("severityValue", "Warning");
        properties.put("columnCountBoard", "10");
        properties.put("columnCountPanel", "5");

        BusinessServiceSearchCriteriaBuilder businessServiceSearchCriteria = (BusinessServiceSearchCriteriaBuilder) BSMConfigHelper.fromMap(properties);
        Assert.assertEquals(BusinessServiceSearchCriteriaBuilder.Order.Severity, businessServiceSearchCriteria.getOrder());
        Assert.assertEquals(BusinessServiceSearchCriteriaBuilder.Sequence.Descending, businessServiceSearchCriteria.getSequence());
    }
}
