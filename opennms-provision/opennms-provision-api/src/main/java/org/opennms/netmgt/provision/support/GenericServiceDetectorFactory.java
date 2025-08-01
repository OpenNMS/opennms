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
package org.opennms.netmgt.provision.support;

import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.ServiceDetectorFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class GenericServiceDetectorFactory<T extends ServiceDetector> implements ServiceDetectorFactory<T> {

    private final Class<T> clazz;

    public GenericServiceDetectorFactory(Class<T> clazz) {
        this.clazz = Objects.requireNonNull(clazz);
    }

    @Override
    public T createDetector(Map<String, String> properties) {
        try {
            T detector = clazz.newInstance();
            setBeanProperties(detector, properties);
            return detector;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<T> getDetectorClass() {
        return clazz;
    }

    @Override
    public DetectRequest buildRequest(String location, InetAddress address, Integer port, Map<String, String> attributes) {
        return new DetectRequestImpl(address, port, attributes);
    }

    @Override
    public void afterDetect(DetectRequest request, DetectResults results, Integer nodeId) {
        //pass
    }

    /**
     * Set detector attributes as bean properties.
     * @param detector  {@link ServiceDetector}
     * @param properties detector attributes from foreign source configuration
     */
    public void setBeanProperties(ServiceDetector detector, Map<String, String> properties) {
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(detector);
        wrapper.setPropertyValues(properties);
    }

}
