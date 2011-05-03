package org.opennms.netmgt.jasper.rrdtool;

import java.util.Map;

import org.opennms.netmgt.jasper.jrobin.JRobinQueryExecutor;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;

public class RrdtoolQueryExecutor extends JRobinQueryExecutor {

	protected RrdtoolQueryExecutor(JRDataset dataset, Map parametersMap) {
		super(dataset, parametersMap);
	}

	@Override
	public JRDataSource createDatasource() throws JRException {
		try {
			return new RrdtoolXportCmd().executeCommand(getQueryString());
		} catch (Exception e) {
			throw new JRException("Error creating RrdtoolDataSource", e);
		}
	}

}
