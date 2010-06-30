/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 27, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision;


import org.opennms.core.utils.ThreadCategory;


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
        log().info("processPendingOperationForNode: Handling Operation: "+op);
        
        if (op.getType() == AdapterOperationType.ADD || op.getType() == AdapterOperationType.UPDATE) {
            throw new ProvisioningAdapterException(new UnsupportedOperationException("This operation: "+op+", is currently not supported."));
        } else if (op.getType() == AdapterOperationType.DELETE) {
            throw new ProvisioningAdapterException(new UnsupportedOperationException("This operation: "+op+", is currently not supported."));
        } else if (op.getType() == AdapterOperationType.CONFIG_CHANGE) {
            throw new ProvisioningAdapterException(new UnsupportedOperationException("This operation: "+op+", is currently not supported."));
        } else {
            log().warn("unknown operation: " + op.getType());
        }
    }

    private static ThreadCategory log() {
        return ThreadCategory.getInstance(PuppetProvisioningAdapter.class);
    }

}
