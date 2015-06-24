/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.rrdtool;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.util.JRProperties;

import org.exolab.castor.xml.Unmarshaller;
import org.opennms.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

public class RrdtoolXportCmd {
    private static final Logger LOG = LoggerFactory.getLogger(RrdtoolXportCmd.class);

	public JRRewindableDataSource executeCommand(String queryString) throws JRException {
		Xport data = getXportData(queryString);
		return new RrdtoolDataSource(data);
	}

	private Xport getXportData(String queryString) throws JRException {
	    String rrdBinary = getRrdBinary();
        if(rrdBinary == null) {
            throw new JRException("rrd.binary property must be set either in opennms.properties or in iReport");
        }

		String command = rrdBinary + " xport " + queryString.replaceAll("[\r\n]+", " ").replaceAll("\\s+", " ");
		LOG.debug("getXportData: executing command: {}", command);
		String[] commandArray = StringUtils.createCommandArray(command, '@');
		Xport data = null;
		try {
			Process process = Runtime.getRuntime().exec(commandArray);
			// this closes the stream when its finished
			byte[] byteArray = FileCopyUtils.copyToByteArray(process.getInputStream());             
			// this close the stream when its finished
			String errors = FileCopyUtils.copyToString(new InputStreamReader(process.getErrorStream()));
			if (errors.length() > 0) {
				LOG.error("getXportData: RRDtool command fail: {}", errors);
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
			LOG.error("getXportData: can't execute command '{}'", command, e);
			throw new JRException("getXportData: can't execute command '" + command + ": ", e);
		}
		return data;
	}

	private String getRrdBinary() {
	    if(System.getProperty("rrd.binary") != null) {
            return System.getProperty("rrd.binary");
        } else if(JRProperties.getProperty("rrd.binary") != null) {
            return JRProperties.getProperty("rrd.binary");
        } else {
            return null;
        }

    }
}
