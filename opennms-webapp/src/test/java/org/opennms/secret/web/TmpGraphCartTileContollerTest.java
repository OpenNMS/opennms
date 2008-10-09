//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.secret.web;

import java.util.LinkedList;

import junit.framework.TestCase;

import org.opennms.secret.model.GraphDataElement;
import org.opennms.secret.model.GraphDefinition;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

public class TmpGraphCartTileContollerTest extends TestCase {

    private MockHttpSession m_session;
    private MockHttpServletRequest m_request;
    private MockHttpServletResponse m_response;
    private TmpGraphCartTileController m_controller;
    private GraphDefinition m_graph;
    private LinkedList m_datasources;
	private GraphDefinition m_graphDef;

    protected void setUp() throws Exception {
        m_session = new MockHttpSession();
        resetRequestResponse();
        m_controller = new TmpGraphCartTileController();
    }

    private void resetRequestResponse() {
        m_request = new MockHttpServletRequest();
        m_request.setSession(m_session);
        m_response = new MockHttpServletResponse();
    }

    protected void tearDown() throws Exception {
    }
    
    public void testBogus() {
        // Empty test so that JUnit doesn't complain about there being not tests
    }
    
    public void testCreateGraph() throws Exception {    
        callController();
        assertGraph();
        GraphDefinition firstGraphDef = m_graphDef;
        callController();
        assertGraph();
        assertSame(firstGraphDef, m_graphDef);
    }

    private void callController() throws Exception {
        m_controller.doPerform(null, m_request, m_response);
    }
    
    public void testAddParameter() throws Exception {
        testAddDatasource("ifInOctets");
    }
    
    public void testRemoveParameter() throws Exception {
        testAddDatasource("ifInOctets");
        
        resetRequestResponse();

        testRemoveDatasource("ifInOctets");
        
    }

    private void testRemoveDatasource(String dsName) throws Exception {
        //
        // FIXME: The remove should take just the dataSource id if possible!  Its not
        // clean separation for the controller to know it has to take 'item_' off....
        // It's really a view problem that should be resolved there somehow.  Just
        // need to think of a simple way to do it there
        //
        m_request.addParameter("remove", "item_"+dsName);
        callController();
        assertGraph();
        assertEquals(0, m_datasources.size());
    }

    private void testAddDatasource(String dsName) throws Exception {
        m_request.addParameter("add", dsName);
        
        callController();
        assertGraph();
        assertEquals(1, m_graphDef.getGraphDataElements().size());
        GraphDataElement dataElem = (GraphDataElement) m_graphDef.getGraphDataElements().get(0);
		assertEquals(dsName, dataElem.getDataSource().getId());
    }

    private void assertGraph() {
        Object attribute = m_request.getSession().getAttribute("graph");
        assertNotNull(attribute);
        assertTrue(attribute instanceof GraphDefinition);
        m_graph = (GraphDefinition)attribute;
        assertDatasources();
        Object graphDef = m_request.getSession().getAttribute("graphDef");
        assertNotNull(graphDef);
        assertTrue(graphDef instanceof GraphDefinition);
        m_graphDef = (GraphDefinition)graphDef;
    }

    private void assertDatasources() {
        m_datasources = m_graph.getGraphDataElements();
        assertNotNull(m_datasources);
    }

}
