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

import static org.opennms.netmgt.provision.service.ProvisionService.DETECTOR_NAME;
import static org.opennms.netmgt.provision.service.ProvisionService.ERROR;
import static org.opennms.netmgt.provision.service.ProvisionService.IP_ADDRESS;
import static org.opennms.netmgt.provision.service.ProvisionService.LOCATION;

import java.net.InetAddress;
import java.util.stream.Collectors;

import org.opennms.core.tasks.Async;
import org.opennms.core.tasks.Callback;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LocationUtils;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.foreignsource.PluginParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.Span;

class DetectorRunner implements Async<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(DetectorRunner.class);

    private final ProvisionService m_service;
    private final PluginConfig m_detectorConfig;
    private final Integer m_nodeId;
    private final InetAddress m_address;
    private final OnmsMonitoringLocation m_location;
    private final Span m_parentSpan;
    private Span m_span;

    public DetectorRunner(ProvisionService service, PluginConfig detectorConfig, Integer nodeId, InetAddress address,
            OnmsMonitoringLocation location, Span span) {
        m_service = service;
        m_detectorConfig = detectorConfig;
        m_nodeId = nodeId;
        m_address = address;
        m_location = location;
        m_parentSpan = span;
    }

    /** {@inheritDoc} */
    @Override
    public void supplyAsyncThenAccept(final Callback<Boolean> cb) {
        try {
            LOG.info("Attemping to detect service {} on address {} at location {}", m_detectorConfig.getName(),
                    getHostAddress(), getLocationName());
            // Launch the detector
            if(!LocationUtils.isDefaultLocationName(getLocationName())) {
                startSpan();
            }
            m_service.getLocationAwareDetectorClient().detect().withClassName(m_detectorConfig.getPluginClass())
                    .withAddress(m_address).withNodeId(m_nodeId).withLocation(getLocationName())
                    .withAttributes(m_detectorConfig.getParameters().stream()
                            .collect(Collectors.toMap(PluginParameter::getKey, PluginParameter::getValue)))
                    .withParentSpan(m_span)
                    .withPreDetectCallback(this::startSpan)
                    .execute()
                    // After completion, run the callback
                    .whenComplete((res, ex) -> {
                        LOG.info("Completed detector execution for service {} on address {} at location {}",
                                m_detectorConfig.getName(), getHostAddress(), getLocationName());
                        if (ex != null) {
                            cb.handleException(ex);
                            if(m_span != null) {
                                m_span.log(ex.getMessage());
                                m_span.setTag(ERROR, true);
                            }
                        } else {
                            cb.accept(res);
                        }
                        if(m_span != null) {
                            m_span.finish();
                        }
                    });
        } catch (Throwable e) {
            cb.handleException(e);
        }
    }

    private void startSpan() {
        if (m_parentSpan != null) {
            m_span = m_service.buildAndStartSpan(m_detectorConfig.getName() + "-DetectRunner", m_parentSpan.context());
            m_span.setTag(DETECTOR_NAME, m_detectorConfig.getName());
            m_span.setTag(IP_ADDRESS, m_address.getHostAddress());
            m_span.setTag(LOCATION, getLocationName());
        }
    }

    private String getHostAddress() {
        return InetAddressUtils.str(m_address);
    }

    private String getLocationName() {
        return m_location != null ? m_location.getLocationName() : null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format("Run detector %s on address %s", m_detectorConfig.getName(), getHostAddress());
    }


}
