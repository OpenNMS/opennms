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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;

public class FtpSystemReportFormatter extends AbstractSystemReportFormatter implements SystemReportFormatter {
    private static final Logger LOG = LoggerFactory.getLogger(FtpSystemReportFormatter.class);
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
            LOG.error("Unable to parse {} as an FTP URL", getOutput(), e);
            throw new IllegalArgumentException(String.format("Unable to parse \"%s\" as an FTP URL", getOutput()));
        }
        if (!m_url.getProtocol().equalsIgnoreCase("ftp")) {
            LOG.error("URL {} is not an FTP URL", m_url);
            throw new IllegalArgumentException(String.format("URL \"%s\" is not an FTP URL", getOutput()));
        }
        m_zipFormatter = new ZipSystemReportFormatter();
        try {
            m_zipFile = File.createTempFile("ftpSystemReportFormatter", null);
            LOG.debug("Temporary ZIP file for system report FTP transfer = {}", m_zipFile.getPath());
            m_outputStream = new FileOutputStream(m_zipFile);
            m_zipFormatter.setOutput(getOutput());
            m_zipFormatter.setOutputStream(m_outputStream);
            m_zipFormatter.begin();
        } catch (final IOException e) {
            LOG.error("Unable to create temporary file for system report FTP transfer", e);
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
                LOG.error("FTP server refused connection.");
                return;
            }

            String path = m_url.getPath();
            if (path.endsWith("/")) {
                LOG.error("Your FTP URL must specify a filename.");
                return;
            }
            File f = new File(path);
            path = f.getParent();
            if (!ftp.changeWorkingDirectory(path)) {
                LOG.info("unable to change working directory to {}", path);
                return;
            }
            LOG.info("uploading {} to {}", f.getName(), path);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            fis = new FileInputStream(m_zipFile);
            if (!ftp.storeFile(f.getName(), fis)) {
                LOG.info("unable to store file");
                return;
            }
            LOG.info("finished uploading");
        } catch (final Exception e) {
            LOG.error("Unable to FTP file to {}", m_url, e);
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
