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
package org.opennms.netmgt.provision.detector.registry.api;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.ServiceDetectorFactory;

/**
 * Used to keep track of all the available {@link ServiceDetector} implementations exposed
 * by {@link ServiceDetectorFactory} beans and provides the ability to instantiate these.
 *
 * @author jwhite
 */
public interface ServiceDetectorRegistry {

    Map<String, String> getTypes();

    Set<String> getClassNames();

    ServiceDetector getDetectorByClassName(String className, Map<String, String> properties);

    ServiceDetectorFactory<?> getDetectorFactoryByClassName(String className);

    Set<String> getServiceNames();

    String getDetectorClassNameFromServiceName(String serviceName);

    Class<?> getDetectorClassByServiceName(String serviceName);

}
