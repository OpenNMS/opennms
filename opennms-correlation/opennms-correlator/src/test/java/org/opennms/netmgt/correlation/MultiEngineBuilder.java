/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation;

import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class MultiEngineBuilder implements InitializingBean {
    
    
    private static class MyEngine extends AbstractCorrelationEngine {

        @Override
        public void correlate(Event e) {
            EventBuilder bldr = new EventBuilder("listLoaded", "TestEngine");
            sendEvent(bldr.getEvent());
        }

        @Override
        public List<String> getInterestingEvents() {
            String[] ueis = {
              "isListLoaded"      
            };
            return Arrays.asList(ueis);
        }

        @Override
        protected void timerExpired(Integer timerId) {
            
        }

        @Override
        public String getName() {
           return "MyEngine";
        }
        
    }
    
    CorrelationEngine[] m_engines;
    CorrelationEngineRegistrar m_correlator;
    EventIpcManager m_eventIpcManager;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        MyEngine engine = new MyEngine();
        engine.setEventIpcManager(m_eventIpcManager);
        
        m_correlator.addCorrelationEngine(engine);
    }
    
    public void setCorrelator(CorrelationEngineRegistrar correlator) {
        m_correlator = correlator;
    }

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

}
