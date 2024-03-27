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
package org.opennms.netmgt.provision;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Integrates Puppet with OpenNMS.  We need create a Puppet Java API for this class.
 * Jason, you know anything about openQRM?
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class PuppetProvisioningAdapter extends SimpleQueuedProvisioningAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(PuppetProvisioningAdapter.class);

    private static final String ADAPTER_NAME = "PuppetAdapter";

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return ADAPTER_NAME;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNodeReady(AdapterOperation op) {
        // TODO Auto-generated method stub
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void processPendingOperationForNode(final AdapterOperation op) throws ProvisioningAdapterException {
        LOG.info("processPendingOperationForNode: Handling Operation: {}", op);
        
        if (op.getType() == AdapterOperationType.ADD || op.getType() == AdapterOperationType.UPDATE) {
            throw new ProvisioningAdapterException(new UnsupportedOperationException("This operation: "+op+", is currently not supported."));
        } else if (op.getType() == AdapterOperationType.DELETE) {
            throw new ProvisioningAdapterException(new UnsupportedOperationException("This operation: "+op+", is currently not supported."));
        } else if (op.getType() == AdapterOperationType.CONFIG_CHANGE) {
            throw new ProvisioningAdapterException(new UnsupportedOperationException("This operation: "+op+", is currently not supported."));
        } else {
            LOG.warn("unknown operation: {}", op.getType());
        }
    }
}
