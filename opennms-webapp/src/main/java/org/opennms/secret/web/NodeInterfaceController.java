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

import org.opennms.secret.model.Node;
import org.opennms.secret.service.NodeInterfaceService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.throwaway.ThrowawayController;

/**
 * <p>NodeInterfaceController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class NodeInterfaceController implements ThrowawayController {

	/** Constant <code>IF_VIEW="ifView"</code> */
	public static final String IF_VIEW = "ifView";
	/** Constant <code>MODEL_NAME="interfaces"</code> */
	public static final String MODEL_NAME = "interfaces";
	private Node node;
	private NodeInterfaceService m_nodeInterfaceService;
	
	/**
	 * <p>execute</p>
	 *
	 * @return a {@link org.springframework.web.servlet.ModelAndView} object.
	 * @throws java.lang.Exception if any.
	 */
	public ModelAndView execute() throws Exception {
		HashSet interfaces = m_nodeInterfaceService.getInterfaces(node);
        return new ModelAndView(IF_VIEW, MODEL_NAME, interfaces);
	}

	/**
	 * <p>setNodeInterfaceService</p>
	 *
	 * @param nodeInterfaceService a {@link org.opennms.secret.service.NodeInterfaceService} object.
	 */
	public void setNodeInterfaceService(NodeInterfaceService nodeInterfaceService) {
		m_nodeInterfaceService = nodeInterfaceService;
	}
	
}
