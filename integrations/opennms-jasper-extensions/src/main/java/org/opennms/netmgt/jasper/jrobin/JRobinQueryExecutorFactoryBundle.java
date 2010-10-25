package org.opennms.netmgt.jasper.jrobin;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuterFactory;
import net.sf.jasperreports.engine.query.QueryExecuterFactoryBundle;

public class JRobinQueryExecutorFactoryBundle implements
		QueryExecuterFactoryBundle {

	public String[] getLanguages() {
		return new String[] {"jrobin"};
	}

	public JRQueryExecuterFactory getQueryExecuterFactory(String language) throws JRException {
		if("jrobin".equals(language)) {
		    return new JRobinQueryExecutorFactory();
		}else {
		    return null;
		}
	}

}
