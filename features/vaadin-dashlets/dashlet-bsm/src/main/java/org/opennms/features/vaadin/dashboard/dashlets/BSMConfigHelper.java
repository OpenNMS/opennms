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

import java.util.Map;

import org.opennms.netmgt.bsm.service.BusinessServiceSearchCriteria;
import org.opennms.netmgt.bsm.service.BusinessServiceSearchCriteriaBuilder;
import org.opennms.netmgt.bsm.service.model.Status;

import com.google.common.base.Strings;

/**
 * Small helper class for loading search criteria from the dashlet's parameter map.
 *
 * @author Christian Pape <christian@opennms.org>
 */
public class BSMConfigHelper {
    /**
     * Returns a boolean value for a given key from the map where "1", "true", "yes" and "on"
     * are interpeted as boolean true, otherwise false.
     *
     * @param map the map to be used
     * @param key the key
     * @return the boolean value
     */
    public static boolean getBooleanForKey(Map<String, String> map, String key) {
        String value = map.get(key);
        return ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value));
    }

    /**
     * Returns the string value for a given key. Null values will be returned as empty
     * strings.
     *
     * @param map the map to use
     * @param key the key
     * @param defaultValue default value
     * @return the string value, defaultValue if Null or empty
     */
    public static String getStringForKey(Map<String, String> map, String key, String defaultValue) {
        String value = map.get(key);

        if (Strings.isNullOrEmpty(value)) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * Returns the int value for a given key. Unparsable values are returned as zero.
     *
     * @param map the map to be used
     * @param key the key
     * @param defaultValue default value
     * @return the int value, defaultValue if not parsable
     */
    public static int getIntForKey(Map<String, String> map, String key, int defaultValue) {
        String value = map.get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Created as business service search criteria for a given map.
     *
     * @param map the map to inverstigate
     * @return the criteria
     */
    public static BusinessServiceSearchCriteria fromMap(Map<String, String> map) {
        boolean filterByName = getBooleanForKey(map, "filterByName");
        String nameValue = getStringForKey(map, "nameValue", "");
        boolean filterByAttribute = getBooleanForKey(map, "filterByAttribute");
        String attributeKey = getStringForKey(map, "attributeKey", "");
        String attributeValue = getStringForKey(map, "attributeValue", "");
        boolean filterBySeverity = getBooleanForKey(map, "filterBySeverity");
        String severityValue = getStringForKey(map, "severityValue", Status.WARNING.getLabel());
        String severityCompareOperator = BSMConfigHelper.getStringForKey(map, "severityCompareOperator", BusinessServiceSearchCriteriaBuilder.CompareOperator.GreaterOrEqual.name());
        String orderBy = BSMConfigHelper.getStringForKey(map, "orderBy", BusinessServiceSearchCriteriaBuilder.Order.Name.name());
        String orderSequence = BSMConfigHelper.getStringForKey(map, "orderSequence", BusinessServiceSearchCriteriaBuilder.Sequence.Ascending.name());
        int resultsLimit = getIntForKey(map, "resultsLimit", 10);

        BusinessServiceSearchCriteriaBuilder b = new BusinessServiceSearchCriteriaBuilder();

        if (filterByName) {
            b.name(nameValue);
        }
        if (filterByAttribute) {
            b.attribute(attributeKey, attributeValue);
        }
        if (filterBySeverity) {
            b.filterSeverity(BusinessServiceSearchCriteriaBuilder.CompareOperator.valueOf(severityCompareOperator), Status.valueOf(severityValue));
        }

        return b.order(BusinessServiceSearchCriteriaBuilder.Order.valueOf(orderBy))
                .order(BusinessServiceSearchCriteriaBuilder.Sequence.valueOf(orderSequence))
                .limit(resultsLimit);
    }
}
