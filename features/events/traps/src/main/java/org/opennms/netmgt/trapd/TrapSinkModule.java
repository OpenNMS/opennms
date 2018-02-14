/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import java.util.Objects;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.xml.AbstractXmlSinkModule;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;

public class TrapSinkModule extends AbstractXmlSinkModule<TrapInformationWrapper, TrapLogDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(TrapSinkModule.class);

    private final TrapdConfig config;

    private OnmsDistPoller distPoller;

    public TrapSinkModule(TrapdConfig trapdConfig, OnmsDistPoller distPoller) {
        super(TrapLogDTO.class);
        this.config = Objects.requireNonNull(trapdConfig);
        this.distPoller = Objects.requireNonNull(distPoller);
    }

    @Override
    public String getId() {
        return "Trap";
    }

    @Override
    public int getNumConsumerThreads() {
        return config.getNumThreads();
    }

    @Override
    public AggregationPolicy<TrapInformationWrapper, TrapLogDTO, TrapLogDTO> getAggregationPolicy() {
        return new AggregationPolicy<TrapInformationWrapper, TrapLogDTO, TrapLogDTO>() {
            @Override
            public int getCompletionSize() {
                return config.getBatchSize();
            }

            @Override
            public int getCompletionIntervalMs() {
                return config.getBatchIntervalMs();
            }

            @Override
            public Object key(TrapInformationWrapper message) {
                return message.getTrapInformation().getTrapAddress();
            }

            @Override
            public TrapLogDTO aggregate(TrapLogDTO accumulator, TrapInformationWrapper newMessage) {
                final TrapInformation trapInfo = newMessage.getTrapInformation();
                if (accumulator == null) { // no log created yet
                    accumulator = new TrapLogDTO(distPoller.getId(), distPoller.getLocation(), TrapUtils.getEffectiveTrapAddress(trapInfo, config.shouldUseAddressFromVarbind()));
                }
                final TrapDTO trapDTO = new TrapDTO(trapInfo);

                // include the raw message, if configured
                if(config.isIncludeRawMessage()) {
                    byte[] rawMessage = convertToRawMessage(trapInfo);
                    if (rawMessage != null) {
                        trapDTO.setRawMessage(convertToRawMessage(trapInfo));
                    }
                }

                accumulator.addMessage(trapDTO);
                return accumulator;
            }

            @Override
            public TrapLogDTO build(TrapLogDTO accumulator) {
                return accumulator;
            }
        };
    }

    @Override
    public AsyncPolicy getAsyncPolicy() {
        return new AsyncPolicy() {
            @Override
            public int getQueueSize() {
                return config.getQueueSize();
            }

            @Override
            public int getNumThreads() {
                return config.getNumThreads();
            }

            @Override
            public boolean isBlockWhenFull() {
                return true;
            }
        };
    }

    /**
     * Converts the {@link TrapInformation} to a raw message.
     * This is only supported for Snmp4J {@link TrapInformation} implementations.
     *
     * @param trapInfo The Snmp4J {@link TrapInformation}
     * @return The bytes representing the raw message, or null if not supported
     */
    private static byte[] convertToRawMessage(TrapInformation trapInfo) {
        // Raw message conversion is not implemented for JoeSnmp, as the usage of that strategy is deprecated
        if (!(trapInfo instanceof Snmp4JTrapNotifier.Snmp4JV1TrapInformation)
                 && !(trapInfo instanceof Snmp4JTrapNotifier.Snmp4JV2TrapInformation)) {
            LOG.warn("Unable to convert TrapInformation of type {} to raw message. " +
                            "Please use {} as snmp strategy to include raw messages",
                    trapInfo.getClass(), Snmp4JStrategy.class);
            return null;
        }

        // Extract PDU
        try {
            PDU pdu = extractPDU(trapInfo);
            if (pdu != null) {
                return Snmp4JUtils.convertPduToBytes(trapInfo.getTrapAddress(), 0, trapInfo.getCommunity(), pdu);
            }
        } catch (Throwable e) {
            LOG.warn("Unable to convert PDU into bytes: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Retreive PDU from SNMP4j {@link TrapInformation}.
     */
    private static PDU extractPDU(TrapInformation trapInfo) {
        if (trapInfo instanceof Snmp4JTrapNotifier.Snmp4JV1TrapInformation) {
            return ((Snmp4JTrapNotifier.Snmp4JV1TrapInformation) trapInfo).getPdu();
        }
        if (trapInfo instanceof Snmp4JTrapNotifier.Snmp4JV2TrapInformation) {
            return ((Snmp4JTrapNotifier.Snmp4JV2TrapInformation) trapInfo).getPdu();
        }
        throw new IllegalArgumentException("Cannot extract PDU from trapInfo of type " + trapInfo.getClass());
    }
}
