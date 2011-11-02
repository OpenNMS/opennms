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

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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
     * Gets the path for 3GPP-A (NE Mode).
     *
     * @return the path for 3GPP-A (NE Mode)
     * @throws SftpUrlException the SFTP URL exception
     */
    @Override
    protected String getPath() throws SftpUrlException {
        String path = m_url.getPath();
        Map<String,String> properties = getProperties(url);

        // Checking required parameters
        if (!properties.containsKey("step")) {
            throw new SftpUrlException("Missing parameter 'step'. 3GPP requires the Collection Step to generate the file name.");
        }
        if (!properties.containsKey("neid")) {
            throw new SftpUrlException("Missing parameter 'neId'. 3GPP requires NE ID to generate the file name.");
        }
        String fileType = properties.get("filetype");
        if (fileType == null) {
            log().debug("getPath: file type not provided, using A by default");
            fileType = "A";
        }

        // Creating common time format objects
        log().info("Processing 3GPP file type " + fileType + " using URL " + url);
        long step = Long.parseLong(properties.get("step")) * 1000;
        String tz = properties.get("tz-offset");
        long timestamp = System.currentTimeMillis() - System.currentTimeMillis()  % step;
        SimpleDateFormat datef = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timef = new SimpleDateFormat("HHmmZ");
        if (tz == null) {
            log().debug("getPath: time zone not provided, using current timezone " + TimeZone.getDefault().getID());
        } else {
            log().debug("getPath: using time zone " + TimeZone.getTimeZone(tz).getID());
            datef.setTimeZone(TimeZone.getTimeZone(tz));
            timef.setTimeZone(TimeZone.getTimeZone(tz));
        }

        // Processing 3GPP File Type A (NE Mode)
        if (fileType.equals("A")) {
            StringBuffer sb = new StringBuffer("A");
            sb.append(datef.format(new Date(timestamp)));
            sb.append(".");
            sb.append(timef.format(new Date(timestamp-step)));
            sb.append("-");
            sb.append(timef.format(new Date(timestamp)));
            sb.append("_");
            sb.append(properties.get("neid"));
            sb.append(".xml");
            File f = new File(path, sb.toString());
            path = f.getAbsolutePath();
        }

        log().info("Retrieving 3GPP NE data using " + path);
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
