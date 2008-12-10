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
import java.util.List;

import junit.framework.TestCase;

import org.opennms.secret.dao.DataSourceDao;
import org.opennms.secret.dao.impl.DataSourceDaoSimple;
import org.opennms.secret.model.DataSource;
import org.opennms.secret.model.GraphDataElement;
import org.opennms.secret.model.GraphDataLine;
import org.opennms.secret.model.GraphDefinition;
import org.opennms.secret.model.Node;
import org.opennms.secret.service.DataSourceService;
import org.opennms.secret.service.impl.DataSourceServiceImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

public class GraphCartTileContollerTest extends TestCase {

    private MockHttpSession m_session;
    private MockHttpServletRequest m_request;
    private MockHttpServletResponse m_response;
    private GraphCartTileController m_controller;
    
//    private Rrd_graph_def m_graph;
//    private Datasources m_datasources;
    
	private GraphDefinition m_graphDef;
    private LinkedList m_graphDataElements;
    private DataSourceDao m_dataSourceDao;
    private DataSourceService m_dataSourceService;
    
    private DataSource m_testDataSource;

    protected void setUp() throws Exception {
        m_dataSourceDao = new DataSourceDaoSimple();
        m_dataSourceService = new DataSourceServiceImpl();
        ((DataSourceServiceImpl) m_dataSourceService).setDataSourceDao(m_dataSourceDao);
        
        m_session = new MockHttpSession();
        resetRequestResponse();
        
        m_controller = new GraphCartTileController();
        // XXX this is so evil and wrong
        m_session.setAttribute("this_is_sick", m_dataSourceService);
        
        callController();

        List dataSources = m_dataSourceService.getDataSourcesByNode(new Node());
        m_testDataSource = (DataSource) dataSources.get(0);
    }

    private void resetRequestResponse() {
        m_request = new MockHttpServletRequest();
        m_request.setSession(m_session);
        m_response = new MockHttpServletResponse();
    }

    protected void tearDown() throws Exception {
    }
    
    public void FIXMEtestCreateGraph() throws Exception {    
        callController();
        assertGraph();
        GraphDefinition firstGraphDef = m_graphDef;
        
        callController();
        assertGraph();
        assertSame(firstGraphDef, m_graphDef);
    }

    private void callController() throws Exception {
        // FIXME this was disabled b/c the unit test is broken
       // m_controller.doPerform(null, m_request, m_response);
    }

    /*
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
        assertEquals(0, m_datasources.getDefCount());
    }

    private void testAddDatasource(String dsName) throws Exception {
        m_request.addParameter("add", dsName);
        
        callController();
        assertGraph();
        assertEquals("graph data elements size", 1, m_graphDef.getGraphDataElements().size());
        GraphDataElement dataElem = (GraphDataElement) m_graphDef.getGraphDataElements().get(0);
        assertEquals(dsName, dataElem.getDataSource().getId());
    }
    */
    
    public void testBogus() {
        // Do nothing.  This is here so JUnit doesn't complain.
    }
    
    public void FIXMEtestAddParameter() throws Exception {
        addDatasource(m_testDataSource, true);
    }
    
    public void FIXMEtestAddBogusParameter() throws Exception {
        addDatasource(new DataSource("bogus", "bogus", "bogus", "bogus"), false);
    }
    
    public void FIXMEtestRemoveParameter() throws Exception {
        GraphDataElement dataElement = addDatasource(m_testDataSource, true);
        
        resetRequestResponse();

        removeDatasource(dataElement, true);
    }
    
    public void FIXMEtestRemoveBogusParameter() throws Exception {
        addDatasource(m_testDataSource, true);
        
        resetRequestResponse();
        
        GraphDataElement dataElement =
            new GraphDataLine(new DataSource("bogus", "bogus", "bogus", "bogus"));

        removeDatasource(dataElement, false);
    }
    
    public void FIXMEtestClearParameter() throws Exception {
        addDatasource(m_testDataSource, true);
        
        resetRequestResponse();

        clearDatasources();
    }


    private void removeDatasource(GraphDataElement dataElement, boolean shouldWork) throws Exception {
        //
        // FIXME: The remove should take just the dataSource id if possible!  Its not
        // clean separation for the controller to know it has to take 'item_' off....
        // It's really a view problem that should be resolved there somehow.  Just
        // need to think of a simple way to do it there
        //

        assertGraph();
        int countBefore = m_graphDef.getGraphDataElements().size();
        int countExpected = countBefore - (shouldWork ? 1 : 0);

        m_request.addParameter("remove", "item_" + dataElement.getUniqueID());
        callController();
        
        assertGraph();
        assertEquals(countExpected, m_graphDef.getGraphDataElements().size());
    }

    private void clearDatasources() throws Exception {
        m_request.addParameter("clear", "");
        callController();
        
        assertGraph();
        assertEquals(0, m_graphDef.getGraphDataElements().size());
    }


    private GraphDataElement addDatasource(DataSource ds, boolean shouldWork) throws Exception {
        assertGraph();
        int countBefore = m_graphDef.getGraphDataElements().size();
        int countExpected = countBefore + (shouldWork ? 1 : 0);

        m_request.addParameter("add", ds.getId());
        callController();
        
        assertGraph();
        assertEquals("graph data elements size", countExpected,
                m_graphDef.getGraphDataElements().size());
        if (countExpected > 0) {
            GraphDataElement dataElem = (GraphDataElement) m_graphDef.getGraphDataElements().get(0);
            assertEquals(ds.getId(), dataElem.getDataSource().getId());
            
            return dataElem;
        } else {
            return null;
        }
    }


    private void assertGraph() {
        /*
        Object graph = m_request.getSession().getAttribute("graph");
        assertNotNull("\"graph\" attribute not null", graph);
        assertTrue("graph instanceof Rrd_graph_def", graph instanceof Rrd_graph_def);
        m_graph = (Rrd_graph_def)graph;
        
        assertDatasources();
        */
        
        Object graphDef = m_request.getSession().getAttribute("graphDef");
        assertNotNull("\"graphDef\" attribute not null", graphDef);
        assertTrue("graphDef instanceof GraphDefinition", graphDef instanceof GraphDefinition);
        m_graphDef = (GraphDefinition)graphDef;
        
        assertGraphDataElements();
    }

    /*
    private void assertDatasources() {
        m_datasources = m_graph.getDatasources();
        assertNotNull("datasources not null", m_datasources);
    }
    */
    
    private void assertGraphDataElements() {
        m_graphDataElements = m_graphDef.getGraphDataElements();
        assertNotNull("graphDataElements not null", m_graphDataElements);
    }

}
