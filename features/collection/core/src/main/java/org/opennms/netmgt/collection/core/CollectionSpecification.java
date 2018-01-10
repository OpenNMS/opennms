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

package org.opennms.netmgt.collection.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.opennms.core.rpc.api.RpcExceptionHandler;
import org.opennms.core.rpc.api.RpcExceptionUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionFailed;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionInstrumentation;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.CollectionUnknown;
import org.opennms.netmgt.collection.api.LocationAwareCollectorClient;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.ServiceParameters.ParameterName;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.rrd.RrdRepository;
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

    private Package m_package;
    private final String m_svcName;
    private final ServiceCollector m_collector;
    private Map<String, Object> m_parameters;
    private final CollectionInstrumentation m_instrumentation;
    private final LocationAwareCollectorClient m_locationAwareCollectorClient;

    public CollectionSpecification(Package wpkg, String svcName, ServiceCollector collector, CollectionInstrumentation instrumentation, LocationAwareCollectorClient locationAwareCollectorClient) {
        m_package = Objects.requireNonNull(wpkg);
        m_svcName = Objects.requireNonNull(svcName);
        m_collector = Objects.requireNonNull(collector);
        m_instrumentation = Objects.requireNonNull(instrumentation);
        m_locationAwareCollectorClient = Objects.requireNonNull(locationAwareCollectorClient);

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
        return m_package.getStoreByIfAlias();
    }

    private String ifAliasComment() {
        return m_package.getIfAliasComment();
    }

    private String storeFlagOverride() {
        return m_package.getStorFlagOverride();
    }

    private String ifAliasDomain() {
        return m_package.getIfAliasDomain();
    }

    private String storeByNodeId() {
        return m_package.getStoreByNodeID();
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

    private void setPackage(Package pkg) {
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
    public ServiceParameters getServiceParameters() {
        return new ServiceParameters(Collections.unmodifiableMap(m_parameters));
    }

    private boolean isTrue(String stg) {
        return stg.equalsIgnoreCase("yes") || stg.equalsIgnoreCase("on") || stg.equalsIgnoreCase("true");
    }

    private boolean isFalse(String stg) {
        return stg.equalsIgnoreCase("no") || stg.equalsIgnoreCase("off") || stg.equalsIgnoreCase("false");
    }

    private void initializeParameters() {
    	final Map<String, Object> m = new TreeMap<String, Object>();
        m.put(ParameterName.SERVICE.toString(), m_svcName);
        m.put(ParameterName.SERVICE_INTERVAL.toString(), getService().getInterval().toString());
        StringBuilder sb;
        Collection<Parameter> params = getService().getParameters();
        for (Parameter p : params) {
            if (LOG.isDebugEnabled()) {
                sb = new StringBuilder();
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
                sb = new StringBuilder();
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
        m.put("packageName", m_package.getName());
        m_parameters = m;
    }

    /**
     * <p>initialize</p>
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     */
    public void initialize(CollectionAgent agent) throws CollectionInitializationException {
        m_instrumentation.beginCollectorInitialize(m_package.getName(), agent.getNodeId(), agent.getHostAddress(), m_svcName);
        try {
            m_collector.validateAgent(agent, getPropertyMap());
        } finally {
            m_instrumentation.endCollectorInitialize(m_package.getName(), agent.getNodeId(), agent.getHostAddress(), m_svcName);
        }
    }

    /**
     * <p>release</p>
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     */
    public void release(CollectionAgent agent) {
        m_instrumentation.beginCollectorRelease(m_package.getName(), agent.getNodeId(), agent.getHostAddress(), m_svcName);
        m_instrumentation.endCollectorRelease(m_package.getName(), agent.getNodeId(), agent.getHostAddress(), m_svcName);
    }

    /**
     * <p>collect</p>
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     * @return a {@link org.opennms.netmgt.collection.api.CollectionSet} object.
     * @throws org.opennms.netmgt.collection.api.CollectionException if any.
     */
    public CollectionSet collect(CollectionAgent agent) throws CollectionException {
        m_instrumentation.beginCollectorCollect(m_package.getName(), agent.getNodeId(), agent.getHostAddress(), m_svcName);
        try {
            final CollectionSet set = m_locationAwareCollectorClient.collect()
                .withAgent(agent)
                .withAttributes(getPropertyMap())
                .withCollector(getCollector())
                // Use the service interval as the TTL
                .withTimeToLive(getService().getInterval())
                .execute()
                .get();

            // There are collector implementations that never throw an exception just return a collection failed
            if (CollectionStatus.FAILED.equals(set.getStatus())) {
                m_instrumentation.reportCollectionException(m_package.getName(), agent.getNodeId(), agent.getHostAddress(), m_svcName, new CollectionFailed(CollectionStatus.FAILED));
            }
            return set;
        } catch (InterruptedException|ExecutionException e) {
            final CollectionException ce = RpcExceptionUtils.handleException(e, new RpcExceptionHandler<CollectionException>() {
                @Override
                public CollectionException onInterrupted(Throwable t) {
                    return new CollectionUnknown("Interrupted.", t);
                }

                @Override
                public CollectionException onTimedOut(Throwable t) {
                    return new CollectionUnknown("Request timed out.", t);
                }

                @Override
                public CollectionException onRejected(Throwable t) {
                    return new CollectionUnknown("Request rejected.", e);
                }

                @Override
                public CollectionException onUnknown(Throwable t) {
                    if (t instanceof CollectionException) {
                        return (CollectionException)t;
                    } else if (t.getCause() != null && t.getCause() instanceof CollectionException) {
                        return (CollectionException)t.getCause();
                    }
                    return new CollectionException("Collection failed.", t);
                }
            });
            m_instrumentation.reportCollectionException(m_package.getName(), agent.getNodeId(), agent.getHostAddress(), m_svcName, ce);
            throw ce;
        } finally {
            m_instrumentation.endCollectorCollect(m_package.getName(), agent.getNodeId(), agent.getHostAddress(), m_svcName);
        }
    }

    /**
     * <p>scheduledOutage</p>
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
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
        for (String outageName : m_package.getOutageCalendars()) {
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
    public void refresh(CollectdConfigFactory collectorConfigDao) {
        Package refreshedPackage = collectorConfigDao.getPackage(getPackageName());
        if (refreshedPackage != null) {
            setPackage(refreshedPackage);
        }
    }

    /**
     * <p>getRrdRepository</p>
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    public RrdRepository getRrdRepository(String collectionName) {
        return m_collector.getRrdRepository(collectionName);
    }
}
