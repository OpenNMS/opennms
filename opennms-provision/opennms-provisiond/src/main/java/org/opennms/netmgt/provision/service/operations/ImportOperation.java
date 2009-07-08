/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.provision.service.operations;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.service.ProvisionService;


public abstract class ImportOperation {
    
    final private ProvisionService m_provisionService;
    
    public ImportOperation(ProvisionService provisionService) {
        m_provisionService = provisionService;
    }


    abstract public void scan();

    /**
     * @return the provisionService
     */
    protected ProvisionService getProvisionService() {
        return m_provisionService;
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    protected abstract void doPersist();


    public void persist() {
    
        final ImportOperation oper = this;
    
        log().info("Persist: "+oper);
    
        doPersist();
    	
    
        log().info("Clear cache: "+this);
    
        // clear the cache to we don't use up all the memory
    	getProvisionService().clearCache();
    }


}
