package org.opennms.netmgt.reporting.service;


import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.spi.JobFactory;
import org.quartz.SchedulerException;
import org.quartz.spi.TriggerFiredBundle;

/**
 * <p>ReportJobFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportJobFactory implements JobFactory {

    private Reportd m_reportd;

    /** {@inheritDoc} */
    public Job newJob(TriggerFiredBundle bundle) throws SchedulerException {

        JobDetail jobDetail = bundle.getJobDetail();
        Class<ReportJob> jobClass = getJobClass(jobDetail);
        
        ReportJob job = null;
        
        try {
            job = jobClass.newInstance();
            job.setReportd(getReportd());
            return job;
        } catch (Exception e) {
            SchedulerException se = new SchedulerException("failed to create job class: "+ jobDetail.getJobClass().getName()+"; "+
                                                           e.getLocalizedMessage(), e);
            throw se;
        }
    }

    @SuppressWarnings("unchecked")
    private Class<ReportJob> getJobClass(JobDetail jobDetail) {
        return (Class<ReportJob>)jobDetail.getJobClass();
    }

    /**
     * <p>setReportd</p>
     *
     * @param reportd a {@link org.opennms.netmgt.reporting.service.Reportd} object.
     */
    public void setReportd(Reportd reportd) {
        m_reportd = reportd;
    }
    
    private Reportd getReportd() {
        return m_reportd;
    }

}
