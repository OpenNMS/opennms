package org.opennms.netmgt.statsd;

import java.util.Date;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public abstract class AbstractReportInstance implements InitializingBean {

    private ReportDefinition m_reportDefinition;
    private Date m_jobCompletedDate;
    private Date m_jobStartedDate;

    public AbstractReportInstance() {
        super();
    }

    public Date getJobCompletedDate() {
        return m_jobCompletedDate;
    }

    public Date getJobStartedDate() {
        return m_jobStartedDate;
    }

    public String getName() {
        return getReportDefinition().getName();
    }

    public String getDescription() {
        return getReportDefinition().getDescription();
    }

    public long getRetainInterval() {
        return getReportDefinition().getRetainInterval();
    }

    public ReportDefinition getReportDefinition() {
        return m_reportDefinition;
    }

    public void setReportDefinition(ReportDefinition reportDefinition) {
        m_reportDefinition = reportDefinition;
    }

    public void setJobCompletedDate(Date jobCompletedDate) {
        m_jobCompletedDate = jobCompletedDate;
    }

    public void setJobStartedDate(Date jobStartedDate) {
        m_jobStartedDate = jobStartedDate;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        Assert.state(m_reportDefinition != null, "property reportDefinition must be set to a non-null value");
    }

}