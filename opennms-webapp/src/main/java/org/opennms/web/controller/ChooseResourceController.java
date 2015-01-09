/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.svclayer.ChooseResourceService;
import org.opennms.web.svclayer.support.ChooseResourceModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * <p>ChooseResourceController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ChooseResourceController extends AbstractController implements InitializingBean {
    private ChooseResourceService m_chooseResourceService;
    private String m_defaultEndUrl;
    private NodeDao m_nodeDao;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] requiredParameters = new String[] { "parentResourceId or", "parentResourceType and parentResource" };

        String endUrl = WebSecurityUtils.sanitizeString(request.getParameter("endUrl"));

        String resourceId = WebSecurityUtils.sanitizeString(request.getParameter("parentResourceId"));
        if (resourceId == null) {
            String resourceType = WebSecurityUtils.sanitizeString(request.getParameter("parentResourceType"));
            String resource = WebSecurityUtils.sanitizeString(request.getParameter("parentResource"));
            boolean isStoreByForeignSource = ResourceTypeUtils.isStoreByForeignSource();
            if (request.getParameter("parentResourceType") == null) {
                throw new MissingParameterException("parentResourceType", requiredParameters);
            }
            if (request.getParameter("parentResource") == null) {
                throw new MissingParameterException("parentResource", requiredParameters);
            }
            if (resourceType.equals("node") && isStoreByForeignSource) {
                OnmsNode node = m_nodeDao.get(resource);
                if (node != null && node.getForeignSource() != null && node.getForeignId() != null) {
                    resourceType = "nodeSource";
                    resource = node.getForeignSource() + ':' + node.getForeignId();
                }
            }
            if (resourceType.equals("nodeSource") && !isStoreByForeignSource) {
                OnmsNode node = m_nodeDao.get(resource);
                if (node != null && node.getForeignSource() != null && node.getForeignId() != null) {
                    resourceType = "node";
                    resource = node.getId().toString();
                }
            }
            resourceId = OnmsResource.createResourceId(resourceType, resource);
        }
        
        if (endUrl == null || "".equals(endUrl)) {
            endUrl = m_defaultEndUrl;
        }

        ChooseResourceModel model = 
            m_chooseResourceService.findChildResources(resourceId,
                                                       endUrl);
        
        return new ModelAndView("/graph/chooseresource",
                                "model",
                                model);
    }
    
    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        if (m_chooseResourceService == null) {
            throw new IllegalStateException("chooseResourceService property not set");
        }
        
        if (m_defaultEndUrl == null) {
            throw new IllegalStateException("defaultEndUrl property not set");
        }

        if (m_nodeDao == null) {
            throw new IllegalStateException("nodeDao property not set");
        }
    }

    /**
     * <p>getChooseResourceService</p>
     *
     * @return a {@link org.opennms.web.svclayer.ChooseResourceService} object.
     */
    public ChooseResourceService getChooseResourceService() {
        return m_chooseResourceService;
    }

    /**
     * <p>setChooseResourceService</p>
     *
     * @param chooseResourceService a {@link org.opennms.web.svclayer.ChooseResourceService} object.
     */
    public void setChooseResourceService(
            ChooseResourceService chooseResourceService) {
        m_chooseResourceService = chooseResourceService;
    }

    /**
     * <p>getDefaultEndUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultEndUrl() {
        return m_defaultEndUrl;
    }

    /**
     * <p>setDefaultEndUrl</p>
     *
     * @param defaultEndUrl a {@link java.lang.String} object.
     */
    public void setDefaultEndUrl(String defaultEndUrl) {
        m_defaultEndUrl = defaultEndUrl;
    }

    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        this.m_nodeDao = nodeDao;
    }

}
