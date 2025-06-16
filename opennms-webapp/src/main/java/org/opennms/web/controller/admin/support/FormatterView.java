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
package org.opennms.web.controller.admin.support;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;

public final class FormatterView implements View {
	
	private static final Logger LOG = LoggerFactory.getLogger(FormatterView.class);


    private SystemReportFormatter m_formatter = null;
    private InternalResourceView m_view = null;

    public FormatterView(final SystemReportFormatter formatter) {
        super();
        m_formatter = formatter;
        
        if (m_formatter == null || !m_formatter.needsOutputStream() || m_formatter.getContentType() == null) {
            m_view = new InternalResourceView("/admin/support/systemReportList.htm");
        }
        
        LOG.debug("formatter = {}, view = {}", m_formatter, m_view);
    }

    @Override
    public String getContentType() {
        if (m_view != null) return m_view.getContentType();
        if (m_formatter == null) return "application/octet-stream";
        return m_formatter.getContentType();
    }

    @Override
    public void render(final Map<String, ?> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        if (m_view == null) {
            String fileName = getFileName(request);
            response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        } else {
            final String outputParameter = request.getParameter("output");
            m_formatter.setOutput(outputParameter);
        }
        if (model.containsKey("report")) {
            final SystemReportInfo info = (SystemReportInfo)model.get("report");
            LOG.debug("found report = {}", info);
            final OutputStream output;

            if (m_view == null) {
                output = response.getOutputStream();
            } else {
                output = new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {}
                };
            }

            try {
                LOG.debug("beginning output");
                m_formatter.setOutputStream(output);
                m_formatter.begin();

                for (final SystemReportPlugin plugin : info.getPlugins()) {
                    LOG.debug("running plugin {}", plugin);
                    m_formatter.write(plugin);
                    output.flush();
                }
                
                LOG.debug("finishing output");
                m_formatter.end();
            } catch (final Throwable e) {
                LOG.warn("Error while formatting system report output", e);
                throw new SystemReportException(e);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } else {
            LOG.info("Invalid form input: {}", model);
            throw new SystemReportException("Form input was invalid.");
        }
        LOG.debug("done");
        if (m_view != null) {
            m_view.render(model, request, response);
        }
    }

    public String getFileName(final HttpServletRequest request) {
        final String outputParameter = request.getParameter("output");
        String fileName = null;
        if (outputParameter != null && !outputParameter.matches("^[\\s]*$")) {
            fileName = new File(outputParameter).getName().replaceAll("[^\\w\\.]", "");
        } else {
            fileName = "opennms-system-report." + m_formatter.getExtension();
        }
        return fileName;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("formatter", m_formatter)
            .toString();
    }
}