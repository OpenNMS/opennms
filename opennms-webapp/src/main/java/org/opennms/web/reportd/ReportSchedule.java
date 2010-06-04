package org.opennms.web.reportd;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.reportd.Report;

@XmlRootElement(name="reportSchedule")
public class ReportSchedule {

    private String m_Engine;
    private String m_Name;
    private String m_Format;
    private String m_Schedule;
    private String m_Template;
    
    public ReportSchedule(){
        
    }
    
    public ReportSchedule(Report report){
        m_Engine = report.getReportEngine();
        m_Name = report.getReportName();
        m_Format = report.getReportFormat();
        m_Schedule = report.getCronSchedule();
        m_Template = report.getReportTemplate();
    }

    public Report getReport(){
        Report rpt = new Report();
        rpt.setCronSchedule(m_Schedule);
        rpt.setReportName(m_Name);
        rpt.setReportFormat(m_Format);
        rpt.setReportEngine(m_Engine);
        rpt.setReportTemplate(m_Template);
        return rpt;
    }
    
    public String getEngine() {
        return m_Engine;
    }

    public void setEngine(String engine) {
        m_Engine = engine;
    }

    public String getName() {
        return m_Name;
    }

    public void setName(String name) {
        m_Name = name;
    }

    public String getFormat() {
        return m_Format;
    }

    public void setFormat(String format) {
        m_Format = format;
    }

    public String getSchedule() {
        return m_Schedule;
    }

    public void setSchedule(String schedule) {
        m_Schedule = schedule;
    }

    public String getTemplate() {
        return m_Template;
    }

    public void setTemplate(String template) {
        m_Template = template;
    }
    
    
}
