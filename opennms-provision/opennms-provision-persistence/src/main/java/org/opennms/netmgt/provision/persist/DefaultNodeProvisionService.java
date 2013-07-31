/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import javax.servlet.http.HttpServletRequest;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.TransactionAwareEventForwarder;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>DefaultNodeProvisionService class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultNodeProvisionService implements NodeProvisionService, InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultNodeProvisionService.class);
    
    private EventForwarder m_eventForwarder;
    
    @Autowired
    private CategoryDao m_categoryDao;
    
    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    private ForeignSourceRepository m_foreignSourceRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /** {@inheritDoc} */
    @Override
    public ModelAndView getModelAndView(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("foreignSources", m_foreignSourceRepository.getForeignSources());
        modelAndView.addObject("requisitions", m_foreignSourceRepository.getRequisitions());
        modelAndView.addObject("categories", m_categoryDao.getAllCategoryNames());
        modelAndView.addObject("success", Boolean.parseBoolean(request.getParameter("success")));
        modelAndView.addObject("foreignSource", request.getParameter("foreignSource"));
        return modelAndView;
    }
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public boolean provisionNode(final String user, String foreignSource, String foreignId, String nodeLabel, String ipAddress,
            String[] categories, String snmpCommunity, String snmpVersion,
            String deviceUsername, String devicePassword, String enablePassword,
            String accessMethod, String autoEnable, String noSNMP) throws NodeProvisionException {

        LOG.debug("adding SNMP community {} ({})", snmpCommunity, snmpVersion);
        // Set the SNMP community name (if necessary)
        if (noSNMP == null &&  snmpCommunity != null && !snmpCommunity.equals("") && snmpVersion != null && !snmpVersion.equals("")) {
            try {
                SnmpEventInfo info = new SnmpEventInfo();
                info.setCommunityString(snmpCommunity);
                info.setFirstIPAddress(ipAddress);
                info.setVersion(snmpVersion);
                m_snmpPeerFactory.define(info);
                SnmpPeerFactory.saveCurrent();
            } catch (Throwable e) {
                throw new NodeProvisionException("unable to add SNMP community information", e);
            }
        }

        LOG.debug("creating requisition node");
        // Create a requisition node based on the form input
        RequisitionInterface reqIface = new RequisitionInterface();
        reqIface.setIpAddr(ipAddress);
        reqIface.setManaged(true);
        reqIface.setSnmpPrimary(PrimaryType.get("P"));
        reqIface.setStatus(1);

        reqIface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
        if(noSNMP == null) {
            reqIface.putMonitoredService(new RequisitionMonitoredService("SNMP"));
        }
        
        RequisitionNode reqNode = new RequisitionNode();
        reqNode.setNodeLabel(nodeLabel);
        reqNode.setForeignId(foreignId);
        reqNode.putInterface(reqIface);

        for (String category : categories) {
            if (category != null && !category.equals("")) {
                reqNode.putCategory(new RequisitionCategory(category));
            }
        }

        if (deviceUsername != null && !deviceUsername.equals("")) {
            reqNode.putAsset(new RequisitionAsset("username", deviceUsername));
        }
        if (devicePassword != null && !devicePassword.equals("")) {
            reqNode.putAsset(new RequisitionAsset("password", devicePassword));
        }
        if (enablePassword != null && !enablePassword.equals("")) {
            reqNode.putAsset(new RequisitionAsset("enable", enablePassword));
        }
        if (accessMethod != null && !accessMethod.equals("")) {
            reqNode.putAsset(new RequisitionAsset("connection", accessMethod));
        }
        if (autoEnable != null) {
            reqNode.putAsset(new RequisitionAsset("autoenable", "A"));
        }

        // Now save it to the requisition
        try {
            Requisition req = m_foreignSourceRepository.getRequisition(foreignSource);
            req.putNode(reqNode);
            LOG.debug("saving requisition node");
            m_foreignSourceRepository.save(req);
        } catch (ForeignSourceRepositoryException e) {
            throw new RuntimeException("unable to retrieve foreign source '" + foreignSource + "'", e);
        }

        Event e = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, "NodeProvisionService")
            .addParam("url", m_foreignSourceRepository.getRequisitionURL(foreignSource).toString())
            .getEvent();
        m_eventForwarder.sendNow(e);

        LOG.warn("about to return ({})", System.currentTimeMillis());
        return true;
    }
    
    /**
     * <p>setForeignSourceRepository</p>
     *
     * @param repository a {@link org.opennms.netmgt.provision.persist.ForeignSourceRepository} object.
     */
    public void setForeignSourceRepository(ForeignSourceRepository repository) {
        m_foreignSourceRepository = repository;
    }

    /**
     * <p>setEventProxy</p>
     *
     * @param proxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
     * @throws java.lang.Exception if any.
     */
    public void setEventProxy(final EventProxy proxy) throws Exception {
        EventForwarder proxyForwarder = new EventForwarder() {
            @Override
            public void sendNow(Event event) {
                try {
                    proxy.send(event);
                } catch (EventProxyException e) {
                    throw new NodeProvisionException("Unable to send "+event, e);
                }
            }

            @Override
            public void sendNow(Log eventLog) {
                try {
                    proxy.send(eventLog);
                } catch (EventProxyException e) {
                    throw new NodeProvisionException("Unable to send eventLog "+eventLog, e);
                }
            }
            
        };
        m_eventForwarder = new TransactionAwareEventForwarder(proxyForwarder);
    }
}
