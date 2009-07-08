/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision;


import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;


/**
 * Integrates Puppet with OpenNMS.  We need create a Puppet Java API for this class.
 * Jason, you know anything about openQRM?
 * 
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class PuppetProvisioningAdapter extends SimpleQueuedProvisioningAdapter {

    private static final String ADAPTER_NAME = "PuppetAdapter";

    @Override
    public String getName() {
        return ADAPTER_NAME;
    }

    @Override
    public boolean isNodeReady(AdapterOperation op) {
        // TODO Auto-generated method stub
        return false;
    }

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

    private static Category log() {
        return ThreadCategory.getInstance(PuppetProvisioningAdapter.class);
    }

}