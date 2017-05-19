/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
