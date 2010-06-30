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
// Modifications:
//
// 2007 Jul 23: Organize imports, use Java 5 generics and remove unused code to eliminate warnings. - dj@opennms.org
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
package org.opennms.secret.web;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.secret.model.DataSource;
import org.opennms.secret.model.InterfaceService;
import org.opennms.secret.model.InterfaceServiceDataSource;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeDataSources;
import org.opennms.secret.model.NodeInterface;
import org.opennms.secret.model.NodeInterfaceDataSources;
import org.opennms.secret.service.DataSourceService;
import org.opennms.secret.service.NodeInterfaceService;
import org.opennms.secret.service.NodeService;
import org.opennms.secret.service.ServiceService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

//public class NodeController implements ThrowawayController {
/**
 * <p>NodeController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class NodeController implements Controller {
    private Long nodeId = new Long(0); // XXX This is hard-coded.  It shouldn't be. ;-)
    private NodeService m_nodeService;
    private NodeInterfaceService m_nodeInterfaceService;
    private ServiceService m_serviceService;
    private DataSourceService m_dataSourceService;
    private String m_viewName;

    /** Constant <code>MODEL_NAME="node"</code> */
    public static final String MODEL_NAME = "node";

//	public ModelAndView execute() throws Exception {

    /** {@inheritDoc} */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Node n = m_nodeService.getNodeById(nodeId);
        
        NodeDataSources node = new NodeDataSources();
        node.setNode(n);

        List dataSources = m_dataSourceService.getDataSourcesByNode(node.getNode());
        node.setDataSources(dataSources);
        
        HashSet interfaces = m_nodeInterfaceService.getInterfaces(node.getNode());
        HashSet<NodeInterfaceDataSources> newInterfaces = new HashSet<NodeInterfaceDataSources>();
        for (Iterator i = interfaces.iterator(); i.hasNext(); ) {
            NodeInterface ni = (NodeInterface) i.next();
            NodeInterfaceDataSources iface = new NodeInterfaceDataSources();
            iface.setNodeInterface(ni);
            
            dataSources = m_dataSourceService.getDataSourcesByInterface(iface.getNodeInterface());
            iface.setDataSources(dataSources);

            Set services = m_serviceService.getServices(iface.getNodeInterface());
            Set<InterfaceServiceDataSource> newServices = new HashSet<InterfaceServiceDataSource>();
            for (Iterator j = services.iterator(); j.hasNext(); ) {
                InterfaceService is = (InterfaceService) j.next();
                InterfaceServiceDataSource service = new InterfaceServiceDataSource();
                service.setInterfaceService(is);
                
                DataSource ds = m_dataSourceService.getDataSourceByService(service.getInterfaceService());
                service.setDataSource(ds);
            
                newServices.add(service);
            }
            iface.setServices(newServices);

            newInterfaces.add(iface);
        }
        node.setInterfaces(newInterfaces);
            
        
        return new ModelAndView(m_viewName, MODEL_NAME, node);
	}

    /**
     * <p>setNodeService</p>
     *
     * @param nodeService a {@link org.opennms.secret.service.NodeService} object.
     */
    public void setNodeService(NodeService nodeService) {
        m_nodeService = nodeService;
    }

    /**
     * <p>setNodeInterfaceService</p>
     *
     * @param nodeInterfaceService a {@link org.opennms.secret.service.NodeInterfaceService} object.
     */
    public void setNodeInterfaceService(NodeInterfaceService nodeInterfaceService) {
        m_nodeInterfaceService = nodeInterfaceService;
    }
    
    /**
     * <p>setServiceService</p>
     *
     * @param serviceService a {@link org.opennms.secret.service.ServiceService} object.
     */
    public void setServiceService(ServiceService serviceService) {
        m_serviceService = serviceService;
    }

    /**
     * <p>setDataSourceService</p>
     *
     * @param dataSourceService a {@link org.opennms.secret.service.DataSourceService} object.
     */
    public void setDataSourceService(DataSourceService dataSourceService) {
        m_dataSourceService = dataSourceService;
    }
	
	/**
	 * <p>Setter for the field <code>nodeId</code>.</p>
	 *
	 * @param id a {@link java.lang.Long} object.
	 */
	public void setNodeId(Long id) {
		nodeId = id;
	}
    
    /**
     * <p>setViewName</p>
     *
     * @param viewName a {@link java.lang.String} object.
     */
    public void setViewName(String viewName) {
        m_viewName = viewName;
    }
}
