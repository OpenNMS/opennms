/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.support;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.io.IOUtils;
import org.opennms.netmgt.rt.ReadOnlyRtConfigDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupportRtConfigDao extends ReadOnlyRtConfigDao {
	
	private static final Logger LOG = LoggerFactory.getLogger(SupportRtConfigDao.class);


    @Override
    protected String getPrefix() {
        return "support";
    }

    @Override
    public void save() throws IOException {
        LOG.debug("saving configuration changes to {}", getFile());

        FileWriter fw = null;

        try {
            fw = new FileWriter(new File(getFile()), false);

            fw.write("# NOTE: this file is generated.  You will lose any modifications that aren't known properties!\n\n");

            if (!"https://mynms.opennms.com".equals(getBaseURL())) {
                fw.write("# The base URL for the OpenNMS support server.\n");
                fw.write("support.baseURL=" + getBaseURL() + "\n\n");
            }

            fw.write("# The support queue numeric ID to use when opening tickets\n");
            fw.write("support.queueId=" + getQueueId() + "\n\n");

            fw.write("# The timeout when attempting to open a ticket\n");
            fw.write("support.timeout=" + getTimeout() + "\n\n");

            fw.write("# The number of times to retry before giving up when opening a ticket\n");
            fw.write("support.retry=" + getRetry() + "\n\n");

            if (!"OpenNMS Version".equals(getVersionFieldName())) {
                fw.write("support.versionFieldName=" + getVersionFieldName() + "\n\n");
            }

            if (!"Operating System".equals(getOSFieldName())) {
                fw.write("support.osFieldName=" + getOSFieldName() + "\n\n");
            }

            fw.flush();

            clearCache();

            LOG.debug("finished saving configuration changes to {}", getFile());
        } finally {
            IOUtils.closeQuietly(fw);
        }
    }

    public void setUsername(final String username) {
        setProperty("support.username", username);
    }

    public void setPassword(final String password) {
        final String propertyName = "support.password";
        setProperty(propertyName, password);
    }

    public Long getQueueId() {
        if (getProperties() == null) {
            return null;
        }
        try {
            return getProperties().getLong("support.queueId");
        } catch (final ConversionException e) {
            return null;
        }
    }

    public void setQueueId(final long queueId) {
        setProperty("support.queueId", Long.valueOf(queueId));
    }

    @Override
    public String getBaseURL() {
        final String baseUrl = getPrefix() + ".baseURL";
        final String defaultBaseUrl = "https://mynms.opennms.com";
        return getStringProperty(baseUrl, defaultBaseUrl);
    }

    public void setFtpBaseURL(final String url) {
        setProperty("support.ftpBaseURL", url);
    }

    public String getFtpBaseURL() {
        return getStringProperty("support.ftpBaseURL", "ftp://ftp.opennms.org/incoming");
    }

    public void setVersionFieldName(final String name) {
        setProperty("support.versionFieldName", name);
    }

    public String getVersionFieldName() {
        return getStringProperty("support.versionFieldName", "OpenNMS Version");
    }

    public void setOSFieldName(final String name) {
        setProperty("support.osFieldName", name);
    }

    public String getOSFieldName() {
        return getStringProperty("support.osFieldName", "Operating System");
    }

}
