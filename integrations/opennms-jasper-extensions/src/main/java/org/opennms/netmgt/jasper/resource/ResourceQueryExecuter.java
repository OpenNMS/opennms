package org.opennms.netmgt.jasper.resource;

import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRAbstractQueryExecuter;

public class ResourceQueryExecuter extends JRAbstractQueryExecuter {

    protected ResourceQueryExecuter(JRDataset dataset, Map parametersMap) {
        super(dataset, parametersMap);
        parseQuery();
    }

    public boolean cancelQuery() throws JRException {
        return false;
    }

    public void close() {}

    public JRDataSource createDatasource() throws JRException {
        return new ResourceQueryCommand().executeCommand(getQueryString());
    }

    @Override
    protected String getParameterReplacement(String parameterName) {
        Object parameterVal = getParameterValue(parameterName);
        
        return String.valueOf(parameterVal);
    }

}
