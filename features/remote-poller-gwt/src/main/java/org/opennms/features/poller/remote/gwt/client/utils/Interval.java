/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client.utils;

/**
 * <p>Interval class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Interval implements Comparable<Interval> {
    private long m_start;
    private long m_end;

    /**
     * <p>Constructor for Interval.</p>
     *
     * @param start a long.
     * @param end a long.
     */
    public Interval(long start, long end) {
        m_start = start;
        m_end = end;
    }

    /**
     * <p>getStartMillis</p>
     *
     * @return a long.
     */
    public long getStartMillis() {
        return m_start;
    }

    /**
     * <p>setStartMillis</p>
     *
     * @param start a long.
     */
    public void setStartMillis(final long start) {
        m_start = start;
    }

    /**
     * <p>getEndMillis</p>
     *
     * @return a long.
     */
    public long getEndMillis() {
        return m_end;
    }

    /**
     * <p>setEndMillis</p>
     *
     * @param end a long.
     */
    public void setEndMillis(final long end) {
        m_end = end;
    }

    /**
     * <p>overlaps</p>
     *
     * @param that a {@link org.opennms.features.poller.remote.gwt.client.utils.Interval} object.
     * @return a boolean.
     */
    public boolean overlaps(final Interval that) {
        if (this.getStartMillis() <= that.getStartMillis() && this.getEndMillis() >= that.getEndMillis()) {
            // completely surrounds
            return true;
        } else if (this.getStartMillis() >= that.getStartMillis() && this.getEndMillis() <= that.getEndMillis()) {
            // completely inside
            return true;
        } else if (this.getStartMillis() <= that.getStartMillis() && this.getEndMillis() >= that.getStartMillis()) {
            // overlaps start
            return true;
        } else if (this.getEndMillis() >= that.getEndMillis() && this.getStartMillis() <= that.getEndMillis()) {
            // overlaps end
            return true;
        }
        return false;
    }

    /**
     * <p>compareTo</p>
     *
     * @param that a {@link org.opennms.features.poller.remote.gwt.client.utils.Interval} object.
     * @return a int.
     */
    @Override
    public int compareTo(final Interval that) {
        if (that == null) return -1;
        return new CompareToBuilder()
        .append(this.getStartMillis(), that.getStartMillis())
        .append(this.getEndMillis(), that.getEndMillis())
        .toComparison();
    }

}
