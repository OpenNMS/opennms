/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.AbstractTask;
import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.Callback;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.core.utils.IPLike;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>IpInterfaceScan class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class IpInterfaceScan implements RunInBatch {
    private static final Logger LOG = LoggerFactory.getLogger(IpInterfaceScan.class);

    private final ProvisionService m_provisionService;
    private final InetAddress m_address;
    private final Integer m_nodeId;
    private final String m_foreignSource;
    private final OnmsMonitoringLocation m_location;

    /**
     * <p>Constructor for IpInterfaceScan.</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param address a {@link java.net.InetAddress} object.
     * @param foreignSource a {@link java.lang.String} object.
     * @param location a {@link org.opennms.netmgt.model.monitoringLocation.OnmsMonitoringLocation} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     */
    public IpInterfaceScan(final Integer nodeId, final InetAddress address, final String foreignSource, final OnmsMonitoringLocation location, final ProvisionService provisionService) {
        m_nodeId = nodeId;
        m_address = address;
        m_foreignSource = foreignSource;
        m_location = location;
        m_provisionService = provisionService;
    }

    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignSource() {
        return m_foreignSource;
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getNodeId() {
        return m_nodeId;
    }

    public OnmsMonitoringLocation getLocation() {
        return m_location;
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_address;
    }

    /**
     * <p>getProvisionService</p>
     *
     * @return a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     */
    public ProvisionService getProvisionService() {
        return m_provisionService;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("address", m_address)
                .append("foreign source", m_foreignSource)
                .append("node ID", m_nodeId)
                .append("location", m_location != null ? m_location.getLocationName() : null)
                .toString();
    }

    /**
     * <p>servicePersister</p>
     * 
     * @param currentPhase a {@link org.opennms.core.tasks.BatchTask} object.
     * @param serviceName a {@link java.lang.String} object.
     * @return a {@link org.opennms.core.tasks.Callback} object.
     */
    public static Callback<Boolean> servicePersister(final BatchTask currentPhase, final ProvisionService service, final PluginConfig detectorConfig, final int nodeId, final InetAddress address) {
        return new Callback<Boolean>() {
            @Override
            public void accept(final Boolean serviceDetected) {
                final String hostAddress = str(address);
                final String serviceName = detectorConfig.getName();
                LOG.info("Attempted to detect service {} on address {}: {}", serviceName, hostAddress, serviceDetected);
                if (serviceDetected) {

                    /*
                     * TODO: Convert this sequence into a chain of CompletableFutures 
                     */
                    currentPhase.getBuilder().addSequence(
                            new RunInBatch() {
                                @Override
                                public void run(final BatchTask batch) {
                                    if ("SNMP".equals(serviceName)) {
                                        service.setIsPrimaryFlag(nodeId, hostAddress);
                                    }
                                }
                            },
                            new RunInBatch() {
                                @Override
                                public void run(final BatchTask batch) {
                                    service.addMonitoredService(nodeId, hostAddress, serviceName);
                                }
                            },
                            new RunInBatch() {
                                @Override
                                public void run(final BatchTask batch) {
                                    // NMS-3906
                                    service.updateMonitoredServiceState(nodeId, hostAddress, serviceName);
                                }
                            });
                }
            }

            @Override
            public Boolean apply(final Throwable t) {
                LOG.info("Exception occurred while trying to detect service {} on address {}", detectorConfig.getName(), str(address), t);
                return false;
            }
        };
    }

    protected static AbstractTask createDetectorTask(final BatchTask currentPhase, final ProvisionService service, final PluginConfig detectorConfig, final int nodeId, final InetAddress address, final OnmsMonitoringLocation location) {
        return currentPhase.getCoordinator().createTask(currentPhase, new DetectorRunner(service, detectorConfig, nodeId, address, location), servicePersister(currentPhase, service, detectorConfig, nodeId, address));
    }

    /** {@inheritDoc} */
    @Override
    public void run(final BatchTask currentPhase) {
        // This call returns a collection of new ServiceDetector instances
        final Collection<PluginConfig> detectorConfigs = getProvisionService().getDetectorsForForeignSource(getForeignSource() == null ? "default" : getForeignSource());

        LOG.info("Detecting services for node {}/{} on address {}: found {} detectors", getNodeId(), getForeignSource(), str(getAddress()), detectorConfigs.size());

        for (final PluginConfig detectorConfig : detectorConfigs) {
            if (shouldDetect(detectorConfig, getAddress())) {
                currentPhase.add(createDetectorTask(currentPhase, getProvisionService(), detectorConfig, getNodeId(), getAddress(), getLocation()));
            }
        }
    }

    protected static boolean shouldDetect(final PluginConfig detectorConfig, final InetAddress address) {
        String ipMatch = detectorConfig.getParameter("ipMatch");
        if (ipMatch  == null || ipMatch.trim().isEmpty()) return true; // Execute the detector if the ipMatch is not provided.
        // Regular Expression Matching
        if (ipMatch.startsWith("~")) {
            return address.getHostAddress().matches(ipMatch.substring(1));
        }
        // Expression based IPLIKE Matching
        return isIpMatching(address, ipMatch);
    }

    protected static boolean isIpMatching(final InetAddress ip, final String expr) {
        try {
            JexlEngine parser = new JexlEngine();
            Expression e = parser.createExpression(generateExpr(expr));
            final Map<String,Object> context = new HashMap<String,Object>();
            context.put("iplike", IPLike.class);
            context.put("ipaddr", ip.getHostAddress());
            Boolean out = (Boolean) e.evaluate(new MapContext(context));
            return out;
        } catch (Exception e) {
            LOG.error("Can't process rule '{}' while checking IP {}.", expr, ip, e);
            return false;
        }
    }

    protected static String generateExpr(final String basicExpr) {
        LOG.debug("generateExpr: original expression {}", basicExpr);
        String data = basicExpr;
        Pattern p = Pattern.compile("[0-9a-f:.,\\-*]+");
        Matcher m = p.matcher(data);
        while (m.find()) {
            data = data.replace(m.group(), "iplike.matches(ipaddr,'" + m.group() + "')");
        }
        LOG.debug("generateExpr: computed expression {}", data);
        return data;
    }

}
