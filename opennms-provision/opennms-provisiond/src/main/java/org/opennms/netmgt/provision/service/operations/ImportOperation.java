/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.provision.service.ProvisionService;

public abstract class ImportOperation {
    private static final Logger LOG = LoggerFactory.getLogger(ImportOperation.class);
    
    final private ProvisionService m_provisionService;
    
    /**
     * <p>Constructor for ImportOperation.</p>
     *
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     */
    public ImportOperation(ProvisionService provisionService) {
        m_provisionService = provisionService;
    }


    /**
     * <p>scan</p>
     */
    abstract public void scan();

    /**
     * <p>getProvisionService</p>
     *
     * @return the provisionService
     */
    protected ProvisionService getProvisionService() {
        return m_provisionService;
    }

    /**
     * <p>doPersist</p>
     */
    protected abstract void doPersist();


    /**
     * <p>persist</p>
     */
    public void persist() {
    
        final ImportOperation oper = this;
    
        LOG.info("Persist: {}", oper);
    
        doPersist();
    	
    
        LOG.info("Clear cache: {}", this);
    
        // clear the cache to we don't use up all the memory
    	getProvisionService().clearCache();
    }


}
