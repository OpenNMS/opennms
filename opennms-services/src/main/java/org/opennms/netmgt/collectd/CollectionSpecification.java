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

package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.dao.api.CollectorConfigDao;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>CollectionSpecification class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class CollectionSpecification {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(CollectionSpecification.class);

    private CollectdPackage m_package;
    private String m_svcName;
    private ServiceCollector m_collector;
    private Map<String, Object> m_parameters;

    /**
     * <p>Constructor for CollectionSpecification.</p>
     *
     * @param wpkg a {@link org.opennms.netmgt.config.CollectdPackage} object.
     * @param svcName a {@link java.lang.String} object.
     * @param collector a {@link org.opennms.netmgt.collectd.ServiceCollector} object.
     */
    public CollectionSpecification(CollectdPackage wpkg, String svcName, ServiceCollector collector) {
        m_package = wpkg;
        m_svcName = svcName;
        m_collector = collector;
        
        initializeParameters();
    }

    /**
     * <p>getPackageName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPackageName() {
        return m_package.getName();
    }

    private String storeByIfAlias() {
        return m_package.storeByIfAlias();
    }

    private String ifAliasComment() {
        return m_package.ifAliasComment();
    }

    private String storeFlagOverride() {
        return m_package.getStorFlagOverride();
    }

    private String ifAliasDomain() {
        return m_package.ifAliasDomain();
    }

    private String storeByNodeId() {
        return m_package.storeByNodeId();
    }

    private Service getService() {
        return m_package.getService(m_svcName);
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_svcName;
    }

    private void setPackage(CollectdPackage pkg) {
        m_package = pkg;
    }

    /**
     * <p>getInterval</p>
     *
     * @return a long.
     */
    public long getInterval() {
        return getService().getInterval();

    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return m_svcName + '/' + m_package.getName();
    }

    private ServiceCollector getCollector() {
        return m_collector;
    }

    private Map<String, Object> getPropertyMap() {
        return m_parameters;
    }

    /**
     * Return a read only instance of the parameters, which consists of the overall service parameters,
     * plus various other Collection specific parameters (e.g. storeByNodeID etc)
     *
     * @return A read only Map instance
     */
    public Map<String, Object> getReadOnlyPropertyMap() {
        return Collections.unmodifiableMap(m_parameters);
    }

    private boolean isTrue(String stg) {
        return stg.equalsIgnoreCase("yes") || stg.equalsIgnoreCase("on") || stg.equalsIgnoreCase("true");
    }

    private boolean isFalse(String stg) {
        return stg.equalsIgnoreCase("no") || stg.equalsIgnoreCase("off") || stg.equalsIgnoreCase("false");
    }

    private void initializeParameters() {
    	final Map<String, Object> m = new TreeMap<String, Object>();
        m.put("SERVICE", m_svcName);
        StringBuffer sb;
        Collection<Parameter> params = getService().getParameterCollection();
        for (Parameter p : params) {
            if (LOG.isDebugEnabled()) {
                sb = new StringBuffer();
                sb.append("initializeParameters: adding service: ");
                sb.append(getServiceName());
                sb.append(" parameter: ");
                sb.append(p.getKey());
                sb.append(" of value ");
                sb.append(p.getValue());
                LOG.debug(sb.toString());
            }
            m.put(p.getKey(), p.getValue());
        }

        if (storeByIfAlias() != null && isTrue(storeByIfAlias())) {
            m.put("storeByIfAlias", "true");
            if (storeByNodeId() != null) {
                if (isTrue(storeByNodeId())) {
                    m.put("storeByNodeID", "true");
                } else if(isFalse(storeByNodeId())) {
                    m.put("storeByNodeID", "false");
                } else {
                    m.put("storeByNodeID", "normal");
                }
            }
            if (ifAliasDomain() != null) {
                m.put("domain", ifAliasDomain());
            } else {
                m.put("domain", getPackageName());
            }
            if (storeFlagOverride() != null && isTrue(storeFlagOverride())) {
                m.put("storFlagOverride", "true");
            }
            m.put("ifAliasComment", ifAliasComment());
            if (LOG.isDebugEnabled()) {
                sb = new StringBuffer();
                sb.append("ifAliasDomain = ");
                sb.append(ifAliasDomain());
                sb.append(", storeByIfAlias = ");
                sb.append(storeByIfAlias());
                sb.append(", storeByNodeID = ");
                sb.append(storeByNodeId());
                sb.append(", storFlagOverride = ");
                sb.append(storeFlagOverride());
                sb.append(", ifAliasComment = ");
                sb.append(ifAliasComment());
                LOG.debug(sb.toString());
            }
        }
        m_parameters = m;
    }

    /**
     * <p>initialize</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public void initialize(CollectionAgent agent) throws CollectionInitializationException {
        Collectd.instrumentation().beginCollectorInitialize(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        try {
            m_collector.initialize(agent, getPropertyMap());
        } finally {
            Collectd.instrumentation().endCollectorInitialize(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        }
    }

    /**
     * <p>release</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public void release(CollectionAgent agent) {
        Collectd.instrumentation().beginCollectorRelease(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        try {
            m_collector.release(agent);
        } finally {
            Collectd.instrumentation().endCollectorRelease(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        }
    }

    /**
     * <p>collect</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a {@link org.opennms.netmgt.config.collector.CollectionSet} object.
     * @throws org.opennms.netmgt.collectd.CollectionException if any.
     */
    public CollectionSet collect(CollectionAgent agent) throws CollectionException {
        Collectd.instrumentation().beginCollectorCollect(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        try {
            return getCollector().collect(agent, EventIpcManagerFactory.getIpcManager(), getPropertyMap());
        } finally {
            Collectd.instrumentation().endCollectorCollect(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        }
    }

    /**
     * <p>scheduledOutage</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a boolean.
     */
    public boolean scheduledOutage(CollectionAgent agent) {
        boolean outageFound = false;

        PollOutagesConfigFactory outageFactory = PollOutagesConfigFactory.getInstance();

        /*
         * Iterate over the outage names defined in the interface's package.
         * For each outage...if the outage contains a calendar entry which
         * applies to the current time and the outage applies to this
         * interface then break and return true. Otherwise process the
         * next outage.
         */ 
        for (String outageName : m_package.getPackage().getOutageCalendarCollection()) {
            // Does the outage apply to the current time?
            if (outageFactory.isCurTimeInOutage(outageName)) {
                // Does the outage apply to this interface?
                if ((outageFactory.isNodeIdInOutage(agent.getNodeId(), outageName)) ||
                        (outageFactory.isInterfaceInOutage(agent.getHostAddress(), outageName)))
                {
                    LOG.debug("scheduledOutage: configured outage '{}' applies, interface {} will not be collected for {}", outageName, agent.getHostAddress(), this);
                    outageFound = true;
                    break;
                }
            }
        }

        return outageFound;
    }

    /**
     * <p>refresh</p>
     *
     * @param collectorConfigDao a {@link org.opennms.netmgt.dao.api.CollectorConfigDao} object.
     */
    public void refresh(CollectorConfigDao collectorConfigDao) {
        CollectdPackage refreshedPackage = collectorConfigDao.getPackage(getPackageName());
        if (refreshedPackage != null) {
            setPackage(refreshedPackage);
        }
    }

    /**
     * <p>getRrdRepository</p>
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public RrdRepository getRrdRepository(String collectionName) {
        return m_collector.getRrdRepository(collectionName);
    }
}
