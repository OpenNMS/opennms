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
import static org.opennms.netmgt.bsm.test.BsmTestUtils.createAlarm;
import static org.opennms.netmgt.bsm.test.BsmTestUtils.createSimpleHierarchy;

import java.util.List;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import org.junit.Test;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceStateChangeHandler;
import org.opennms.netmgt.bsm.test.BsmTestData;
import org.opennms.netmgt.bsm.test.BsmTestUtils;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.collect.Lists;

public class DefaultBusinessServiceStateMachineTest {

    /* TODO: FIXME: HACK: JW, MVR
    @Test
    public void canMaintainState() {
        BsmTestData testSpecification = createSimpleHierarchy();
        List<BusinessServiceEntity> bss = testSpecification.getServices();

        // Setup the state machine
        LoggingStateChangeHandler handler = new LoggingStateChangeHandler();
        DefaultBusinessServiceStateMachine stateMachine = new DefaultBusinessServiceStateMachine();
        stateMachine.setBusinessServices(bss);
        stateMachine.addHandler(handler, null);

        // Verify the initial state
        assertEquals(0, handler.getStateChanges().size());
        for (BusinessServiceEntity eachBs : bss) {
            assertEquals(DefaultBusinessServiceStateMachine.DEFAULT_SEVERITY, stateMachine.getOperationalStatus(eachBs));
        }

        BusinessServiceEntity bsChild1 = testSpecification.findByName("Child 1");
        BusinessServiceEntity bsChild2 = testSpecification.findByName("Child 2");
        BusinessServiceEntity bsParent = testSpecification.findByName("Parent");
        OnmsMonitoredService svc1 = testSpecification.findIpService("192.168.1.1", "ICMP");
        OnmsMonitoredService svc2 = testSpecification.findIpService("192.168.1.2", "SNMP");

        // Pass the alarm to the state machine
        stateMachine.handleNewOrUpdatedAlarm(createAlarm(svc1, OnmsSeverity.MINOR));

        // Verify the updated state
        assertEquals(2, handler.getStateChanges().size());
        assertEquals(OnmsSeverity.MINOR, stateMachine.getOperationalStatus(svc1));
        assertEquals(OnmsSeverity.MINOR, stateMachine.getOperationalStatus(bsChild1));
        assertEquals(DefaultBusinessServiceStateMachine.DEFAULT_SEVERITY, stateMachine.getOperationalStatus(svc2));
        assertEquals(DefaultBusinessServiceStateMachine.DEFAULT_SEVERITY, stateMachine.getOperationalStatus(bsChild2));
        assertEquals(OnmsSeverity.MINOR, stateMachine.getOperationalStatus(bsParent));

        // Verify that hierarchy also works
        stateMachine.handleNewOrUpdatedAlarm(BsmTestUtils.createAlarm(svc2, OnmsSeverity.MAJOR));
        assertEquals(4, handler.getStateChanges().size());
        assertEquals(OnmsSeverity.MINOR, stateMachine.getOperationalStatus(svc1));
        assertEquals(OnmsSeverity.MINOR, stateMachine.getOperationalStatus(bsChild1));
        assertEquals(OnmsSeverity.MAJOR, stateMachine.getOperationalStatus(svc2));
        assertEquals(OnmsSeverity.MAJOR, stateMachine.getOperationalStatus(bsChild2));
        assertEquals(OnmsSeverity.MAJOR, stateMachine.getOperationalStatus(bsParent));
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
