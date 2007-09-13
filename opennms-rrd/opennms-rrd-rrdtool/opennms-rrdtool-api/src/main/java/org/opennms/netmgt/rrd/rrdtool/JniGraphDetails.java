package org.opennms.netmgt.rrd.rrdtool;

import java.io.InputStream;

import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;

public class JniGraphDetails implements RrdGraphDetails {

	private int m_height;
	private int m_width;
	private String[] m_printLines;
	private InputStream m_inputStream;

	public JniGraphDetails(int height, int width, String[] lines, InputStream stream) {
		m_height = height;
		m_width = width;
		m_printLines = lines;
		m_inputStream = stream;
	}

	public int getHeight() throws RrdException {
		return m_height;
	}

	public InputStream getInputStream() throws RrdException {
		return m_inputStream;
	}

	public String[] getPrintLines() throws RrdException {
		return m_printLines;
	}

	public int getWidth() throws RrdException {
		return m_width;
	}

}
