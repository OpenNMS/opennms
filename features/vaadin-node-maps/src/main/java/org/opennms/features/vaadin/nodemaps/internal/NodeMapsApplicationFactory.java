/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.web.api.OnmsHeaderProvider;
import org.ops4j.pax.vaadin.AbstractApplicationFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.vaadin.Application;

/**
 * A factory for creating NodeMapsApplication objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class NodeMapsApplicationFactory extends AbstractApplicationFactory {
    private static final Logger LOG = LoggerFactory.getLogger(NodeMapsApplicationFactory.class);
    private NodeDao m_nodeDao;
    private AssetRecordDao m_assetDao;
    private AlarmDao m_alarmDao;
    private GeocoderService m_geocoder;
    private TransactionOperations m_transaction;
    private OnmsHeaderProvider m_headerProvider;

    /*
     * (non-Javadoc)
     * @see
     * org.ops4j.pax.vaadin.ApplicationFactory#createApplication(javax.servlet
     * .http.HttpServletRequest)
     */
    @Override
    public Application createApplication(final HttpServletRequest request) throws ServletException {
        if (m_nodeDao == null) {
            throw new RuntimeException("m_nodeDao cannot be null.");
        }
        final NodeMapsApplication app = new NodeMapsApplication();
        app.setNodeDao(m_nodeDao);
        app.setAssetRecordDao(m_assetDao);
        app.setAlarmDao(m_alarmDao);
        app.setGeocoderService(m_geocoder);
        app.setTransactionOperations(m_transaction);
        
        if (m_headerProvider != null) {
            try {
                app.setHeaderHtml(m_headerProvider.getHeaderHtml(request));
            } catch (final Exception e) {
                LOG.warn("failed to get header HTML for request {}", request.getPathInfo(), e);
            }
        }
        return app;
    }

    /*
     * (non-Javadoc)
     * @see org.ops4j.pax.vaadin.ApplicationFactory#getApplicationClass()
     */
    @Override
    public Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
        return NodeMapsApplication.class;
    }

    /**
     * Sets the OpenNMS Node DAO.
     * 
     * @param m_nodeDao
     *            the new OpenNMS Node DAO
     */
    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setAssetDao(final AssetRecordDao assetDao) {
        m_assetDao = assetDao;
    }

    public void setAlarmDao(final AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public void setGeocoderService(final GeocoderService geocoderService) {
        m_geocoder = geocoderService;
    }
    
    public void setTransactionOperations(final TransactionOperations tx) {
        m_transaction = tx;
    }
    
    public void setHeaderProvider(final OnmsHeaderProvider headerProvider) {
        m_headerProvider = headerProvider;
    }
}
