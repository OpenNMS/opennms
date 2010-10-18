package org.opennms.netmgt.jasper.jrobin;

import java.util.Date;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class JRobinDataSource implements JRDataSource {

    private static long INCREMENT = 300L * 1000L;
    
    private int m_rows;
    private String[] m_fields;
    private int m_currentRow = 0;
    private long m_end;

    public JRobinDataSource(String queryString) {
        String[] stringArray = queryString.split(":");
        m_rows = Integer.parseInt(stringArray[0]);
        m_fields = new String[stringArray.length -1];
        System.arraycopy(stringArray, 1, m_fields, 0, m_fields.length);
        m_end = ((System.currentTimeMillis() / INCREMENT) * INCREMENT);
    }

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
    
    public boolean next() throws JRException {
        m_currentRow++;
        return m_currentRow <= m_rows;
    }

}
