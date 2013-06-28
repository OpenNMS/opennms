/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
