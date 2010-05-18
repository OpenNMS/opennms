//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
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

    public ThreadCategory log() {
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
