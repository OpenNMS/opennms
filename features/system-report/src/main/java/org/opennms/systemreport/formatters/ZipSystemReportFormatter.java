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

import org.apache.commons.io.IOUtils;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipSystemReportFormatter extends AbstractSystemReportFormatter implements SystemReportFormatter {
    private static final Logger LOG = LoggerFactory.getLogger(ZipSystemReportFormatter.class);
    private File m_tempFile;
    private ZipOutputStream m_zipOutputStream;
    private Set<String> m_directories = new HashSet<>();

    public ZipSystemReportFormatter() {
        super();
        try {
            m_tempFile = File.createTempFile(getName(), null);
            m_tempFile.deleteOnExit();
        } catch (final IOException e) {
            LOG.error("Unable to create temporary file!", e);
        }
    }

    @Override
    public String getName() {
        return "zip";
    }

    @Override
    public String getDescription() {
        return "Compressed file of all resources (full output)";
    }

    @Override
    public String getContentType() {
        return "application/zip";
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
    public boolean isVisible() { return true; }

    @Override
    public void begin() {
        super.begin();
        try {
            m_zipOutputStream = new ZipOutputStream(new FileOutputStream(m_tempFile));
            m_zipOutputStream.setLevel(9);
        } catch (final Exception e) {
            LOG.error("Unable to create zip file '{}'", m_tempFile, e);
            return;
        }
    }

    @Override
    public void write(final SystemReportPlugin plugin) {
        final String name = plugin.getName() + plugin.defaultFormat();
        try {
            createDirectory("");
        } catch (final Exception e) {
            LOG.error("Unable to create entry '{}'", name, e);
            return;
        }

        if (!plugin.getFullOutputOnly()) {
            try {
                createEntry(name);
            } catch (final Exception e) {
                LOG.error("Unable to create entry '{}'", name, e);
                return;
            }

            AbstractSystemReportFormatter formatter = new TextSystemReportFormatter();
            if (".csv".equals(plugin.defaultFormat())) {
                formatter = new CsvSystemReportFormatter();
            }

            formatter.setOutputStream(m_zipOutputStream);
            formatter.begin();
            formatter.write(plugin);
            formatter.end();
        }

        if (plugin.getOutputsFiles()) {
            byte[] buf = new byte[1024];

            for (final Map.Entry<String, Resource> entry : plugin.getEntries().entrySet()) {
                final Resource resource = entry.getValue();

                try {
                    createDirectory(plugin.getName());
                } catch (final Exception e) {
                    LOG.error("Unable to create directory '{}'", plugin.getName(), e);
                    return;
                }
                final String entryName = String.format("%s/%s", plugin.getName(), entry.getKey());
                try {
                    createEntry(entryName);
                } catch (final Exception e) {
                    LOG.error("Unable to create entry '{}'", entryName, e);
                    return;
                }

                InputStream is = null;
                try {
                    is = resource.getInputStream();
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        m_zipOutputStream.write(buf, 0, len);
                    }
                } catch (Throwable e) {
                    LOG.warn("Unable to read resource '{}'", resource, e);
                    return;
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }
    }

    @Override
    public void end() {
        try {
            m_zipOutputStream.closeEntry();
        } catch (IOException e) {
            LOG.warn("Unable to close last entry.", e);
        }
        IOUtils.closeQuietly(m_zipOutputStream);
        
        InputStream is = null;
        try {
            byte[] buf = new byte[1024];
            is = new FileInputStream(m_tempFile);
            final OutputStream os = getOutputStream();
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
        } catch (final Exception e) {
            LOG.warn("Unable to read temporary zip file '{}'", m_tempFile, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private void createEntry(final String name) throws IOException {
        LOG.info("adding to zip: opennms-system-report/{}", name);
        m_zipOutputStream.putNextEntry(new ZipEntry("opennms-system-report/" + name));
    }

    private void createDirectory(final String name) throws IOException {
        if (m_directories.contains(name)) return;
        createEntry(name + "/");
        m_directories.add(name);
    }
}
