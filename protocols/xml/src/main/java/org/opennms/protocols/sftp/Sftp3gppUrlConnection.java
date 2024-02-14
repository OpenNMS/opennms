/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.protocols.sftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;

/**
 * The class for managing SFTP.3GPP URL Connection.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Sftp3gppUrlConnection extends SftpUrlConnection {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Sftp3gppUrlConnection.class);

    /** The URL properties map. */
    private Map<String,String> m_urlProperties;

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
        File f = new File(url.getPath(), get3gppFileName());
        String path = f.getAbsolutePath();
        LOG.debug("getPath: retrieving data 3GPP (NE Mode) using {}", path);
        return path;
    }

    /**
     * Gets the file name for 3GPP-A (NE Mode).
     *
     * @return the path for 3GPP-A (NE Mode)
     * @throws SftpUrlException the SFTP URL exception
     */
    public String get3gppFileName() throws SftpUrlException {
        Map<String,String> properties = getQueryMap();

        // Checking required parameters
        if (!properties.containsKey("step")) {
            throw new SftpUrlException("Missing parameter 'step'. 3GPP requires the Collection Step to generate the file name.");
        }
        if (!properties.containsKey("neid")) {
            throw new SftpUrlException("Missing parameter 'neId'. 3GPP requires NE ID to generate the file name.");
        }

        // Build reference timestamp
        long reference = System.currentTimeMillis();
        String referenceStr = properties.get("referencetimestamp");
        if (referenceStr != null) {
            try {
                Date d = new Date(Long.parseLong(referenceStr));
                reference = d.getTime();
            } catch (Exception e) {
                throw new SftpUrlException("Invalid value for parameter 'referenceTimestamp': " + referenceStr);
            }
        }
        long step = Long.parseLong(properties.get("step")) * 1000;
        long timestamp = reference - reference  % step; // normalize timestamp
        LOG.debug("getPath: the reference timestamp used will be {}", new Date(timestamp));

        // Creating common time format objects
        LOG.info("getPath: generating 3GPP file type A (NE Mode) using URL {}", url);
        SimpleDateFormat datef = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timef = new SimpleDateFormat("HHmmZ");

        // Timezone processing
        String tz = properties.get("timezone");
        if (tz == null) {
            LOG.debug("getPath: time zone not provided, using current timezone {}", TimeZone.getDefault().getID());
        } else {
            LOG.debug("getPath: using time zone {}", TimeZone.getTimeZone(tz).getID());
            datef.setTimeZone(TimeZone.getTimeZone(tz));
            timef.setTimeZone(TimeZone.getTimeZone(tz));
        }

        // Processing 3GPP File Type A (NE Mode)
        final StringBuilder sb = new StringBuilder("A");
        sb.append(datef.format(new Date(timestamp)));
        sb.append(".");
        sb.append(timef.format(new Date(timestamp-step)));
        sb.append("-");
        sb.append(timef.format(new Date(timestamp)));
        sb.append("_");
        sb.append(properties.get("neid"));
        return sb.toString();
    }

    /**
     * Gets the time stamp from 3GPP XML file name.
     *
     * @param fileName the 3GPP XML file name
     * @return the time stamp from file
     */
    public long getTimeStampFromFile(String fileName) {
        Pattern p = Pattern.compile("\\w(\\d+)\\.(\\d+)-(\\d+)-(\\d+)-(\\d+)_.+");
        Matcher m = p.matcher(fileName);
        if (m.find()) {
            String value = m.group(1) + '-' + m.group(4); // Using end date as a reference
            try {
                DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd-HHmm");
                DateTime dateTime = dtf.parseDateTime(value);
                return dateTime.getMillis();
            } catch (Exception e) {
                LOG.warn("getTimeStampFromFile: malformed 3GPP file {}, because {}", fileName, e.getMessage());
                return 0;
            }
        }
        return 0;
    }

    /**
     * Gets the properties map from the URL Query..
     *
     * @return the query map
     */
    public Map<String,String> getQueryMap() {
        if (m_urlProperties == null) {
            m_urlProperties = new HashMap<String,String>();
            if (url.getQuery() != null) {
                for (String pair : url.getQuery().split("&")) {
                    String[] data = pair.split("=");
                    m_urlProperties.put(data[0].toLowerCase(), data[1]);
                }
            }
        }
        return m_urlProperties;
    }

    /**
     * Gets the file list (from the path defined on the URL).
     *
     * @return the file list
     * @throws SftpException the SFTP exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    public List<String> getFileList() throws SftpException, IOException {
        List<String> files = new ArrayList<>();
        List<LsEntry> entries = getChannel().ls(url.getPath());
        for (LsEntry entry : entries) {
            if (entry.getFilename().startsWith("."))
                continue;
            files.add(entry.getFilename());
        }
        Collections.sort(files);
        return files;
    }

    /**
     * Delete file (from the path defined on the URL).
     *
     * @param fileName the file name
     * @throws SftpException the SFTP exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void deleteFile(String fileName) throws SftpException, IOException {
        String deleteFlag = getQueryMap().get("deletefile");
        if (deleteFlag != null && Boolean.parseBoolean(deleteFlag)) {
            String file = url.getPath() + File.separatorChar + fileName;
            LOG.debug("deleting file {} from {}", file, url.getHost());
            getChannel().rm(file);
        }
    }

    /**
     * Gets the file (from the path defined on the URL).
     *
     * @param fileName the file name
     * @return the file
     * @throws SftpException the SFTP exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public InputStream getFile(String fileName) throws SftpException, IOException {
        return getChannel().get(url.getPath() + File.separatorChar + fileName);
    }

}
