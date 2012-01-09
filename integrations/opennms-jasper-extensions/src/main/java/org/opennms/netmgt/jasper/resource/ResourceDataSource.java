package org.opennms.netmgt.jasper.resource;

import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class ResourceDataSource implements JRDataSource {

    private int m_currentRow = -1;
    private List<String> m_paths;
    
    public ResourceDataSource(List<String> paths) {
        m_paths = paths;
    }

    public Object getFieldValue(JRField field) throws JRException {
        return computeValueForField(field);
    }

    private Object computeValueForField(JRField field) {
        return m_paths.get(m_currentRow);
    }

    public boolean next() throws JRException {
        m_currentRow++;
        return m_currentRow < m_paths.size();
    }

}
