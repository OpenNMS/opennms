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
package org.opennms.web.svclayer.model;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * <p>RelativeTimePeriod class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class RelativeTimePeriod {
    private static final RelativeTimePeriod[] s_defaultPeriods;
    
    private String m_id = null;
    private String m_name = null;
    private int m_offsetField = Calendar.DATE;
    private int m_offsetAmount = -1;
    
    static {
        s_defaultPeriods = new RelativeTimePeriod[] {
                new RelativeTimePeriod("last_1_hour", "Last Hour", Calendar.HOUR, -1),
                new RelativeTimePeriod("last_2_hour", "Last 2 Hours", Calendar.HOUR, -2),
                new RelativeTimePeriod("last_4_hour", "Last 4 Hours", Calendar.HOUR, -4),
                new RelativeTimePeriod("last_8_hour", "Last 8 Hours", Calendar.HOUR, -8),
                new RelativeTimePeriod("last_12_hour", "Last 12 Hours", Calendar.HOUR, -12),
                new RelativeTimePeriod("lastday", "Last Day", Calendar.DATE,
                                       -1),
                new RelativeTimePeriod("lastweek", "Last Week",
                                       Calendar.DATE, -7),
                new RelativeTimePeriod("lastmonth", "Last Month",
                                       Calendar.DATE, -31),
                new RelativeTimePeriod("lastyear", "Last Year",
                                       Calendar.DATE, -366) };
    }

    public static final RelativeTimePeriod DEFAULT_RELATIVE_TIME_PERIOD = RelativeTimePeriod.getPeriodByIdOrDefault(System.getProperty("org.opennms.web.defaultGraphPeriod", "lastday"));

    /**
     * <p>Constructor for RelativeTimePeriod.</p>
     */
    public RelativeTimePeriod() {
    }

    /**
     * <p>Constructor for RelativeTimePeriod.</p>
     *
     * @param id a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @param offsetField a int.
     * @param offsetAmount a int.
     */
    public RelativeTimePeriod(String id, String name, int offsetField,
                              int offsetAmount) {
        m_id = id;
        m_name = name;
        m_offsetField = offsetField;
        m_offsetAmount = offsetAmount;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getId() {
        return m_id;
    }

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public void setId(String id) {
        m_id = id;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getOffsetField</p>
     *
     * @return a int.
     */
    public int getOffsetField() {
        return m_offsetField;
    }

    /**
     * <p>setOffsetField</p>
     *
     * @param offsetField a int.
     */
    public void setOffsetField(int offsetField) {
        m_offsetField = offsetField;
    }

    /**
     * <p>getOffsetAmount</p>
     *
     * @return a int.
     */
    public int getOffsetAmount() {
        return m_offsetAmount;
    }

    /**
     * <p>setOffsetAmount</p>
     *
     * @param offsetAmount a int.
     */
    public void setOffsetAmount(int offsetAmount) {
        m_offsetAmount = offsetAmount;
    }

    /**
     * <p>getDefaultPeriods</p>
     *
     * @return an array of {@link org.opennms.web.svclayer.model.RelativeTimePeriod} objects.
     */
    public static RelativeTimePeriod[] getDefaultPeriods() {
        return s_defaultPeriods;
    }
    
    /**
     * <p>getPeriodByIdOrDefault</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.model.RelativeTimePeriod} object.
     */
    public static RelativeTimePeriod getPeriodByIdOrDefault(String id) {
        return getPeriodByIdOrDefault(s_defaultPeriods, id, s_defaultPeriods[5]);
    }
    /**
     * <p>getPeriodByIdOrDefault</p>
     *
     * @param periods an array of {@link org.opennms.web.svclayer.model.RelativeTimePeriod} objects.
     * @param id a {@link java.lang.String} object.
     * @param defaultPeriod a {@link org.opennms.web.svclayer.model.RelativeTimePeriod} object.
     * @return a {@link org.opennms.web.svclayer.model.RelativeTimePeriod} object.
     */
    public static RelativeTimePeriod
        getPeriodByIdOrDefault(RelativeTimePeriod[] periods, String id,
                RelativeTimePeriod defaultPeriod) {
        // default to the first time period
        RelativeTimePeriod chosenPeriod = defaultPeriod;
        
        for (RelativeTimePeriod period : periods) {
            if (period.getId().equals(id)) {
                chosenPeriod = period;
                break;
            }
        }
        
        return chosenPeriod;
    }
    
    /**
     * <p>getStartAndEndTimes</p>
     *
     * @return an array of long.
     */
    public long[] getStartAndEndTimes() {
        Calendar cal = new GregorianCalendar();
        long end = cal.getTime().getTime();
        cal.add(getOffsetField(), getOffsetAmount());
        long start = cal.getTime().getTime();        

        return new long[] { start, end };
    }
}
