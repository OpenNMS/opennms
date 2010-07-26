/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 14, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * <p>ImportJobFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ImportJobFactory implements JobFactory {

    private Provisioner m_provisioner;

    /** {@inheritDoc} */
    public Job newJob(TriggerFiredBundle bundle) throws SchedulerException {

        JobDetail jobDetail = bundle.getJobDetail();
        Class<ImportJob> jobClass = getJobClass(jobDetail);
        
        ImportJob job = null;
        
        try {
            job = jobClass.newInstance();
            job.setProvisioner(getProvisioner());
            return job;
        } catch (Exception e) {
            SchedulerException se = new SchedulerException("failed to create job class: "+jobDetail.getJobClass().getName()+"; "+
                                                           e.getLocalizedMessage(), e);
            throw se;
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
        m_provisioner = provisioner;
    }
    
    private Provisioner getProvisioner() {
        return m_provisioner;
    }
}

