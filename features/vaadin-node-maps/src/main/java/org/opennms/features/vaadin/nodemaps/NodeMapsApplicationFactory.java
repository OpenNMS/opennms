/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.nodemaps;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.opennms.netmgt.dao.NodeDao;
import org.ops4j.pax.vaadin.AbstractApplicationFactory;

import com.vaadin.Application;

/**
 * A factory for creating NodeMapsApplication objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class NodeMapsApplicationFactory extends AbstractApplicationFactory {

    /** The OpenNMS Node DAO. */
    private NodeDao nodeDao;

    /* (non-Javadoc)
     * @see org.ops4j.pax.vaadin.ApplicationFactory#createApplication(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Application createApplication(HttpServletRequest request) throws ServletException {
        if (nodeDao == null)
            throw new RuntimeException("nodeDao cannot be null.");
        NodeMapsApplication app = new NodeMapsApplication();
        app.setNodeDao(nodeDao);
        return app;
    }

    /* (non-Javadoc)
     * @see org.ops4j.pax.vaadin.ApplicationFactory#getApplicationClass()
     */
    @Override
    public Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
        return NodeMapsApplication.class;
    }

    /**
     * Sets the OpenNMS Node DAO.
     *
     * @param nodeDao the new OpenNMS Node DAO
     */
    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

}
