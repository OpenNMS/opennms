/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class OpenNMSDaemonTest extends MockObjectTestCase {
    
    Mock m_mockConfiguration;
    Mock m_mockForwarder;
    OpenNMSDaemon m_daemon;
    
    public void setUp() {
      
        m_mockConfiguration = mock(Configuration.class);
        m_mockForwarder = mock(EventForwarder.class);
        
        //m_daemon = new OpenNMSDaemon();
        //m_daemon.setConfiguration(getConfiguration());
       // m_daemon.setEventForwarder(getForwarder());
        
        
        
        
    }
    
    public Configuration getConfiguration() {
        return (Configuration)m_mockConfiguration.proxy();
    }
    
    public static NNMEvent getEvent() {
        return MockNNMEvent.createEvent("Category", "Warning", "linkDown", "1.1.1.1");
    }

    public EventForwarder getForwarder() {
        return (EventForwarder)m_mockForwarder.proxy();
    }
    
    public void testBogus() {}
    public void XXXtestInitWithNoConfig() {
        OpenNMSDaemon daemon = new OpenNMSDaemon();
        try {
            daemon.onInit();
            fail("Expected an exception");
        } catch (IllegalStateException e) {
            
        } catch (Throwable t) {
            fail("Unexpected exception "+t);
        }
        
        daemon.onStop();
        
    }
    
    public void XXXtestInit() {
        
        m_daemon.onInit();
        
        m_daemon.onStop();

    }
    
    public void XXXtestEventAccept() {

        NNMEvent event = getEvent();

        m_mockForwarder.expects(once()).method("accept").
        with( same( event ) );


        testEventProcessing(Filter.ACCEPT, event);
    }
    
    public void XXXtestEventDiscard() {
        NNMEvent event = getEvent();
        
        m_mockForwarder.expects(once()).method("discard").
        with( same( event ) );

        testEventProcessing(Filter.DISCARD, event);
    }

    private void testEventProcessing(String action, NNMEvent event) {
        Filter acceptEverything = new Filter();
        acceptEverything.setAction(action);
        

        FilterChain filterChain = new FilterChain();
        filterChain.addFilter(acceptEverything);
        
        

        m_mockConfiguration.expects(once()).
            method("getFilterChain").
            will( returnValue(filterChain) );
        

        m_daemon.onInit();
        
        m_daemon.onEvent(event);
        
        m_daemon.onStop();
    }

}
