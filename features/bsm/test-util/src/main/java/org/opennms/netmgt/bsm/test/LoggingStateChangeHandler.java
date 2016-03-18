/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.test;

import java.util.List;

import org.opennms.netmgt.bsm.service.BusinessServiceStateChangeHandler;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;

import com.google.common.collect.Lists;

public class LoggingStateChangeHandler implements BusinessServiceStateChangeHandler {

    public class StateChange {
        private final BusinessService m_businessService;
        private final Status m_newStatus;
        private final Status m_prevStatus;

        public StateChange(BusinessService businessService, Status newStatus, Status prevStatus) {
            m_businessService = businessService;
            m_newStatus = newStatus;
            m_prevStatus = prevStatus;
        }

        public BusinessService getBusinessService() {
            return m_businessService;
        }

        public Status getNewSeverity() {
            return m_newStatus;
        }

        public Status getPrevSeverity() {
            return m_prevStatus;
        }
    }

    private final List<StateChange> m_stateChanges = Lists.newArrayList();

    @Override
    public void handleBusinessServiceStateChanged(BusinessService businessService, Status newStatus, Status prevStatus) {
        m_stateChanges.add(new StateChange(businessService, newStatus, prevStatus));
    }

    public List<StateChange> getStateChanges() {
        return m_stateChanges;
    }
}
