/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.jrobin;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;

import org.opennms.netmgt.jasper.jrobin.RrdXportCmd.XPort;

public class JRobinDataSource implements JRRewindableDataSource {

    private int m_currentRow = -1;
    private long[] m_timestamps;
    private long m_step;
    private List<XPort> m_xports;

    public JRobinDataSource(long step, long[] timestamps, List<XPort> xports) {
        m_step = step;
        m_timestamps = Arrays.copyOf(timestamps, timestamps.length);
        m_xports = xports;
        moveFirst();
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        Object computeFieldValue = computeFieldValue(field);
        return computeFieldValue;
    }

    private Object computeFieldValue(JRField field) {
        if ("Timestamp".equalsIgnoreCase(getColumnName(field))) {
            return new Date(m_timestamps[m_currentRow] * 1000L);
        } else if("Step".equalsIgnoreCase(getColumnName(field))) {
            return m_step;
        }
        XPort xport = findXPortForField(getColumnName(field));
        return xport == null ? null : Double.valueOf(xport.values[m_currentRow]);
    }

    private String getColumnName(JRField field) {
        return field.getDescription() == null || field.getDescription().trim().equals("")
                ? field.getName() : field.getDescription();
    }

    private XPort findXPortForField(String description) {
        for(XPort xport : m_xports) {
            if(xport.legend.equalsIgnoreCase(description)) {
                return xport;
            }
        }
        return null;
    }

    
    @Override
    public boolean next() throws JRException {
        m_currentRow++;
        return m_currentRow < m_timestamps.length;
    }

    @Override
    public void moveFirst() {
        m_currentRow = -1;
    }
}
