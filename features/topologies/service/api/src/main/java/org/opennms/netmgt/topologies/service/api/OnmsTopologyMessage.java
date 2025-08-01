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
package org.opennms.netmgt.topologies.service.api;

import java.util.Objects;

public class OnmsTopologyMessage {

    public static OnmsTopologyMessage update(OnmsTopologyRef messagebody, OnmsTopologyProtocol protocol) {
        Objects.requireNonNull(messagebody);
        Objects.requireNonNull(protocol);
        return new OnmsTopologyMessage(messagebody, protocol, TopologyMessageStatus.UPDATE);
    }

    public static OnmsTopologyMessage delete(OnmsTopologyRef messagebody, OnmsTopologyProtocol protocol) {
        Objects.requireNonNull(messagebody);
        Objects.requireNonNull(protocol);
        return new OnmsTopologyMessage(messagebody, protocol, TopologyMessageStatus.DELETE);
    }

    public enum TopologyMessageStatus {
        UPDATE,
        DELETE
    }

    private final OnmsTopologyRef m_messagebody;
    private final TopologyMessageStatus m_messagestatus;
    private final OnmsTopologyProtocol m_protocol;
    
    private <T extends OnmsTopologyRef>OnmsTopologyMessage(T messagebody, OnmsTopologyProtocol protocol, TopologyMessageStatus messagestatus) {
        m_messagebody=messagebody;
        m_messagestatus=messagestatus;
        m_protocol = protocol;
    }

    public OnmsTopologyRef getMessagebody() {
        return m_messagebody;
    }

    public TopologyMessageStatus getMessagestatus() {
        return m_messagestatus;
    }

    public OnmsTopologyProtocol getProtocol() {
        return m_protocol;
    }

}
