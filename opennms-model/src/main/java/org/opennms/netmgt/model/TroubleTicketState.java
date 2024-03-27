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
package org.opennms.netmgt.model;

/**
 * OpenNMS Trouble Ticket State Enumerations.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dschlenk@convergeone.com">David Schlenk</a>
 */
public enum TroubleTicketState {
    /* KEEP THESE IN ORDER or the DEFAULT ALARM VACUUM QUERIES will BREAK */

    /* TODO: once JPA 2.1+ in use, change things that use this to also use a
     * javax.persistence.AttributeConverter<TroubleTicketState, Integer> that
     * returns TroubleTicketState.getValue() in the converter. That should
     * maintain backwords compatibility to the current default 
     * Enum.toOrdinal() JPA behavior and prevent breaking when reordering items
     * in the Enum. 
     */

    /** Trouble ticket is currently open */
    OPEN(0),

    /** Trouble ticket is being created */
    CREATE_PENDING(1),

    /** Trouble ticket creation has failed */
    CREATE_FAILED(2),

    /** Trouble ticket is pending an update from the remote helpdesk system */
    UPDATE_PENDING(3),

    /** Updating ticket state from the remote helpdesk system failed */
    UPDATE_FAILED(4),

    /** Trouble ticket has been closed */
    CLOSED(5),

    /** Trouble ticket is pending closure in the remote helpdesk system */
    CLOSE_PENDING(6),

    /** An attempt to mark the ticket closed in the remote helpdesk system has failed */
    CLOSE_FAILED(7),

    /** Trouble ticket has been resolved */
    RESOLVED(8),

    /** Trouble ticket is in the process of being marked resolved */
    RESOLVE_PENDING(9),

    /** Resolving ticket in the remote helpdesk system has failed */
    RESOLVE_FAILED(10),

    /** Trouble ticket has been cancelled */
    CANCELLED(11),

    /** Trouble ticket is in the process of being marked as cancelled */
    CANCEL_PENDING(12),

    /** An attempt to mark the ticket cancelled in the remote helpdesk system has failed */
    CANCEL_FAILED(13);

    private final int m_value;

    TroubleTicketState(int value) {
        m_value = value;
    }

    public int getValue() {
        return this.m_value;
    }

}
