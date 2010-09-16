package org.opennms.systemreport.formatters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public abstract class AbstractSystemReportFormatter implements SystemReportFormatter {
    private OutputStream m_outputStream = null;
    private String m_output;

    public AbstractSystemReportFormatter() {
    }

    protected String getOutput() {
        return m_output;
    }

    public void setOutput(final String output) {
        m_output = output;
    }

    protected OutputStream getOutputStream() {
        return m_outputStream;
    }

    public void setOutputStream(final OutputStream stream) {
        m_outputStream = stream;
    }

    public boolean needsOutputStream() {
        return true;
    }

    public String getName() {
        LogUtils.warnf(this, "Plugin did not implement getFormatName()!  Using the class name.");
        return this.getClass().getName();
    }

    public String getDescription() {
        LogUtils.warnf(this, "Plugin did not implement getDescription()!  Using the format name.");
        return this.getName();
    }

    public void write(final SystemReportPlugin plugin) {
        LogUtils.warnf(this, "Plugin did not implement write()!  No data written.");
    }

    public void begin() {
    }

    public void end() {
    }
    
    public int compareTo(final SystemReportFormatter o) {
        return new CompareToBuilder()
            .append(this.getName(), (o == null? null:o.getName()))
            .append(this.getDescription(), (o == null? null:o.getDescription()))
            .toComparison();
    }

    protected boolean isDisplayable(final Resource r) {
        return (r instanceof ByteArrayResource);
    }

    protected boolean isFile(final Resource r) {
        return (r instanceof FileSystemResource);
    }

    protected String getResourceText(final Resource r) {
        if (r instanceof ByteArrayResource) {
            return new String(((ByteArrayResource) r).getByteArray());
        } else {
            InputStream is = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            try {
                is = r.getInputStream();
                if (is != null) {
                    final StringBuilder sb = new StringBuilder();
                    String line = null;
                    isr = new InputStreamReader(is, Charset.defaultCharset());
                    br = new BufferedReader(isr);
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    sb.deleteCharAt(sb.length());
                    return sb.toString();
                }
            } catch (final IOException e) {
                LogUtils.warnf(this, e, "Unable to get inputstream for resource '%s'", r);
                return null;
            } finally {
                IOUtils.closeQuietly(br);
                IOUtils.closeQuietly(isr);
                IOUtils.closeQuietly(is);
            }
        }
        return null;
    }

    protected boolean hasDisplayable(final SystemReportPlugin plugin) {
        for (final Map.Entry<String,Resource> entry : plugin.getEntries().entrySet()) {
            if (isDisplayable(entry.getValue())) {
                return true;
            }
        }
        return false;
    }
}
