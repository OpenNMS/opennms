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
package org.opennms.features.apilayer.common.requisition;

import java.util.Map;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
import org.opennms.integration.api.v1.requisition.RequisitionProvider;
import org.osgi.framework.BundleContext;

import com.google.common.collect.ImmutableMap;

public class RequisitionProviderManager extends InterfaceMapper<RequisitionProvider, org.opennms.netmgt.provision.persist.RequisitionProvider> {

    public RequisitionProviderManager(BundleContext bundleContext) {
        super(org.opennms.netmgt.provision.persist.RequisitionProvider.class, bundleContext);
    }

    @Override
    public org.opennms.netmgt.provision.persist.RequisitionProvider map(RequisitionProvider ext) {
        return new RequisitionProviderImpl(ext);
    }

    @Override
    public Map<String, Object> getServiceProperties(RequisitionProvider extension) {
        return ImmutableMap.<String,Object>builder()
                // Registry needs type of provider
                .put("type", extension.getType())
                .build();
    }
}
