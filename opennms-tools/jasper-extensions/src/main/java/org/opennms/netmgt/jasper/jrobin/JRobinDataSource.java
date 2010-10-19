package org.opennms.netmgt.jasper.jrobin;

import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.opennms.netmgt.jasper.jrobin.RrdXportCmd.XPort;

public class JRobinDataSource implements JRDataSource {

    private int m_currentRow = -1;
    private long[] m_timestamps;
    private List<XPort> m_xports;

    public JRobinDataSource(long[] timestamps, List<XPort> xports) {
        m_timestamps = timestamps;
        m_xports = xports;
    }

    public Object getFieldValue(JRField field) throws JRException {
        if ("Timestamp".equals(field.getName())) {
            return new Date(m_timestamps[m_currentRow] * 1000L);
        }
        XPort xport = findXPortForField(field.getDescription());
        return xport == null ? null : Double.valueOf(xport.values[m_currentRow]);
    }

    private XPort findXPortForField(String description) {
        for(XPort xport : m_xports) {
            if(xport.legend.equals(description)) {
                return xport;
            }
        }
        return null;
    }

    
    public boolean next() throws JRException {
        m_currentRow++;
        return m_currentRow < m_timestamps.length;
    }

}
