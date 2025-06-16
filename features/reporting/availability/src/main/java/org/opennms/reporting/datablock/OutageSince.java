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
package org.opennms.reporting.datablock;

import java.util.Date;

/**
 * <B>OutageSince </B> holds the temporary storage for data used during
 * availability report generation presented in the report.
 */
public class OutageSince {
    /**
     * Node name
     */
    private String m_nodename;

    /**
     * Down since
     */
    private long m_outTime;

    /**
     * DownTime
     */
    private long m_outage;

    /**
     * Constructor
     *
     * @param nodename
     *            Node Name
     * @param outTime
     *            Start of Outage
     * @param outage
     *            Downtime
     */
    public OutageSince(String nodename, long outTime, long outage) {
        m_nodename = nodename;
        m_outTime = outTime;
        m_outage = outage;
    }

    /**
     * Returns Node name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeName() {
        return m_nodename;
    }

    /**
     * Returns Downtime
     *
     * @return a long.
     */
    public long getOutage() {
        return m_outage;
    }

    /**
     * Returns Down since
     *
     * @return a long.
     */
    public long getOutTime() {
        return m_outTime;
    }

    /**
     * Returns the string format of this object
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return " Node Name: " + m_nodename + " Out Time Since : " + new Date(m_outTime) + " Outage : " + m_outage;
    }
}
