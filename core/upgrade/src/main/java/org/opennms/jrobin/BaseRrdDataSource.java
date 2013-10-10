/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.jrobin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The Class BaseRrdDataSource.
 * <p>Copied from opennms-tools/opennms-rrd-converter.</p>
 */
public abstract class BaseRrdDataSource implements TimeSeriesDataSource {

    /** The m_ds name mapping. */
    private Map<String,Integer> m_dsNameMapping = new TreeMap<String,Integer>();

    /** The m_ds names. */
    private List<String> m_dsNames = new ArrayList<String>();

    /**
     * Instantiates a new base rrd data source.
     *
     * @param dsNames the ds names
     */
    public BaseRrdDataSource(final List<String> dsNames) {
        setDsNames(dsNames);
    }

    /**
     * Instantiates a new base rrd data source.
     */
    public BaseRrdDataSource() {
    }

    /**
     * Sets the ds names.
     *
     * @param dsNames the new ds names
     */
    public void setDsNames(final List<String> dsNames) {
        m_dsNames = dsNames;
        for (int i = 0; i < dsNames.size(); i++) {
            m_dsNameMapping.put(dsNames.get(i), i);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#getDsNames()
     */
    public List<String> getDsNames() throws IOException {
        return m_dsNames;
    }

    /**
     * Gets the ds index.
     *
     * @param dsName the ds name
     * @return the ds index
     */
    protected Integer getDsIndex(final String dsName) {
        return m_dsNameMapping.get(dsName);
    }

    /**
     * Gets the row number for timestamp.
     *
     * @param timestamp the timestamp
     * @return the row number for timestamp
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected int getRowNumberForTimestamp(final long timestamp) throws IOException {
        final long arcStep = getNativeStep();
        final long offset = timestamp - getStartTime();
        final int row = (int)(offset/arcStep);
        return row;
    }

    /**
     * Checks if is valid timestamp.
     *
     * @param timestamp the timestamp
     * @return true, if is valid timestamp
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected boolean isValidTimestamp(final long timestamp) throws IOException {
        return getStartTime() <= timestamp && timestamp < getEndTime() + getNativeStep();
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#getData(long)
     */
    public List<RrdEntry> getData(final long step) throws IOException {
        final List<RrdEntry> entries = new ArrayList<RrdEntry>(getRows());
        for (long time = getStartTime(); time < getEndTime() + getNativeStep(); time += step) {
            entries.add(getDataAt(time));
        }
        return entries;
    }

    /* (non-Javadoc)
     * @see org.opennms.jrobin.TimeSeriesDataSource#getRows()
     */
    public int getRows() throws IOException {
        return (int)((getEndTime() - getStartTime()) / getNativeStep());
    }

}
