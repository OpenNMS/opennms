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
package org.opennms.features.jmxconfiggenerator.jmxconfig.query;

import javax.management.MBeanAttributeInfo;
import java.util.regex.Pattern;

/**
 * Represents a filter criteria to be used by the {@link MBeanServerQuery}.
 */
public class FilterCriteria {
    public String objectName;
    public String attributeName;

    FilterCriteria() {

    }

    public FilterCriteria(String objectName, String attributeName) {
        this.objectName = objectName;
        this.attributeName = attributeName;
    }

    public static FilterCriteria parse(String input) {
        FilterCriteria qd = new FilterCriteria();
        String[] split = input.split(":");
        if (split.length == 3) {
            qd.objectName = input.substring(0, input.lastIndexOf(":"));
            qd.attributeName = input.substring(input.lastIndexOf(":") + 1);
        } else {
            qd.objectName = input;
        }
        return qd;
    }

    public String toString() {
        if (attributeName != null) {
            return objectName + ":" + attributeName;
        }
        return objectName;
    }

    public boolean matches(MBeanAttributeInfo eachAttribute) {
        Pattern pattern = attributeName != null ? Pattern.compile(attributeName) : null;
        return pattern == null || pattern.matcher(eachAttribute.getName()).matches();
    }
}