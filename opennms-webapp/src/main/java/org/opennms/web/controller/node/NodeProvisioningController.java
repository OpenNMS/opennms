/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.web.controller.node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.provision.persist.NodeProvisionService;
import org.opennms.web.MissingParameterException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Node list controller.
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
public class NodeProvisioningController extends AbstractController implements
        InitializingBean {
    private static final long serialVersionUID = 1L;

    private NodeProvisionService m_nodeProvisionService;
    
    private String m_successView;
    private String m_redirectView;

    /**
     * Acknowledge the alarms specified in the POST and then redirect the
     * client to an appropriate URL for display.
     */
    @Transactional
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String action        = request.getParameter("actionCode");
        String redirectParms = request.getParameter("redirectParms");
        String redirect      = request.getParameter("redirect");

        if (action == null || !action.equals("add")) {
            ModelAndView modelAndView = m_nodeProvisionService.getModelAndView(request);
            modelAndView.setViewName(m_successView);
            return modelAndView;
        } else {
            String[] required = new String[] { "foreignSource", "nodeLabel", "ipAddress", "category" };

            for (String key : required) {
                String[] value = request.getParameterValues(key);
                if (value == null || value.length == 0) {
                    throw new MissingParameterException(key, required);
                }
            }

            String foreignSource  = request.getParameter("foreignSource");
            if (m_nodeProvisionService.provisionNode(
                foreignSource,
                String.valueOf(System.currentTimeMillis()),
                request.getParameter("nodeLabel"),
                request.getParameter("ipAddress"),
                request.getParameterValues("category"),
                request.getParameter("community"),
                request.getParameter("snmpVersion"),
                request.getParameter("deviceUsername"),
                request.getParameter("devicePassword"),
                request.getParameter("enablePassword")
                )) {
                redirectParms = "success=true&foreignSource=" + foreignSource;
            }
        }

        String viewName;
        if (redirect != null) {
            viewName = redirect;
        } else {
            viewName = (redirectParms == null || redirectParms == "" || redirectParms == "null" ? m_redirectView : m_redirectView + "?" + redirectParms);
        }
        RedirectView view = new RedirectView(viewName, true);
        return new ModelAndView(view);

    }

    public void setRedirectView(String redirectView) {
        m_redirectView = redirectView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    public void setNodeProvisionService(NodeProvisionService nodeProvisionService) {
        m_nodeProvisionService = nodeProvisionService;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_redirectView, "redirectView must be set");
        Assert.notNull(m_successView, "successView must be set");
        Assert.notNull(m_nodeProvisionService, "nodeProvisionService must be set");
    }

}
