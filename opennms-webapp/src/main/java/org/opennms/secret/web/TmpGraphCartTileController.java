//
// $Id: TmpGraphCartTileController.java,v 1.2 2005/11/28 14:52:11 devjam Exp $
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
// Modifications:
//
// 2007 Jul 23: Organize imports and comment-out unused methods to eliminate warnings. - dj@opennms.org
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

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.tiles.ComponentContext;
import org.opennms.secret.model.DataSource;
import org.opennms.secret.model.GraphDataElement;
import org.opennms.secret.model.GraphDataLine;
import org.opennms.secret.model.GraphDefinition;
import org.springframework.web.servlet.view.tiles.ComponentControllerSupport;


/**
 * <p>TmpGraphCartTileController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class TmpGraphCartTileController extends ComponentControllerSupport {
    private static final String s_sessionAttribute = "graphDef";

    /** {@inheritDoc} */
    protected void doPerform(ComponentContext componentContext, HttpServletRequest request, HttpServletResponse response) throws Exception {
        GraphDefinition graphDef = getGraphDef(request);
        DataSource ds = new DataSource();
        ds.setId(request.getParameter("add").substring(5)); // chop off "item_"
        ds.setDataSource(request.getParameter("ds"));
        ds.setSource(request.getParameter("source"));
        
//        Datasources dataSources = getGraphDataSources(graph);
         // Need to get the data source that mtaches the ID given   
        removeDataSource(graphDef, ds);
        addDataSource(graphDef, ds);
     }

    // FIXME: This is unused
//    private LinkedList getGraphDataSources(GraphDefinition graphDef) {
//        LinkedList gdes = new LinkedList();
//        gdes = graphDef.getGraphDataElements();
////        if (gdes == null) {
////            gdes = new Datasources();
////            graph.setDatasources(dataSources);
////        }
//        return gdes;
//    }

    private void addDataSource(GraphDefinition graphDef, DataSource ds) {
        if (graphDef != null ) {
            
            int i = 0;
            boolean alreadyExists = false;
//            GraphDataElement gde;
            for (Iterator iter = graphDef.getGraphDataElements().iterator(); iter.hasNext();) {
				GraphDataElement gde = (GraphDataElement) iter.next();
				
                org.opennms.secret.model.DataSource dataSource = (org.opennms.secret.model.DataSource) gde.getDataSource();
                
                if (ds.equals(dataSource)) {
                    alreadyExists = true;
                    break;
                }
                i++;
            }
            
            if (!alreadyExists) {	
                GraphDataLine gdl = new GraphDataLine(null);
                gdl.setDataSource(ds);
                graphDef.addGraphDataElement(gdl);
            }
        }
    }

    private void removeDataSource(GraphDefinition graphDef, DataSource ds) {
        if (ds != null) {
            int i = 0;
            for (Iterator iter = graphDef.getGraphDataElements().iterator(); iter.hasNext();) {
				GraphDataElement gde = (GraphDataElement) iter.next();
				
                org.opennms.secret.model.DataSource dataSource = (org.opennms.secret.model.DataSource) gde.getDataSource();
                
                if (ds.equals(dataSource)) {
                    graphDef.removeGraphDataElement(gde);
                    break;
                }
                i++;
            }
        }
    }
    
//    FIXME: These are unused
//    private String getAddedDataSource(HttpServletRequest request) {
//        return (String)request.getParameter("add");
//    }
//
//    private String getRemovedDataSource(HttpServletRequest request) {
//        return (String)request.getParameter("remove");
//    }

	private GraphDefinition getGraphDef(HttpServletRequest request) {
	    HttpSession session = request.getSession();
            
	    Object o = session.getAttribute(s_sessionAttribute);
	    if (o == null || !(o instanceof GraphDefinition)) {
	        session.setAttribute(s_sessionAttribute, new GraphDefinition());
        }
	    return (GraphDefinition) session.getAttribute(s_sessionAttribute);
    }
}
