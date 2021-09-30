/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.newts.converter.rrd.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class BaseRrdDataSource implements TimeSeriesDataSource {
    private Map<String,Integer> m_dsNameMapping = new TreeMap<String,Integer>();
    private List<String> m_dsNames = new ArrayList<>();

    public BaseRrdDataSource(final List<String> dsNames) {
        setDsNames(dsNames);
    }
    
    public BaseRrdDataSource() {
    }

    public void setDsNames(final List<String> dsNames) {
        m_dsNames = dsNames;
        for (int i = 0; i < dsNames.size(); i++) {
            m_dsNameMapping.put(dsNames.get(i), i);
        }
    }
    
    public List<String> getDsNames() throws IOException {
        return m_dsNames;
    }
    
    protected Integer getDsIndex(final String dsName) {
        return m_dsNameMapping.get(dsName);
    }

    protected int getRowNumberForTimestamp(final long timestamp) throws IOException {
        final long arcStep = getNativeStep();
        final long offset = timestamp - getStartTime();
        final int row = (int)(offset/arcStep);
        return row;
    }

    protected boolean isValidTimestamp(final long timestamp) throws IOException {
        return getStartTime() <= timestamp && timestamp < getEndTime() + getNativeStep();
    }

    public List<RrdEntry> getData(final long step) throws IOException {
        final List<RrdEntry> entries = new ArrayList<RrdEntry>(getRows());
        for (long time = getStartTime(); time < getEndTime() + getNativeStep(); time += step) {
            entries.add(getDataAt(time));
        }
        return entries;
    }

    public int getRows() throws IOException {
        return (int)((getEndTime() - getStartTime()) / getNativeStep());
    }

    /*
    public abstract long getStartTime() throws IOException;
    public abstract long getEndTime() throws IOException;
    public abstract long getNativeStep() throws IOException;
    public abstract long getRows() throws IOException;
    public abstract RrdEntry getDataAt(long timestamp) throws IOException;
    public abstract List<RrdEntry> getData(long step) throws IOException;
    */
    
}
