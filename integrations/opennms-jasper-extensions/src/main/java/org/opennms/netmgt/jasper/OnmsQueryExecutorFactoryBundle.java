package org.opennms.netmgt.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuterFactory;
import net.sf.jasperreports.engine.query.QueryExecuterFactoryBundle;

import org.opennms.netmgt.jasper.jrobin.JRobinQueryExecutorFactory;
import org.opennms.netmgt.jasper.resource.ResourceQueryExecuterFactory;
import org.opennms.netmgt.jasper.rrdtool.RrdtoolQueryExecutorFactory;

public class OnmsQueryExecutorFactoryBundle implements QueryExecuterFactoryBundle {
    
    public String[] getLanguages() {
        return new String[] {"jrobin","rrdtool","resourceQuery"};
    }

    public JRQueryExecuterFactory getQueryExecuterFactory(String language) throws JRException {
        if("jrobin".equals(language)) {
            return new JRobinQueryExecutorFactory();
        } else if("rrdtool".equals(language)) {
            return new RrdtoolQueryExecutorFactory();
        } else if("resourceQuery".equals(language)) {
            return new ResourceQueryExecuterFactory();
        } else {
            return null;
        }
    }
}
