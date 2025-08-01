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
    @Override
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
