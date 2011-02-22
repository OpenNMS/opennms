package org.opennms.netmgt.jasper.rrdtool;

import java.util.Date;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class RrdtoolDataSource implements JRDataSource {

	private int m_currentRow = -1;
	private Xport m_data;

	public RrdtoolDataSource(Xport data) {
		this.m_data = data;
	}

	public Object getFieldValue(JRField field) throws JRException {
		if ("Timestamp".equalsIgnoreCase(getColumnName(field))) {
			long ts = new Long(m_data.getData().getRow(m_currentRow).getT().getContent()) * 1000l;
			return new Date(ts);
		}
		int index = getColumnIndex(field);
		return new Double(m_data.getData().getRow(m_currentRow).getV(index).getContent());
	}

	private String getColumnName(JRField field) {
		return field.getDescription() == null || field.getDescription().trim().equals("")
		        ? field.getName() : field.getDescription();
	}

	public boolean next() throws JRException {
		m_currentRow++;
		return m_data == null || m_data.getData() == null ? false : m_currentRow < m_data.getData().getRowCount();
	}

	private int getColumnIndex(JRField field) {
		String column = getColumnName(field);
		int i=0;
		for (Entry legend : m_data.getMeta().getLegend().getEntryCollection()) {
			if (legend.getContent().equals(column))
				return i;
			i++;
		}
		return -1;
	}

}
