package org.opennms.netmgt.jasper.resource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuterFactory;
import net.sf.jasperreports.engine.query.QueryExecuterFactoryBundle;

import org.opennms.netmgt.jasper.jrobin.JRobinQueryExecutorFactory;

public class ResourceQueryExecuterFactoryBundle implements
        QueryExecuterFactoryBundle {

    public String[] getLanguages() {
        return new String[] {"resourceQuery"};
    }

    public JRQueryExecuterFactory getQueryExecuterFactory(String language) throws JRException {
        if("resourceQuery".equals(language)) {
            return new JRobinQueryExecutorFactory();
        }else {
            return null;
        }
    }

}
