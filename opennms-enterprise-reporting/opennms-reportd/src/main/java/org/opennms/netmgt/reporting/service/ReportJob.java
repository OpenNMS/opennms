package org.opennms.netmgt.reporting.service;

import org.opennms.netmgt.config.reportd.Report;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <p>ReportJob class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportJob implements Job {

    
    /** Constant <code>KEY="report"</code> */
    protected static final String KEY = "report";

    private Reportd m_reportd;

    
    /** {@inheritDoc} */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {
            m_reportd.runReport((Report)context.getJobDetail().getJobDataMap().get(KEY));
    }


    /**
     * <p>getReportd</p>
     *
     * @return a {@link org.opennms.netmgt.reporting.service.Reportd} object.
     */
    public Reportd getReportd() {
        return m_reportd;
    }


    /**
     * <p>setReportd</p>
     *
     * @param reportd a {@link org.opennms.netmgt.reporting.service.Reportd} object.
     */
    public void setReportd(Reportd reportd) {
        m_reportd = reportd;
    }

}
