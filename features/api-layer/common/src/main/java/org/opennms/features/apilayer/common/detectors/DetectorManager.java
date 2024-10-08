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
package org.opennms.features.apilayer.common.detectors;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
import org.opennms.integration.api.v1.detectors.ServiceDetectorFactory;
import org.osgi.framework.BundleContext;

/**
 * Manager to plug detectors from integration-api to provisioning detectors.
 */
public class DetectorManager extends InterfaceMapper<ServiceDetectorFactory, org.opennms.netmgt.provision.ServiceDetectorFactory> {


    public DetectorManager(BundleContext bundleContext) {
        super(org.opennms.netmgt.provision.ServiceDetectorFactory.class, bundleContext);
    }

    @Override
    public org.opennms.netmgt.provision.ServiceDetectorFactory map(ServiceDetectorFactory ext) {
        return new ServiceDetectorFactoryImpl(ext);
    }

}
