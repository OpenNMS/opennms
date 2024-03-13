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
package org.opennms.netmgt.provision.service;

import org.opennms.core.mate.api.EntityScopeProvider;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>ImportJobFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ImportJobFactory implements JobFactory {

    private Provisioner provisioner;

    @Autowired
    private MonitorHolder monitorHolder;

    @Autowired
    private EntityScopeProvider entityScopeProvider;

    /** {@inheritDoc} */
    @Override
    public Job newJob(final TriggerFiredBundle bundle, final Scheduler scheduler) throws SchedulerException {

        JobDetail jobDetail = bundle.getJobDetail();
        Class<ImportJob> jobClass = getJobClass(jobDetail);
        
        ImportJob job = null;
        
        try {
            job = jobClass.getDeclaredConstructor().newInstance();
            job.setProvisioner(getProvisioner());
            job.setMonitor(monitorHolder.createMonitor(jobDetail.getKey().getName(), job));
            job.setEntityScopeProvider(entityScopeProvider);

            return job;
        } catch (Exception e) {
            throw new SchedulerException("failed to create job class: "+jobDetail.getJobClass().getName()+"; "+
                                                           e.getLocalizedMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<ImportJob> getJobClass(JobDetail jobDetail) {
        return (Class<ImportJob>)jobDetail.getJobClass();
    }

    /**
     * <p>setProvisioner</p>
     *
     * @param provisioner a {@link org.opennms.netmgt.provision.service.Provisioner} object.
     */
    public void setProvisioner(Provisioner provisioner) {
        this.provisioner = provisioner;
    }
    
    private Provisioner getProvisioner() {
        return provisioner;
    }
}

