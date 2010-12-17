package org.opennms.systemreport.formatters;

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

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.Resource;

public class ZipSystemReportFormatter extends AbstractSystemReportFormatter implements SystemReportFormatter {
    private File m_tempFile;
    private ZipOutputStream m_zipOutputStream;
    private Set<String> m_directories = new HashSet<String>();

    public ZipSystemReportFormatter() {
        super();
        try {
            m_tempFile = File.createTempFile(getName(), null);
            m_tempFile.deleteOnExit();
        } catch (final IOException e) {
            LogUtils.errorf(this, e, "Unable to create temporary file!");
        }
    }

    public String getName() {
        return "zip";
    }

    public String getDescription() {
        return "compressed file of all resources";
    }

    public String getContentType() {
        return "application/zip";
    }

    public String getExtension() {
        return "zip";
    }

    public boolean canStdout() {
        return false;
    }

    public void begin() {
        super.begin();
        try {
            m_zipOutputStream = new ZipOutputStream(new FileOutputStream(m_tempFile));
            m_zipOutputStream.setLevel(9);
        } catch (final Exception e) {
            LogUtils.errorf(this, e, "Unable to create zip file '%s'", m_tempFile);
            return;
        }
    }

    public void write(final SystemReportPlugin plugin) {
        final String name = plugin.getName() + ".txt";
        try {
            createDirectory("");
        } catch (final Exception e) {
            LogUtils.errorf(this, e, "Unable to create entry '%s'", name);
            return;
        }
        
        if (hasDisplayable(plugin)) {
            try {
                createEntry(name);
            } catch (final Exception e) {
                LogUtils.errorf(this, e, "Unable to create entry '%s'", name);
                return;
            }
            final AbstractSystemReportFormatter formatter = new TextSystemReportFormatter();
            formatter.setOutputStream(m_zipOutputStream);
            formatter.begin();
            formatter.write(plugin);
            formatter.end();
        }
        
        byte[] buf = new byte[1024];

        for (final Map.Entry<String,Resource> entry : plugin.getEntries().entrySet()) {
            final Resource resource = entry.getValue();
            if (isFile(resource)) {
                try {
                    createDirectory(plugin.getName());
                } catch (final Exception e) {
                    LogUtils.errorf(this, e, "Unable to create directory '%s'", plugin.getName());
                    return;
                }
                final String entryName = String.format("%s/%s", plugin.getName(), entry.getKey());
                try {
                    createEntry(entryName);
                } catch (final Exception e) {
                    LogUtils.errorf(this, e, "Unable to create entry '%s'", entryName);
                    return;
                }

                InputStream is = null;
                try {
                    is = resource.getInputStream();
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        m_zipOutputStream.write(buf, 0, len);
                    }
                } catch (Exception e) {
                    LogUtils.warnf(this, e, "Unable to read resource '%s'", resource);
                    return;
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }
    }

    public void end() {
        try {
            m_zipOutputStream.closeEntry();
        } catch (IOException e) {
            LogUtils.warnf(this, e, "Unable to close last entry.");
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
            LogUtils.warnf(this, e, "Unable to read temporary zip file '%s'", m_tempFile);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private void createEntry(final String name) throws IOException {
        LogUtils.infof(this, "adding to zip: opennms-system-report/%s", name);
        m_zipOutputStream.putNextEntry(new ZipEntry("opennms-system-report/" + name));
    }

    private void createDirectory(final String name) throws IOException {
        if (m_directories.contains(name)) return;
        createEntry(name + "/");
        m_directories.add(name);
    }
}
