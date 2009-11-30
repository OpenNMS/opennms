package org.opennms.netmgmt.reporting.service;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ReportJob implements Job {

    
    protected static final String KEY = "report";

    private Reportd m_reportd;

    
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        m_reportd.runReport(context.getJobDetail().getName());
    }


    public Reportd getReportd() {
        return m_reportd;
    }


    public void setReportd(Reportd reportd) {
        m_reportd = reportd;
    }

}
