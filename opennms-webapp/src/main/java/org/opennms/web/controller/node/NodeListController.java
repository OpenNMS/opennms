/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.command.NodeListCommand;
import org.opennms.web.svclayer.NodeListService;
import org.opennms.web.svclayer.support.NodeListModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * Node list controller.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class NodeListController extends AbstractCommandController implements InitializingBean {

    private String m_successView;
    private NodeListService m_nodeListService;
    
    /** {@inheritDoc} */
    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object cmd, BindException errors) throws Exception {
        NodeListCommand command = (NodeListCommand) cmd;
    
        NodeListModel model = m_nodeListService.createNodeList(command);
        ModelAndView modelAndView = new ModelAndView(getSuccessView(), "model", model);
        modelAndView.addObject(getCommandName(), command);
        return modelAndView;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_successView != null, "successView property cannot be null");
        Assert.state(m_nodeListService != null, "nodeListService property cannot be null");
    }
    
    /**
     * <p>getSuccessView</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSuccessView() {
        return m_successView;
    }

    /**
     * <p>setSuccessView</p>
     *
     * @param successView a {@link java.lang.String} object.
     */
    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    /**
     * <p>getNodeListService</p>
     *
     * @return a {@link org.opennms.web.svclayer.NodeListService} object.
     */
    public NodeListService getNodeListService() {
        return m_nodeListService;
    }

    /**
     * <p>setNodeListService</p>
     *
     * @param nodeListService a {@link org.opennms.web.svclayer.NodeListService} object.
     */
    public void setNodeListService(NodeListService nodeListService) {
        m_nodeListService = nodeListService;
    }
}
