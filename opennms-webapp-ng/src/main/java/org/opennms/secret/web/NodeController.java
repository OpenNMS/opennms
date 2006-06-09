package org.opennms.secret.web;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import org.springframework.web.servlet.mvc.throwaway.ThrowawayController;

//public class NodeController implements ThrowawayController {
public class NodeController implements Controller {
    private static final String s_dataSourcesSessionAttribute = "dataSources";

    private Long nodeId = new Long(0); // XXX This is hard-coded.  It shouldn't be. ;-)
    private NodeService m_nodeService;
    private NodeInterfaceService m_nodeInterfaceService;
    private ServiceService m_serviceService;
    private DataSourceService m_dataSourceService;
    private String m_viewName;

    public static final String MODEL_NAME = "node";

//	public ModelAndView execute() throws Exception {

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Node n = m_nodeService.getNodeById(nodeId);
        
        NodeDataSources node = new NodeDataSources();
        node.setNode(n);

        List dataSources = m_dataSourceService.getDataSourcesByNode(node.getNode());
        node.setDataSources(dataSources);
        
        HashSet interfaces = m_nodeInterfaceService.getInterfaces(node.getNode());
        HashSet newInterfaces = new HashSet();
        for (Iterator i = interfaces.iterator(); i.hasNext(); ) {
            NodeInterface ni = (NodeInterface) i.next();
            NodeInterfaceDataSources iface = new NodeInterfaceDataSources();
            iface.setNodeInterface(ni);
            
            dataSources = m_dataSourceService.getDataSourcesByInterface(iface.getNodeInterface());
            iface.setDataSources(dataSources);

            HashSet services = m_serviceService.getServices(iface.getNodeInterface());
            HashSet newServices = new HashSet();
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

    public void setNodeService(NodeService nodeService) {
        m_nodeService = nodeService;
    }

    public void setNodeInterfaceService(NodeInterfaceService nodeInterfaceService) {
        m_nodeInterfaceService = nodeInterfaceService;
    }
    
    public void setServiceService(ServiceService serviceService) {
        m_serviceService = serviceService;
    }

    public void setDataSourceService(DataSourceService dataSourceService) {
        m_dataSourceService = dataSourceService;
    }
	
	public void setNodeId(Long id) {
		nodeId = id;
	}
    
    public void setViewName(String viewName) {
        m_viewName = viewName;
    }
}
