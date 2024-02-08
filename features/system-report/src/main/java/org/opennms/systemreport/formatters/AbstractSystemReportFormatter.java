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
import org.apache.commons.lang.builder.CompareToBuilder;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

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
}
