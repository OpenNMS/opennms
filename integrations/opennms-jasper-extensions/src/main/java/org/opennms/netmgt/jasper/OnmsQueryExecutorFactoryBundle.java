package org.opennms.netmgt.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuterFactory;
import net.sf.jasperreports.engine.query.QueryExecuterFactoryBundle;

import org.opennms.netmgt.jasper.jrobin.JRobinQueryExecutorFactory;
import org.opennms.netmgt.jasper.rrdtool.RrdtoolQueryExecutorFactory;

public class OnmsQueryExecutorFactoryBundle implements QueryExecuterFactoryBundle {
    
    public String[] getLanguages() {
        return new String[] {"jrobin","rrdtool"};
    }

    public JRQueryExecuterFactory getQueryExecuterFactory(String language) throws JRException {
        if("jrobin".equals(language)) {
            return new JRobinQueryExecutorFactory();
        } else if("rrdtool".equals(language)) {
            return new RrdtoolQueryExecutorFactory();
        } else {
            return null;
        }
    }
}
