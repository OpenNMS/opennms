/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.bsm.test;

import java.util.List;

import org.opennms.netmgt.bsm.service.BusinessServiceStateChangeHandler;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;

import com.google.common.collect.Lists;

public class LoggingStateChangeHandler implements BusinessServiceStateChangeHandler {

    public class StateChange {
        private final BusinessServiceGraph m_graph;
        private final BusinessService m_businessService;
        private final Status m_newStatus;
        private final Status m_prevStatus;

        public StateChange(BusinessServiceGraph graph, BusinessService businessService, Status newStatus, Status prevStatus) {
            m_graph= graph;
            m_businessService = businessService;
            m_newStatus = newStatus;
            m_prevStatus = prevStatus;
        }

        public BusinessService getBusinessService() {
            return m_businessService;
        }

        public BusinessServiceGraph getGraph() {
            return m_graph;
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
    public void handleBusinessServiceStateChanged(BusinessServiceGraph graph, BusinessService businessService, Status newStatus, Status prevStatus) {
        m_stateChanges.add(new StateChange(graph, businessService, newStatus, prevStatus));
    }

    public List<StateChange> getStateChanges() {
        return m_stateChanges;
    }
}
