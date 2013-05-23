/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import org.opennms.netmgt.provision.persist.NodeProvisionService;
import org.opennms.web.servlet.MissingParameterException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Node list controller.
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeProvisioningController extends AbstractController implements
        InitializingBean {
    private NodeProvisionService m_nodeProvisionService;
    
    private String m_successView;
    private String m_redirectView;

    /**
     * {@inheritDoc}
     *
     * Acknowledge the alarms specified in the POST and then redirect the
     * client to an appropriate URL for display.
     */
    @Transactional
    @Override
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        String action        = request.getParameter("actionCode");
        String redirectParms = request.getParameter("redirectParms");
        String redirect      = request.getParameter("redirect");

        if (action == null || !action.equals("add")) {
            ModelAndView modelAndView = m_nodeProvisionService.getModelAndView(request);
            modelAndView.setViewName(m_successView);
            return modelAndView;
        } else {
            String[] required = new String[] { "foreignSource", "nodeLabel", "ipAddress" };

            for (String key : required) {
                String[] value = request.getParameterValues(key);
                if (value == null || value.length == 0) {
                    throw new MissingParameterException(key, required);
                }
            }

            String foreignSource  = request.getParameter("foreignSource");
            if (m_nodeProvisionService.provisionNode(
                user,
                foreignSource,
                String.valueOf(System.currentTimeMillis()),
                request.getParameter("nodeLabel"),
                request.getParameter("ipAddress"),
                request.getParameterValues("category"),
                request.getParameter("community"),
                request.getParameter("snmpVersion"),
                request.getParameter("deviceUsername"),
                request.getParameter("devicePassword"),
                request.getParameter("enablePassword"),
                request.getParameter("accessMethod"),
                request.getParameter("autoEnable"),
                request.getParameter("noSNMP")
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

    /**
     * <p>setRedirectView</p>
     *
     * @param redirectView a {@link java.lang.String} object.
     */
    public void setRedirectView(String redirectView) {
        m_redirectView = redirectView;
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
     * <p>setNodeProvisionService</p>
     *
     * @param nodeProvisionService a {@link org.opennms.netmgt.provision.persist.NodeProvisionService} object.
     */
    public void setNodeProvisionService(NodeProvisionService nodeProvisionService) {
        m_nodeProvisionService = nodeProvisionService;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_redirectView, "redirectView must be set");
        Assert.notNull(m_successView, "successView must be set");
        Assert.notNull(m_nodeProvisionService, "nodeProvisionService must be set");
    }

}
