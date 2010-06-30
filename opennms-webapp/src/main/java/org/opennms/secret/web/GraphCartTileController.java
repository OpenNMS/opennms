//
// $Id: GraphCartTileController.java,v 1.13 2005/12/22 22:10:21 devjam Exp $
//

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

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.tiles.ComponentContext;
import org.opennms.secret.model.DataSource;
import org.opennms.secret.model.GraphDataElement;
import org.opennms.secret.model.GraphDataLine;
import org.opennms.secret.model.GraphDefinition;
import org.opennms.secret.service.DataSourceService;
import org.springframework.web.servlet.view.tiles.ComponentControllerSupport;


/**
 * <p>GraphCartTileController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class GraphCartTileController extends ComponentControllerSupport {

//    private static final String s_graphSessionAttribute = "graph";
    private static final String s_graphDefSessionAttribute = "graphDef";
    
    private static final String[] s_colors = { "0000ff", "00ff00", "ff0000", "ffff00", "ff00ff",
        "800000", "008000" };

    
    private DataSourceService m_dataSourceService;

    /** {@inheritDoc} */
    protected void doPerform(ComponentContext componentContext, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // XXX  Bleh.  We shouldn't be doing it this way.  It should be wired up like other services.
        m_dataSourceService = (DataSourceService) getApplicationContext().getBean("dataSourceService");

//        Rrd_graph_def graph = getGraph(request);
        GraphDefinition graphDef = getGraphDef(request);
//        Datasources dataSources = getGraphDataSources(graph);
        LinkedList<GraphDataElement> dataSources = getGraphDataSources(graphDef);
        
        if (clearDataSources(request)) {
            dataSources = newDataSources(graphDef);
        }
            
        removeDataSource(dataSources, getRemovedDataSource(request));
        addDataSource(dataSources, request.getSession(), getAddedDataSource(request));
        
        // XXX this is for debugging.... because I have no idea what attributes are in the componentContext
        if (componentContext != null) {
            for (Iterator i = componentContext.getAttributeNames(); i.hasNext(); ) {
                System.out.println("graphcardtilecontroller attribute: " + i.next());
            }
        }
     }

    /*
    private Datasources getGraphDataSources(Rrd_graph_def graph) {
        Datasources dataSources = graph.getDatasources();
        if (dataSources == null) {
            dataSources = newDataSources(graph);
        }
        return dataSources;
    }

    private Datasources newDataSources(Rrd_graph_def graph) {
        Datasources dataSources = new Datasources();
        graph.setDatasources(dataSources);
        
        return dataSources;
    }
    */
    
    private LinkedList<GraphDataElement> getGraphDataSources(GraphDefinition graphDef) {
        LinkedList<GraphDataElement> dataSources = graphDef.getGraphDataElements();
        if (dataSources == null) {
            dataSources = newDataSources(graphDef);
        }
        return dataSources;
    }


    private LinkedList<GraphDataElement> newDataSources(GraphDefinition graphDef) {
        LinkedList<GraphDataElement> dataSources = new LinkedList<GraphDataElement>();
        graphDef.setGraphDataElements(dataSources);
        
        return dataSources;
    }


    /*
    private void addDataSource(Datasources dataSources, String dataSourceId) {
        if (dataSourceId == null ) {
            return; // XXX should we be tossing an error here?
        }
        
        int i = 0;
        boolean alreadyExists = false;
        Def def;
        for (Enumeration e = dataSources.enumerateDef(); e.hasMoreElements();) {
            def = (Def) e.nextElement();
            if (dataSourceId.equals(def.getName())) {
                alreadyExists = true;
                break;
            }
            i++;
        }

        if (!alreadyExists) {
            def = new Def();
            def.setName(dataSourceId);
            dataSources.addDef(def);
        }
    }

    private void removeDataSource(Datasources dataSources, String dataSourceId) {
        if (dataSourceId == null) {
            return; // XXX should we be tossing an error here?
        }
        
        String remove = dataSourceId.substring(5); // chop off "item_"
        int i = 0;
        for (java.util.Enumeration e = dataSources.enumerateDef(); e.hasMoreElements(); ) {
            Def def2 = (Def) e.nextElement();
            if (remove.equals(def2.getName())) {
                dataSources.removeDef(i);
                break;
            }
            i++;
        }
    }
    */
    
    private void addDataSource(LinkedList<GraphDataElement> dataSources, HttpSession session, String dataSourceId) {
        if (m_dataSourceService == null) {
            throw new IllegalStateException("setDataSourceService has never been called to initialize the DataSourceService provider");
        }
        
        if (dataSourceId == null ) {
            return; // XXX should we be tossing an error here?
        }
        
        /*
        Object o = session.getAttribute("dataSources");
        if (o == null || !(o instanceof List)) {
            return; // XXX should we be tossing an error here?
        }
        
        List sources = (List) o;
        DataSource ds = null;
        for (Iterator i = sources.iterator(); i.hasNext(); ) {
            DataSource t = (DataSource) i.next();
            if (dataSourceId.equals(t.getId())) {
                ds = t;
                break;
            }
        }
        */
        
        DataSource ds = m_dataSourceService.getDataSourceById(dataSourceId);
        
        if (ds == null) {
            return; // XXX should we be tossing an error here?
        }
        
        boolean alreadyExists = false;
        GraphDataElement def;
        for (Iterator i = dataSources.iterator(); i.hasNext(); ) {
            def = (GraphDataElement) i.next();
            if (ds.getId().equals(def.getDataSource().getId())) {
                alreadyExists = true;
                break;
            }
        }

        if (!alreadyExists) {
            def = new GraphDataLine(ds, getColor(session), 1);
            def.setLegend(ds.getName());
            dataSources.add(def);
        }
    }
    
    private Color getColor(HttpSession session) {
        int n = 0;
        
        Object o = session.getAttribute("colorIndex");
        if (o != null && (o instanceof Integer)) {
            n = ((Integer) o).intValue();
        }
        
        session.setAttribute("colorIndex", new Integer(n + 1));

        return new Color(Integer.parseInt(s_colors[n % s_colors.length], 16));
    }


    private void removeDataSource(LinkedList dataSources, String dataSourceId) {
        if (dataSourceId == null) {
            return; // XXX should we be tossing an error here?
        }
        
        String remove = dataSourceId.substring(5); // chop off "item_"
        for (Iterator i = dataSources.iterator(); i.hasNext(); ) {
            GraphDataElement def = (GraphDataElement) i.next();
//          System.out.println("test: " + remove + " = " + def.getDataSource().getId() + " ?");
//          System.out.println("test: " + remove + " = " + def.getUniqueID() + " ?");
            if (remove.equals(def.getUniqueID())) {
//            if (remove.equals(def.getDataSource().getId())) {
                i.remove();
                break;
            }
        }
    }
    
    /*
    private Rrd_graph_def getGraph(HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        Object o = session.getAttribute(s_graphSessionAttribute);
        if (o == null || !(o instanceof Rrd_graph_def)) {
            session.setAttribute(s_graphSessionAttribute, new Rrd_graph_def());
        }

        return (Rrd_graph_def) session.getAttribute(s_graphSessionAttribute);
    }
    */
    
    private GraphDefinition getGraphDef(HttpServletRequest request) {
        HttpSession session = request.getSession();
        
        Object o = session.getAttribute(s_graphDefSessionAttribute);
        if (o == null || !(o instanceof GraphDefinition)) {
            session.setAttribute(s_graphDefSessionAttribute, new GraphDefinition());
        }

        return (GraphDefinition) session.getAttribute(s_graphDefSessionAttribute);
    }
    
    private String getAddedDataSource(HttpServletRequest request) {
        return (String)request.getParameter("add");
    }

    private String getRemovedDataSource(HttpServletRequest request) {
        return (String)request.getParameter("remove");
    }
    
    private boolean clearDataSources(HttpServletRequest request) {
        return (request.getParameter("clear") != null);
    }
    
    /**
     * <p>setDataSourceService</p>
     *
     * @param dataSourceService a {@link org.opennms.secret.service.DataSourceService} object.
     */
    public void setDataSourceService(DataSourceService dataSourceService) {
        m_dataSourceService = dataSourceService;
    }

}
