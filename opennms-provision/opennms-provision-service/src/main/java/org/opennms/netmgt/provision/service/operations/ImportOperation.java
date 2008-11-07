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

import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.service.DefaultProvisionService;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


public abstract class ImportOperation {
    
    final private DefaultProvisionService m_provisionService;
    
    public ImportOperation(DefaultProvisionService provisionService) {
        m_provisionService = provisionService;
    }


    @SuppressWarnings("unchecked")
    private List<Event> persist() {
        TransactionTemplate template = m_provisionService.getTransactionTemplate();
        
        return (List<Event>) template.execute(new TransactionCallback() {
    		public Object doInTransaction(TransactionStatus status) {
    			List<Event> result = doPersist();
                return result;
    		}
    	});
    }

    abstract public void gatherAdditionalData();

    /**
     * @return the provisionService
     */
    protected DefaultProvisionService getProvisionService() {
        return m_provisionService;
    }

    protected abstract List<Event> doPersist();

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void persist(final ProvisionMonitor monitor) {
        
        TransactionTemplate template = m_provisionService.getTransactionTemplate();
    
        final ImportOperation oper = this;
    
    	template.execute(new TransactionCallbackWithoutResult() {
    	    @Override
        	public void doInTransactionWithoutResult(TransactionStatus status) {
        	    
                DefaultProvisionService provisionService = getProvisionService();

                monitor.beginPersisting(oper);
                log().info("Persist: "+oper);

                List<Event> events = doPersist();
                

                monitor.finishPersisting(oper);
                
                monitor.beginSendingEvents(oper, events);
                
                provisionService.sendEvents(oper, events);
            
                monitor.finishSendingEvents(oper, events);

        	}

        });
    	

        log().info("Clear cache: "+this);

        // clear the cache to we don't use up all the memory
    	m_provisionService.clearCache();
    }


}
