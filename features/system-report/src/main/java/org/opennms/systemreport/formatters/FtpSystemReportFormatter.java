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

package org.opennms.systemreport.formatters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;

public class FtpSystemReportFormatter extends AbstractSystemReportFormatter implements SystemReportFormatter {
    private URL m_url;
    private ZipSystemReportFormatter m_zipFormatter;
    private File m_zipFile;

    @Override
    public String getName() {
        return "ftp";
    }

    @Override
    public String getDescription() {
        return "FTP to the URL specified in the output option (eg. ftp://username:password@ftp.example.com/incoming/my-file.zip) (full output)";
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getExtension() {
        return "zip";
    }

    @Override
    public boolean canStdout() {
        return false;
    }

    @Override
    public boolean needsOutputStream() {
        return false;
    }

    @Override
    public void begin() {
        super.begin();
        try {
            m_url = new URL(getOutput());
        } catch (final MalformedURLException e) {
            LogUtils.errorf(this, e, "Unable to parse %s as an FTP URL", getOutput());
            throw new IllegalArgumentException(String.format("Unable to parse \"%s\" as an FTP URL", getOutput()));
        }
        if (!m_url.getProtocol().equalsIgnoreCase("ftp")) {
            LogUtils.errorf(this, "URL %s is not an FTP URL", m_url);
            throw new IllegalArgumentException(String.format("URL \"%s\" is not an FTP URL", getOutput()));
        }
        m_zipFormatter = new ZipSystemReportFormatter();
        try {
            m_zipFile = File.createTempFile("ftpSystemReportFormatter", null);
            LogUtils.debugf(this, "Temporary ZIP file for system report FTP transfer = %s", m_zipFile.getPath());
            m_outputStream = new FileOutputStream(m_zipFile);
            m_zipFormatter.setOutput(getOutput());
            m_zipFormatter.setOutputStream(m_outputStream);
            m_zipFormatter.begin();
        } catch (final IOException e) {
            LogUtils.errorf(this, e, "Unable to create temporary file for system report FTP transfer");
            throw new IllegalStateException("Unable to create temporary file for system report FTP transfer");
        }
    }
    
    @Override
    public void write(final SystemReportPlugin plugin) {
        if (m_url == null) return;

        m_zipFormatter.write(plugin);
    }
    
    @Override
    public void end() {
        m_zipFormatter.end();
        IOUtils.closeQuietly(m_outputStream);
        
        final FTPClient ftp = new FTPClient();
        FileInputStream fis = null;
        try {
            if (m_url.getPort() == -1 || m_url.getPort() == 0 || m_url.getPort() == m_url.getDefaultPort()) {
                ftp.connect(m_url.getHost());
            } else {
                ftp.connect(m_url.getHost(), m_url.getPort());
            }
            if (m_url.getUserInfo() != null && m_url.getUserInfo().length() > 0) {
                final String[] userInfo = m_url.getUserInfo().split(":", 2);
                ftp.login(userInfo[0], userInfo[1]);
            } else {
                ftp.login("anonymous", "opennmsftp@");
            }
            int reply = ftp.getReplyCode();
            if(!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                LogUtils.errorf(this, "FTP server refused connection.");
                return;
            }

            String path = m_url.getPath();
            if (path.endsWith("/")) {
                LogUtils.errorf(this, "Your FTP URL must specify a filename.");
                return;
            }
            File f = new File(path);
            path = f.getParent();
            if (!ftp.changeWorkingDirectory(path)) {
                LogUtils.infof(this, "unable to change working directory to %s", path);
                return;
            }
            LogUtils.infof(this, "uploading %s to %s", f.getName(), path);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            fis = new FileInputStream(m_zipFile);
            if (!ftp.storeFile(f.getName(), fis)) {
                LogUtils.infof(this, "unable to store file");
                return;
            }
            LogUtils.infof(this, "finished uploading");
        } catch (final Exception e) {
            LogUtils.errorf(this, e, "Unable to FTP file to %s", m_url);
        } finally {
            IOUtils.closeQuietly(fis);
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                  } catch(IOException ioe) {
                    // do nothing
                  }
            }
        }
    }
}
