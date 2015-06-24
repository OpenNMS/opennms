/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.opennmsd;

import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.nnm.swig.OVsnmpSession;
import org.opennms.ovapi.TrapProcessingDaemon;

public class OpenNMSDaemon extends TrapProcessingDaemon implements ProcessManagementListener, NNMEventListener {
	
	private Configuration m_configuration;
	private EventForwarder m_eventForwarder;
	
    public void setConfiguration(Configuration configuration) {
        m_configuration = configuration;
    }
    
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

	public String onInit() {
		Assert.notNull(m_configuration, "the configuration property must not be null");
		Assert.notNull(m_eventForwarder, "the eventForwarder property must not be null");
		
		super.onInit();
		
		return "opennms initialization complete.";
	}

	public String onStop() {
	    super.onStop();
	    
	    return "opennms stopped successfully.";
		
	}

	public void onEvent(NNMEvent event) {
	    FilterChain chain = m_configuration.getFilterChain();
	    
	    String action = chain.filterEvent(event);
	    
	    if (Filter.PRESERVE.equals(action)) {
	        m_eventForwarder.preserve(event);
	    } else if (Filter.ACCEPT.equals(action)) {
            m_eventForwarder.accept(event);
	    } else {
	        m_eventForwarder.discard(event);
	    }
		
	}

    protected void onEvent(int reason, OVsnmpSession session, OVsnmpPdu pdu) {
        
        onEvent(new DefaultNNMEvent(pdu));

    }

 
}
