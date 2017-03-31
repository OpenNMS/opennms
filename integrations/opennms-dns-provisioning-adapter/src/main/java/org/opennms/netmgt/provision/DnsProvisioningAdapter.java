/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.xbill.DNS.Name;
import org.xbill.DNS.Resolver;
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
public class DnsProvisioningAdapter extends SimpleQueuedProvisioningAdapter implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(DnsProvisioningAdapter.class);
    
    /**
     * A read-only DAO will be set by the Provisioning Daemon.
     */
    private NodeDao m_nodeDao;
    private EventForwarder m_eventForwarder;
    private Resolver m_resolver = null;
    private String m_signature;
    
    private TransactionTemplate m_template;
    
    private static final String MESSAGE_PREFIX = "Dynamic DNS provisioning failed: ";
    private static final String ADAPTER_NAME="DNS Provisioning Adapter";
    private Integer m_level = 0;
    
    private static volatile ConcurrentMap<Integer, DnsRecord> m_nodeDnsRecordMap;

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_nodeDao, "DnsProvisioner requires a NodeDao which is not null.");
        
        //load current nodes into the map
        m_template.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus arg0) {
                createDnsRecordMap();
            }
        });

        String levelString = System.getProperty("importer.adapter.dns.level");
        if (levelString != null) {
        	Integer level = Integer.getInteger(levelString);
        	if (level != null && level.intValue() > 0)
        		m_level = level;
        }
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
            LOG.warn("no DNS server configured, DnsProvisioningAdapter will not do anything!");
        }
    }
    
    private void createDnsRecordMap() {
        List<OnmsNode> nodes = m_nodeDao.findAllProvisionedNodes();
        
        m_nodeDnsRecordMap = new ConcurrentHashMap<Integer, DnsRecord>(nodes.size());
        
        for (OnmsNode onmsNode : nodes) {
            m_nodeDnsRecordMap.putIfAbsent(onmsNode.getId(), new DnsRecord(onmsNode,m_level));
        }
        
    }

    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }
    /**
     * <p>setNodeDao</p>
     *
     * @param dao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao dao) {
        m_nodeDao = dao;
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
            m_template.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus arg0) {
                    doUpdate(op);
                }
            });
        } else if (op.getType() == AdapterOperationType.DELETE) {
            m_template.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus arg0) {
                    doDelete(op);
                }
            });
        } else if (op.getType() == AdapterOperationType.CONFIG_CHANGE) {
            //do nothing in this adapter
        } else {
            LOG.warn("unknown operation: {}", op.getType());
        }
    }

    private void doUpdate(AdapterOperation op) {
        OnmsNode node = null;
        LOG.debug("doUpdate: operation: {}", op.getType().name());
        try {
            node = m_nodeDao.get(op.getNodeId());
            if (node == null) {
            	doDelete(op);
            	return;
            }
            DnsRecord record = new DnsRecord(node,m_level);
            LOG.debug("doUpdate: DnsRecord: hostname: {} zone: {} ip address {}", record.getIp().getHostAddress(), record.getHostname(), record.getZone());
            DnsRecord oldRecord = m_nodeDnsRecordMap.get(Integer.valueOf(node.getId()));

            Update update = new Update(Name.fromString(record.getZone()));

            if (oldRecord != null && oldRecord.getHostname() != record.getHostname()) {
                update.delete(Name.fromString(oldRecord.getHostname()), Type.A);
            }
            update.replace(Name.fromString(record.getHostname()), Type.A, 3600, record.getIp().getHostAddress());
            m_resolver.send(update);

            m_nodeDnsRecordMap.put(Integer.valueOf(op.getNodeId()), record);
        } catch (Throwable e) {
            LOG.error("addNode: Error handling node added event.", e);
            sendAndThrow(op.getNodeId(), e);
        }
    }

    private void doDelete(AdapterOperation op) {
        try {
            DnsRecord record = m_nodeDnsRecordMap.get(Integer.valueOf(op.getNodeId()));

            if (record != null) {
                Update update = new Update(Name.fromString(record.getZone()));
                update.delete(Name.fromString(record.getHostname()), Type.A);
                m_resolver.send(update);

                m_nodeDnsRecordMap.remove(Integer.valueOf(op.getNodeId()));
            }
        } catch (Throwable e) {
            LOG.error("deleteNode: Error handling node deleted event.", e);
            sendAndThrow(op.getNodeId(), e);
        }
    }
    
    private void sendAndThrow(int nodeId, Throwable e) {
        String message = e.getLocalizedMessage() == null ? "" : ": " + e.getLocalizedMessage();
        Event event = buildEvent(
                EventConstants.PROVISIONING_ADAPTER_FAILED,
                nodeId
            ).addParam(
                "reason", 
                MESSAGE_PREFIX + e.getClass().getName() + message
            ).getEvent();
        m_eventForwarder.sendNow(event);
        throw new ProvisioningAdapterException(MESSAGE_PREFIX, e);
    }

    private EventBuilder buildEvent(String uei, int nodeId) {
        EventBuilder builder = new EventBuilder(uei, "Provisioner", new Date());
        builder.setNodeid(nodeId);
        return builder;
    }

    /**
     * <p>setTemplate</p>
     *
     * @param template a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public void setTemplate(TransactionTemplate template) {
        m_template = template;
    }
}
