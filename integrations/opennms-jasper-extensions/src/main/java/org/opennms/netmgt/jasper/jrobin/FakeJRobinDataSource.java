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

package org.opennms.netmgt.jasper.jrobin;

import java.util.Date;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class FakeJRobinDataSource implements JRDataSource {

    private static long INCREMENT = 300L * 1000L;
    
    private int m_rows;
    private String[] m_fields;
    private int m_currentRow = 0;
    private long m_end;

    public FakeJRobinDataSource(String queryString) {
        String[] stringArray = queryString.split(":");
        m_rows = Integer.parseInt(stringArray[0]);
        m_fields = new String[stringArray.length -1];
        System.arraycopy(stringArray, 1, m_fields, 0, m_fields.length);
        m_end = ((System.currentTimeMillis() / INCREMENT) * INCREMENT);
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        if ("Timestamp".equals(field.getName())) {
            long millis = m_end - (m_rows - m_currentRow)*INCREMENT;
            return new Date(millis);
        }
        Integer index = getColumnIndex(field.getName());
        return index == null ? null : Double.valueOf(m_currentRow * index);
    }

    private Integer getColumnIndex(String fieldName) {
        for(int i =0; i < m_fields.length; i++) {
            if(m_fields[i].equals(fieldName)) {
                return (i + 1);
            }
        }
        return null;
    }
    
    @Override
    public boolean next() throws JRException {
        m_currentRow++;
        return m_currentRow <= m_rows;
    }

}
