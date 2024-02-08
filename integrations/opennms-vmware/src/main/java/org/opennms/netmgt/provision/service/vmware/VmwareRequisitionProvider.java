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
package org.opennms.netmgt.provision.service.vmware;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.dao.vmware.VmwareConfigDao;
import org.opennms.netmgt.provision.persist.AbstractRequisitionProvider;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Generates requisition for Vmware entities.
 *
 * See {@link VmwareImportRequest} for all supported options.
 *
 * @author jwhite
 */
public class VmwareRequisitionProvider extends AbstractRequisitionProvider<VmwareImportRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(VmwareRequisitionProvider.class);

    public static final String TYPE_NAME = "vmware";

    @Autowired
    @Qualifier("fileDeployed")
    private ForeignSourceRepository foreignSourceRepository;

    @Autowired
    private VmwareConfigDao vmwareConfigDao;

    public VmwareRequisitionProvider() {
        super(VmwareImportRequest.class);
    }

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public VmwareImportRequest getRequest(Map<String, String> parameters) {
        // Generate a request using the parameter map
        final VmwareImportRequest request = new VmwareImportRequest(parameters);
        if (StringUtils.isBlank(request.getUsername()) ||
                StringUtils.isBlank(request.getPassword())) {
            // No credentials were specified in the parameter map, attempt to look these up
            final Map<String, VmwareServer> serverMap = vmwareConfigDao.getServerMap();
            final VmwareServer vmwareServer = serverMap.get(request.getHostname());
            if (vmwareServer != null) {
                // We found a corresponding entry - copy the credentials to the request
                request.setUsername(vmwareServer.getUsername());
                request.setPassword(vmwareServer.getPassword());
            }
        }
        // Lookup the existing requisition, and store it in the request
        final Requisition existingRequisition = getExistingRequisition(request.getForeignSource());
        request.setExistingRequisition(existingRequisition);
        return request;
    }

    @Override
    public Requisition getRequisitionFor(VmwareImportRequest request) {
        final VmwareImporter importer = new VmwareImporter(request);
        return importer.getRequisition();
    }

    protected Requisition getExistingRequisition(String foreignSource) {
        try {
            return foreignSourceRepository.getRequisition(foreignSource);
        } catch (Exception e) {
            LOG.warn("Can't retrieve requisition {}", foreignSource, e);
            return null;
        }
    }

}
