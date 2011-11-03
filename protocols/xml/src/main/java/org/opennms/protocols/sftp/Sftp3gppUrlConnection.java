/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.protocols.sftp;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.core.utils.LogUtils;

/**
 * The class for managing SFTP+3GPP URL Connection.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Sftp3gppUrlConnection extends SftpUrlConnection {

    /**
     * Instantiates a new SFTP+3GPP URL connection.
     *
     * @param url the URL
     */
    protected Sftp3gppUrlConnection(URL url) {
        super(url);
    }

    /**
     * Gets the path for 3GPP-A (NE Mode)
     *
     * @param path the current unformatted path
     * @return the path for 3GPP-A (NE Mode)
     */
    @Override
    protected String getPath() {
        String path = m_url.getPath();
        if (path.contains("___CURRENT_3GPP_A_FORMAT")) {
            Map<String,String> properties = getProperties(url);
            log().info("Processing 3GPP NE URL on " + path);
            long step = Long.parseLong(properties.get("step")) * 1000;
            String tz = properties.get("tz-offset");
            long timestamp = System.currentTimeMillis() - System.currentTimeMillis()  % step;
            SimpleDateFormat datef = new SimpleDateFormat("yyyyMMdd");
            datef.setTimeZone(TimeZone.getTimeZone(tz));
            SimpleDateFormat timef = new SimpleDateFormat("HHmmZ");
            timef.setTimeZone(TimeZone.getTimeZone(tz));

            StringBuffer sb = new StringBuffer("A");
            sb.append(datef.format(new Date(timestamp)));
            sb.append(".");
            sb.append(timef.format(new Date(timestamp-step)));
            sb.append("-");
            sb.append(timef.format(new Date(timestamp)));
            sb.append("_");
            sb.append(properties.get("neid"));
            sb.append(".xml");
            RE re;
			try {
				re = new RE("___CURRENT_3GPP_A_FORMAT");
	            path = re.subst(path, sb.toString());
			} catch (final RESyntaxException e) {
				LogUtils.warnf(this, e, "An error occurred applying path '%s'", path);
			}
        }
        return path;
    }

    /**
     * Gets the properties.
     *
     * @param url the URL
     * @return the properties map
     */
    private Map<String,String> getProperties(URL url) {
        Map<String,String> properties = new HashMap<String,String>();
        if (url.getQuery() != null) {
            for (String pair : url.getQuery().split("&")) {
                String data[] = pair.split("=");
                properties.put(data[0].toLowerCase(), data[1]);
            }
        }
        return properties;
    }

}
