/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.xbill.DNS.Name;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.Type;
import org.xbill.DNS.Update;


/**
 * A Dynamic DNS provisioning adapter for integration with OpenNMS Provisioning daemon API.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class ReverseDnsProvisioningAdapter extends SimpleQueuedProvisioningAdapter implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(ReverseDnsProvisioningAdapter.class);
    
    /*
     * A read-only DAO will be set by the Provisioning Daemon.
     */
    private EventForwarder m_eventForwarder;
    private Resolver m_resolver = null;
    private String m_signature;    
    private ReverseDnsProvisioningAdapterService m_reverseDnsProvisioningAdapterService;
    
    private static final String MESSAGE_PREFIX = "Dynamic Reverse DNS provisioning failed: ";
    private static final String ADAPTER_NAME="Reverse DNS Provisioning Adapter";
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {        
        String dnsServer = System.getProperty("importer.adapter.dns.server");
        if (!StringUtils.isBlank(dnsServer)) {
            LOG.info("DNS property found: {}", dnsServer);
            if (dnsServer.contains(":")) {
                final String[] serverAddress = dnsServer.split(":");
                m_resolver = new SimpleResolver(serverAddress[0]);
                m_resolver.setPort(Integer.valueOf(serverAddress[1]));
            } else {
                m_resolver = new SimpleResolver(dnsServer);
            }
    
            // Doesn't work for some reason, haven't figured out why yet
            String key = System.getProperty("importer.adapter.dns.privatekey");
            if (key != null && key.length() > 0) {
                m_signature = key;
                m_resolver.setTSIGKey(TSIG.fromString(m_signature));
            }
        } else {
            LOG.warn("no DNS server configured, ReverseDnsProvisioningAdapter will not do anything!");
        }
    }
    
    public ReverseDnsProvisioningAdapterService getReverseProvisioningAdapterService() {
        return m_reverseDnsProvisioningAdapterService;
    }

    public void setReverseDnsProvisioningAdapterService(
            ReverseDnsProvisioningAdapterService reverseProvisioningAdapterService) {
        m_reverseDnsProvisioningAdapterService = reverseProvisioningAdapterService;
    }

    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.events.api.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventForwarder} object.
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return ADAPTER_NAME;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNodeReady(AdapterOperation op) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void processPendingOperationForNode(final AdapterOperation op) throws ProvisioningAdapterException {
        if (m_resolver == null) {
            return;
        }
        LOG.info("processPendingOperationForNode: Handling Operation: {}", op);
        if (op.getType() == AdapterOperationType.ADD || op.getType() == AdapterOperationType.UPDATE) {
            doUpdate(op);
        } else if (op.getType() == AdapterOperationType.DELETE) {
            //do nothing in this adapter
        } else if (op.getType() == AdapterOperationType.CONFIG_CHANGE) {
            //do nothing in this adapter
        } else {
            LOG.warn("unknown operation: {}", op.getType());
        }
    }

    private void doUpdate(AdapterOperation op) {
        LOG.debug("doUpdate: operation: {}", op.getType().name());
        for (ReverseDnsRecord record : m_reverseDnsProvisioningAdapterService.get(op.getNodeId()) ) {
            LOG.debug("doUpdate: ReverseDnsRecord: hostname: {} zone: {} ip address: {}", record.getIp().getHostAddress(), record.getHostname(), record.getZone());
            try {
                Update update = new Update(Name.fromString(record.getZone()));
                Name ptrRecord=ReverseMap.fromAddress(record.getIp());
                update.replace(ptrRecord, Type.PTR, 3600, record.getHostname());
                m_resolver.send(update);
                m_reverseDnsProvisioningAdapterService.update(op.getNodeId(),record);
            } catch (Exception e) {
                LOG.error("updateNode: Error handling updated event.", e);
                sendAndThrow(op.getNodeId(), e);
            }
        }
    }
    
    private void sendAndThrow(int nodeId, Exception e) {
        Event event = buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED, nodeId).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent();
        m_eventForwarder.sendNow(event);
        throw new ProvisioningAdapterException(MESSAGE_PREFIX, e);
    }

    private EventBuilder buildEvent(String uei, int nodeId) {
        EventBuilder builder = new EventBuilder(uei, "Provisioner", new Date());
        builder.setNodeid(nodeId);
        return builder;
    }
}
