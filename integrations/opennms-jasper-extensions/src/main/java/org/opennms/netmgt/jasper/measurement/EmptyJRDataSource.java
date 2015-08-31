package org.opennms.netmgt.jasper.measurement;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;

/**
 * An empty or nullable data source implementation.
 */
class EmptyJRDataSource implements JRRewindableDataSource {
    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        return null;
    }

    @Override
    public boolean next() throws JRException {
        return false;
    }

    @Override
    public void moveFirst() throws JRException {

    }
}
