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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public abstract class AbstractSystemReportFormatter implements SystemReportFormatter {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSystemReportFormatter.class);
    protected OutputStream m_outputStream = null;
    private String m_output;

    public AbstractSystemReportFormatter() {
    }

    protected String getOutput() {
        return m_output;
    }

    @Override
    public void setOutput(final String output) {
        m_output = output;
    }

    protected OutputStream getOutputStream() {
        return m_outputStream;
    }

    @Override
    public void setOutputStream(final OutputStream stream) {
        m_outputStream = stream;
    }

    @Override
    public boolean needsOutputStream() {
        return true;
    }

    @Override
    public String getName() {
        LOG.warn("Plugin did not implement getFormatName()! Using the class name: {}", this.getClass().getName());
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        LOG.warn("Plugin {} did not implement getDescription()! Using the format name.", getName());
        return this.getName();
    }

    @Override
    public void write(final SystemReportPlugin plugin) {
        LOG.warn("Plugin {} did not implement write()! No data was written.", getName());
    }

    @Override
    public void begin() {
        if (needsOutputStream() && m_outputStream == null) {
            LOG.error("The output stream is not set and this formatter requires an output stream.");
        }
    }

    @Override
    public void end() {
    }
    
    @Override
    public final int compareTo(final SystemReportFormatter o) { 
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
                LOG.warn("Unable to get inputstream for resource '{}'", r, e);
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
