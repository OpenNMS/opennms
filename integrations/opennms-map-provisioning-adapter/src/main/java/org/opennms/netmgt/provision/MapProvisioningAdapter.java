/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: December 16, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.provision;

import java.util.Date;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.dao.OnmsMapElementDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.springframework.transaction.annotation.Transactional;


/**
 * A Dynamic Map provisioning adapter for integration with OpenNMS Provisoning daemon API.
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 *
 */
public class MapProvisioningAdapter implements ProvisioningAdapter {
    
    private OnmsMapDao m_onmsMapDao;
    private OnmsMapElementDao m_onmsMapElementDao;
    private EventForwarder m_eventForwarder;
    private static final String MESSAGE_PREFIX = "Dynamic Map provisioning failed: ";
    private static final String ADAPTER_NAME="MAP Provisioning Adapter";


    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#addNode(org.opennms.netmgt.model.OnmsNode)
     */
    @Transactional
    public void addNode(int nodeId) throws ProvisioningAdapterException {
        try {
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#updateNode(org.opennms.netmgt.model.OnmsNode)
     */
    @Transactional
    public void updateNode(int nodeId) throws ProvisioningAdapterException {
        try {
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.ProvisioningAdapter#deleteNode(org.opennms.netmgt.model.OnmsNode)
     */
    @Transactional
    public void deleteNode(int nodeId) throws ProvisioningAdapterException {
        try {
        } catch (Exception e) {
            sendAndThrow(nodeId, e);
        }
    }

    public OnmsMapDao getOnmsMapDao() {
        return m_onmsMapDao;
    }

    public void setOnmsMapDao(OnmsMapDao onmsMapDao) {
        m_onmsMapDao = onmsMapDao;
    }

    public OnmsMapElementDao getOnmsMapElementDao() {
        return m_onmsMapElementDao;
    }

    public void setOnmsMapElementDao(OnmsMapElementDao onmsMapElementDao) {
        m_onmsMapElementDao = onmsMapElementDao;
    }

    private void sendAndThrow(int nodeId, Exception e) {
        m_eventForwarder.sendNow(buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED, nodeId).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent());
        throw new ProvisioningAdapterException(MESSAGE_PREFIX, e);
    }

    private EventBuilder buildEvent(String uei, int nodeId) {
        EventBuilder builder = new EventBuilder(uei, "Provisioner", new Date());
        builder.setNodeid(nodeId);
        return builder;
    }
    
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    public String getName() {
        return ADAPTER_NAME;
    }
       
}
