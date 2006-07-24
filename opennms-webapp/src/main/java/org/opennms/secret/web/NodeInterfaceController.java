package org.opennms.secret.web;

import java.util.HashSet;

import org.opennms.secret.model.Node;
import org.opennms.secret.service.NodeInterfaceService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.throwaway.ThrowawayController;

public class NodeInterfaceController implements ThrowawayController {

	public static final String IF_VIEW = "ifView";
	public static final String MODEL_NAME = "interfaces";
	private Node node;
	private NodeInterfaceService m_nodeInterfaceService;
	
	public ModelAndView execute() throws Exception {
		HashSet interfaces = m_nodeInterfaceService.getInterfaces(node);
        return new ModelAndView(IF_VIEW, MODEL_NAME, interfaces);
	}

	public void setNodeInterfaceService(NodeInterfaceService nodeInterfaceService) {
		m_nodeInterfaceService = nodeInterfaceService;
	}
	
}
