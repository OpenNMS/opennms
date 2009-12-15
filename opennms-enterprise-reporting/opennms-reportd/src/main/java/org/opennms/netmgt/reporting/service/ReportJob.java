package org.opennms.netmgt.reporting.service;

import org.opennms.netmgt.config.reportd.Report;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ReportJob implements Job {

    
    protected static final String KEY = "report";

    private Reportd m_reportd;

    
    public void execute(JobExecutionContext context)
        throws JobExecutionException {
            m_reportd.runReport((Report)context.getJobDetail().getJobDataMap().get(KEY));
    }


    public Reportd getReportd() {
        return m_reportd;
    }


    public void setReportd(Reportd reportd) {
        m_reportd = reportd;
    }

}
