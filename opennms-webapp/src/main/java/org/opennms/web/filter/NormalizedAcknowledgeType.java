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
package org.opennms.web.filter;



public enum NormalizedAcknowledgeType {
    ACKNOWLEDGED("ack"),
    UNACKNOWLEDGED("unack"),
    BOTH("both");

    private final String shortName;

    private NormalizedAcknowledgeType(final String shortName) {
        this.shortName = shortName;
    }

    public static NormalizedAcknowledgeType createFrom(org.opennms.web.event.AcknowledgeType eventAckType) {
        if (eventAckType == null) return null;
        return NormalizedAcknowledgeType.valueOf(eventAckType.name());
    }

    public static NormalizedAcknowledgeType createFrom(org.opennms.web.alarm.AcknowledgeType alarmAckType) {
        if (alarmAckType == null) return null;
        return NormalizedAcknowledgeType.valueOf(alarmAckType.name());
    }

    public String getShortName() {
        return shortName;
    }
}
