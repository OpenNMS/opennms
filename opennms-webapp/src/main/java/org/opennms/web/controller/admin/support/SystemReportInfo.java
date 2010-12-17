package org.opennms.web.controller.admin.support;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;

public class SystemReportInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<SystemReportPlugin> m_plugins;
    private final List<SystemReportFormatter> m_formatters;


    public SystemReportInfo(final List<SystemReportFormatter> formatters, final List<SystemReportPlugin> plugins) {
        m_formatters = formatters;
        m_plugins = plugins;
    }

    public SystemReportFormatter getFormatter() {
        if (m_formatters != null && m_formatters.size() > 0) {
            return m_formatters.get(0);
        }
        return null;
    }
    
    public List<SystemReportFormatter> getFormatters() {
        return m_formatters;
    }
    
    public List<SystemReportPlugin> getPlugins() {
        return m_plugins;
    }
    
    public String toString() {
        return new ToStringBuilder(this)
            .append("formatters", m_formatters)
            .append("plugins", m_plugins)
            .toString();
    }
}
