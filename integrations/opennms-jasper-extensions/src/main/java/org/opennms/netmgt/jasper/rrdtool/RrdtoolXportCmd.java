package org.opennms.netmgt.jasper.rrdtool;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.exolab.castor.xml.Unmarshaller;
import org.opennms.core.utils.StringUtils;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.util.FileCopyUtils;

import net.sf.jasperreports.engine.JRDataSource;

public class RrdtoolXportCmd {

	public JRDataSource executeCommand(String queryString) {
		Xport data = getXportData(queryString);
		return new RrdtoolDataSource(data);
	}

	private Xport getXportData(String queryString) {
		String command = System.getProperty("rrd.binary") + " xport " + queryString.replaceAll("[\r\n]+", " ").replaceAll("\\s+", " ");
		log().debug("getXportData: executing command: " + command);
		String[] commandArray = StringUtils.createCommandArray(command, '@');
		Xport data = null;
		try {
			Process process = Runtime.getRuntime().exec(commandArray);
			// this closes the stream when its finished
			byte[] byteArray = FileCopyUtils.copyToByteArray(process.getInputStream());             
			// this close the stream when its finished
			String errors = FileCopyUtils.copyToString(new InputStreamReader(process.getErrorStream()));
			if (errors.length() > 0) {
				log().error("getXportData: RRDtool command fail: " + errors);
				return null;
			}
			BufferedReader reader = null;
			try {
				InputStream is = new ByteArrayInputStream(byteArray);
				reader = new BufferedReader(new InputStreamReader(is));
				data = (Xport) Unmarshaller.unmarshal(Xport.class, reader);
			} finally {
				reader.close();
			}
		} catch (Exception e) {
			log().error("getXportData: can't execute command '" + command + ": ", e);
			return null;
		}
		return data;
	}

	private ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}

}
