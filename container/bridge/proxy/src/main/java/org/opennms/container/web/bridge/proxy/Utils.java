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
package org.opennms.container.web.bridge.proxy;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.ServiceReference;

public final class Utils {

    private Utils() {}

    public static List<String> getListProperty(ServiceReference reference, String key) {
        final List<String> returnList = new ArrayList<>();
        final Object property = reference.getProperty(key);
        if (property instanceof String) {
            final String value = ((String) property).trim();
            if (value != null && !"".equals(property)) {
                returnList.add(value);
            }
        }
        return returnList;
    }

    public static String getStringProperty(ServiceReference reference, String key) {
        final Object property = reference.getProperty(key);
        if (property instanceof String) {
            return ((String) property).trim();
        }
        return null;
    }
}
