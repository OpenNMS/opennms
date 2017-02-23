/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
                new RelativeTimePeriod("lastday", "Last Day", Calendar.DATE,
                                       -1),
                new RelativeTimePeriod("lastweek", "Last Week",
                                       Calendar.DATE, -7),
                new RelativeTimePeriod("lastmonth", "Last Month",
                                       Calendar.DATE, -31),
                new RelativeTimePeriod("lastyear", "Last Year",
                                       Calendar.DATE, -366) };
    }

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
        return getPeriodByIdOrDefault(s_defaultPeriods, id,
                                      s_defaultPeriods[0]);
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
