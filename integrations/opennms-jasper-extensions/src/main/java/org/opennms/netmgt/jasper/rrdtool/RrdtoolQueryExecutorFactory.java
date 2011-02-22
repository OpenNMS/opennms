package org.opennms.netmgt.jasper.rrdtool;

import java.util.Map;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.JRQueryExecuterFactory;

public class RrdtoolQueryExecutorFactory implements JRQueryExecuterFactory {

    public JRQueryExecuter createQueryExecuter(JRDataset dataset, Map parameters)throws JRException {
        return new RrdtoolQueryExecutor(dataset, parameters);
    }

    public Object[] getBuiltinParameters() {
        return null;
    }

    public boolean supportsQueryParameterType(String parameterType) {
        return true;
    }

}
