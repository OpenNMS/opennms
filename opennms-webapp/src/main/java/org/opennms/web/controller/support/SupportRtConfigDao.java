package org.opennms.web.controller.support;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.rt.ReadOnlyRtConfigDao;

public class SupportRtConfigDao extends ReadOnlyRtConfigDao {

    @Override
    protected String getPrefix() {
        return "support";
    }

    @Override
    public void save() throws IOException {
        LogUtils.debugf(this, "saving configuration changes to " + getFile());

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
            
            fw.flush();

            clearCache();

            LogUtils.debugf(this, "finished saving configuration changes to " + getFile());
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
    
}
