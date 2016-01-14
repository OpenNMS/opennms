/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.internal;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import org.junit.Test;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceStateChangeHandler;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;

public class DefaultBusinessServiceStateMachineTest {

    /* TODO: FIXME: HACK: JW, MVR
    @Test
    public void canMaintainState() {
        String explicitReductionKey = "explicitReductionKey";

        // Create a simple hierarchy
        OnmsMonitoredService svc1 = createService(1, "192.168.1.1", "ICMP");
        BusinessServiceEntity bs1 = new BusinessServiceEntity();
        bs1.getIpServices().add(svc1);
        bs1.setName("BS1");
        bs1.setReductionFunction(new MostCritical());
 
        BusinessServiceEntity bs2 = new BusinessServiceEntity();
        bs2.setName("BS2");
        List<BusinessServiceEntity> bss = Lists.newArrayList(bs1, bs2);

        // Setup the state machine
        LoggingStateChangeHandler handler = new LoggingStateChangeHandler();
        DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setBusinessServices(bss);
        stateMachine.addHandler(handler, null);

        // Verify the initial state
        assertEquals(0, handler.getStateChanges().size());
        assertEquals(DefaultBusinessServiceStateMachine.DEFAULT_SEVERITY, stateMachine.getOperationalStatus(bs1));
        assertEquals(DefaultBusinessServiceStateMachine.DEFAULT_SEVERITY, stateMachine.getOperationalStatus(bs2));

        // Now createBusinessService an alarm
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setUei(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
        alarm.setSeverity(OnmsSeverity.MINOR);
        alarm.setReductionKey(String.format("%s::1:192.168.1.1:ICMP", EventConstants.NODE_LOST_SERVICE_EVENT_UEI));

        // Pass the alarm to the state machine
        stateMachine.handleNewOrUpdatedAlarm(alarm);

        // Verify the updated state
        assertEquals(1, handler.getStateChanges().size());
        assertEquals(OnmsSeverity.MINOR, stateMachine.getOperationalStatus(svc1));
        assertEquals(OnmsSeverity.MINOR, stateMachine.getOperationalStatus(bs1));
        assertEquals(DefaultBusinessServiceStateMachine.DEFAULT_SEVERITY, stateMachine.getOperationalStatus(bs2));

        // Now create an alarm matching the explicit reductionKey
        alarm = new OnmsAlarm();
        alarm.setUei(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
        alarm.setSeverity(OnmsSeverity.MAJOR);
        alarm.setReductionKey(explicitReductionKey);

        // Pass the alarm to the state machine
        stateMachine.handleNewOrUpdatedAlarm(alarm);

        // Verify the updated state regarding the explicit reductionKey
        assertEquals(2, handler.getStateChanges().size());
        assertEquals(OnmsSeverity.MAJOR, stateMachine.getOperationalStatus(bs1));
    }
    */

    private static OnmsMonitoredService createService(final int nodeId, final String ipAddress, final String serviceName) {
        return new OnmsMonitoredService() {
            private static final long serialVersionUID = 8510675581667310365L;

            public Integer getNodeId() {
                return nodeId;
            }

            public InetAddress getIpAddress() {
                try {
                    return InetAddress.getByName(ipAddress);
                } catch (UnknownHostException e) {
                    throw Throwables.propagate(e);
                }
            }

            public String getServiceName() {
                return serviceName;
            }

            public String toString() {
                return getServiceName();
            }
        };
    }

    public static class LoggingStateChangeHandler implements BusinessServiceStateChangeHandler {
        
        public static class StateChange {
            private final BusinessServiceEntity m_businessService;
            private final OnmsSeverity m_newSeverity;
            private final OnmsSeverity m_prevSeverity;

            public StateChange(BusinessServiceEntity businessService, OnmsSeverity newSeverity, OnmsSeverity prevSeverity) {
                m_businessService = businessService;
                m_newSeverity = newSeverity;
                m_prevSeverity = prevSeverity;
            }

            public BusinessServiceEntity getBusinessService() {
                return m_businessService;
            }

            public OnmsSeverity getNewSeverity() {
                return m_newSeverity;
            }

            public OnmsSeverity getPrevSeverity() {
                return m_prevSeverity;
            }
        }

        private final List<StateChange> m_stateChanges = Lists.newArrayList();

        @Override
        public void handleBusinessServiceStateChanged(BusinessServiceEntity businessService, OnmsSeverity newSeverity,
                                                      OnmsSeverity prevSeverity) {
            m_stateChanges.add(new StateChange(businessService, newSeverity, prevSeverity));
        }

        public List<StateChange> getStateChanges() {
            return m_stateChanges;
        }
    }
}
