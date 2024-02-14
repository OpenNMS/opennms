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


/**
 * This class holds the service information and list of outages for the service.
 *
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 */
public class Service extends StandardNamedObject {
    /**
     * List of outages.
     */
    private OutageSvcTimesList m_outageList;

    /**
     * Percentage Availability during regular hours.
     */
    private double m_percentAvail;

    /**
     * Percentage Availability during business hours.
     */
    private double m_percentBusAvail;

    /**
     * DownTime during regular hours.
     */
    private long m_downTime;

    /**
     * DownTime during business hours.
     */
    private long m_busDownTime;

    /**
     * Total Regular Monitored Time
     */
    private long m_monitoredTime;

    /**
     * Total Monitored Time during business hours.
     */
    private long m_monitoredBusTime;

    /**
     * Default Constructor.
     */
    public Service() {
        m_outageList = new OutageSvcTimesList();
    }

    /**
     * <p>Constructor for Service.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public Service(String name) {
        setName(name);
        m_outageList = new OutageSvcTimesList();
    }

    /**
     * Constructor that sets the name and the outages.
     *
     * @param name
     *            Name of the service.
     * @param outages
     *            Outages to be set for this service.
     */
    public Service(String name, OutageSvcTimesList outages) {
        setName(name);
        if (outages != null)
            m_outageList = outages;
        else
            m_outageList = new OutageSvcTimesList();
    }

    /**
     * Constructor that sets the outages.
     *
     * @param outages
     *            Outages for this service to be set.
     */
    public Service(OutageSvcTimesList outages) {
        if (outages != null)
            m_outageList = outages;
    }

    /**
     * Returns the outage time for this service.
     *
     * @return a double.
     */
    public double getDownTime() {
        return m_downTime;
    }

    /**
     * Returns the outage time for this service during business hours.
     *
     * @return a long.
     */
    public long getBusDownTime() {
        return m_busDownTime;
    }

    /**
     * Returns the percentage Availability.
     *
     * @return a double.
     */
    public double getPercentAvail() {
        return m_percentAvail;
    }

    /**
     * Returns the percentage Availability for this service during business
     * hours.
     *
     * @return a double.
     */
    public double getBusPercentAvail() {
        return m_percentBusAvail;
    }

    /**
     * Returns the percentage Availability for this service during business
     * hours.
     *
     * @return a long.
     */
    public long getMonitoredTime() {
        return m_monitoredTime;
    }

    /**
     * Returns the monitored time for this service during business hours.
     *
     * @return a long.
     */
    public long getMonitoredBusTime() {
        return m_monitoredBusTime;
    }

    /**
     * Return the outages
     *
     * @return outages Outages to be set.
     */
    public OutageSvcTimesList getOutages() {
        return m_outageList;
    }

    /**
     * Added outage.
     *
     * @param lost a long.
     * @param regained a long.
     */
    public void addOutage(long lost, long regained) {
        if (m_outageList == null)
            m_outageList = new OutageSvcTimesList();
        m_outageList.addSvcTime(lost, regained);
    }

    /**
     * Added outage.
     *
     * @param lost a long.
     */
    public void addOutage(long lost) {
        if (m_outageList == null)
            m_outageList = new OutageSvcTimesList();
        m_outageList.addSvcTime(lost);
    }

    /**
     * Adds a lost time / regained time combination for the node.
     *
     * @param outage a {@link org.opennms.reporting.datablock.Outage} object.
     */
    public void addOutage(Outage outage) {
        if (m_outageList == null)
            m_outageList = new OutageSvcTimesList();
        m_outageList.addSvcTime(outage.getLostTime(), outage.getRegainedTime());
    }

    /**
     * Return the outage for this service.
     *
     * @param currentTime a long.
     * @param rollingWindow a long.
     * @return a long.
     */
    public long getDownTime(long currentTime, long rollingWindow) {
        if (m_outageList != null)
            return m_outageList.getDownTime(currentTime, rollingWindow);
        return 0;
    }

    /**
     * Returns the Percentage Availability for the service
     *
     * @param currentTime
     *            Time at the end of the Rolling Window.
     * @param rollingWindow
     *            Actual Monitored Time.
     * @return Percentage Availability
     */
    public double getPercentAvail(long currentTime, long rollingWindow) {
        m_downTime = getDownTime(currentTime, rollingWindow);
        double outage = 1.0 * m_downTime;
        double denom = 1.0 * rollingWindow;
        double percent = 100.0 * (1.0 - outage / denom);
        m_percentAvail = percent;
        m_monitoredTime = rollingWindow;
        return percent;
    }

    /**
     * {@inheritDoc}
     *
     * Equals method.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            if (obj instanceof String)
                return ((String) obj).equals(getName());
            else if (obj instanceof Service)
                return obj == this;
        }
        return false;
    }
}
