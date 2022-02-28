/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service;

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

