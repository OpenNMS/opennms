package org.opennms.netmgt.jasper.measurement;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;

import java.util.Map;

/**
 * The executor factory to create a Query Executor.
 */
public class MeasurementExecutorFactory implements QueryExecuterFactory {

    @Override
    public Object[] getBuiltinParameters() {
        return null;
    }

    @Override
    public JRQueryExecuter createQueryExecuter(JasperReportsContext jasperReportsContext, JRDataset dataset, Map<String, ? extends JRValueParameter> parameters) throws JRException {
        return new MeasurementQueryExecutor(jasperReportsContext, dataset, parameters);
    }

    @Override
    public JRQueryExecuter createQueryExecuter(JRDataset dataset, Map<String, ? extends JRValueParameter> parameters) throws JRException {
        return createQueryExecuter(DefaultJasperReportsContext.getInstance(), dataset, parameters);
    }

    @Override
    public boolean supportsQueryParameterType(String className) {
        return true;
    }
}
