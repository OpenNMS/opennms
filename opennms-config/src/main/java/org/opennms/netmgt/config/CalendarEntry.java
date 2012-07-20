/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.util.Date;
import java.util.List;

import org.opennms.core.utils.Owner;

/**
 * <p>CalendarEntry class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class CalendarEntry {
    
    Date m_start;
    Date m_end;
    String m_descr;
    List<Owner> m_labels;
    
    /**
     * <p>Constructor for CalendarEntry.</p>
     *
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @param descr a {@link java.lang.String} object.
     * @param labels a {@link java.util.List} object.
     */
    public CalendarEntry(Date start, Date end, String descr, List<Owner> labels) {
        m_start = start;
        m_end = end;
        m_descr = descr;
        m_labels = labels;
    }

    /**
     * <p>getStartTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getStartTime() { return m_start; }
    
    /**
     * <p>getEndTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getEndTime() { return m_end; }
    
    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() { return m_descr; }
    
    /**
     * <p>getLabels</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Owner> getLabels() { return m_labels; }
}
