package org.opennms.web.controller.admin.support;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;

public final class FormatterView implements View {

    private SystemReportFormatter m_formatter = null;
    private InternalResourceView m_view = null;

    public FormatterView(final SystemReportFormatter formatter) {
        super();
        m_formatter = formatter;
        
        if (m_formatter == null || !m_formatter.needsOutputStream() || m_formatter.getContentType() == null) {
            m_view = new InternalResourceView("admin/support/systemReport");
        }
        
        LogUtils.debugf(this, "formatter = %s, view = %s", m_formatter, m_view);
    }

    public String getContentType() {
        if (m_view != null) return m_view.getContentType();
        if (m_formatter == null) return "application/octet-stream";
        return m_formatter.getContentType();
    }

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
            LogUtils.debugf(this, "found report = %s", info);
            final OutputStream output;

            if (m_view == null) {
                output = response.getOutputStream();
                m_formatter.setOutputStream(response.getOutputStream());
            } else {
                output = new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {}
                };
            }

            try {
                LogUtils.debugf(this, "beginning output");
                m_formatter.setOutputStream(output);
                m_formatter.begin();

                for (final SystemReportPlugin plugin : info.getPlugins()) {
                    LogUtils.debugf(this, "running plugin %s", plugin);
                    m_formatter.write(plugin);
                    output.flush();
                }
                
                LogUtils.debugf(this, "finishing output");
                m_formatter.end();
            } catch (final Exception e) {
                LogUtils.warnf(this, e, "Error while formatting output.");
                throw new SystemReportException(e);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } else {
            LogUtils.infof(this, "Invalid form input: %s", model);
            throw new SystemReportException("Form input was invalid.");
        }
        LogUtils.debugf(this, "done");
        if (m_view != null) {
            m_view.render(model, request, response);
        }
    }

    public String getFileName(final HttpServletRequest request) {
        final String outputParameter = request.getParameter("output");
        String fileName = null;
        if (outputParameter != null && !outputParameter.matches("^[\\s]*$")) {
            fileName = new File(outputParameter).getName().replaceAll("[^[:alnum:]\\.]", "");
        } else {
            fileName = "opennms-system-report." + m_formatter.getExtension();
        }
        return fileName;
    }
    
    public String toString() {
        return new ToStringBuilder(this)
            .append("formatter", m_formatter)
            .toString();
    }
}