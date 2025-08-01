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
package org.opennms.netmgt.statsd;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


/**
 * <p>Abstract AbstractReportInstance class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public abstract class AbstractReportInstance implements ReportInstance, InitializingBean {

    private ReportDefinition m_reportDefinition;
    private Date m_jobCompletedDate;
    private Date m_jobStartedDate;

    /**
     * <p>getJobCompletedDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Override
    public Date getJobCompletedDate() {
        return m_jobCompletedDate;
    }

    /**
     * <p>getJobStartedDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Override
    public Date getJobStartedDate() {
        return m_jobStartedDate;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return getReportDefinition().getName();
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getDescription() {
        return getReportDefinition().getDescription();
    }

    /**
     * <p>getRetainInterval</p>
     *
     * @return a long.
     */
    @Override
    public long getRetainInterval() {
        return getReportDefinition().getRetainInterval();
    }

    /**
     * <p>getReportDefinition</p>
     *
     * @return a {@link org.opennms.netmgt.statsd.ReportDefinition} object.
     */
    @Override
    public ReportDefinition getReportDefinition() {
        return m_reportDefinition;
    }

    /**
     * <p>setReportDefinition</p>
     *
     * @param reportDefinition a {@link org.opennms.netmgt.statsd.ReportDefinition} object.
     */
    @Override
    public void setReportDefinition(ReportDefinition reportDefinition) {
        m_reportDefinition = reportDefinition;
    }

    /**
     * <p>setJobCompletedDate</p>
     *
     * @param jobCompletedDate a {@link java.util.Date} object.
     */
    public void setJobCompletedDate(Date jobCompletedDate) {
        m_jobCompletedDate = jobCompletedDate;
    }

    /**
     * <p>setJobStartedDate</p>
     *
     * @param jobStartedDate a {@link java.util.Date} object.
     */
    public void setJobStartedDate(Date jobStartedDate) {
        m_jobStartedDate = jobStartedDate;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#afterPropertiesSet()
     */
    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_reportDefinition != null, "property reportDefinition must be set to a non-null value");
    }
    
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this);
        tsb.append("name", getName());
        tsb.append("description", getDescription());
        tsb.append("retainInterval", getRetainInterval());
        return tsb.toString();
    }

}
