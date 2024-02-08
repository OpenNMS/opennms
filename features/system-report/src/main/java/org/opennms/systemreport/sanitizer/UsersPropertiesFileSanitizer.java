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
package org.opennms.systemreport.sanitizer;

import java.util.Properties;

public class UsersPropertiesFileSanitizer extends PropertiesFileSanitizer {

    @Override
    public String getFileName() {
        return "users.properties";
    }

    @Override
    protected void sanitizeProperties(Properties properties) {
        properties.stringPropertyNames().forEach(propertyName -> {
            if (!propertyName.startsWith("_g_:")) {
                String propertyValue = properties.getProperty(propertyName);
                String[] propertyParts = propertyValue.split(",");
                propertyParts[0] = SANITIZED_VALUE;
                properties.setProperty(propertyName, String.join(",", propertyParts));
            }
        });
    }

}
