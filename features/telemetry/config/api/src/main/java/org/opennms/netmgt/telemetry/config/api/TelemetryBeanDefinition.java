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
package org.opennms.netmgt.telemetry.config.api;

import java.util.Map;

/**
 * The {@link TelemetryBeanDefinition} defines a bean in order to create it afterwards usually via a Factory.
 * It is required in order to allow configuration of beans via a properties file, to for example configure
 * some features and later instantiate the bean accordingly.
 *
 * @author mvrueden
 */
public interface TelemetryBeanDefinition {

    /** The name of the bean */
    String getName();

    /** The type of the bean */
    String getClassName();

    /** Additional parameters for the bean, e.g. to fill setters */
    Map<String, String> getParameterMap();
}
