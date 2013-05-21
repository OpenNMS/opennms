/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service;

import static org.junit.Assert.fail;

import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.ProvisiondConfigurationDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ImportSchedulerTest implements InitializingBean {
    
    @Autowired
    ImportJobFactory m_factory;
    
    @Autowired
    Provisioner m_provisioner;
    
    @Autowired
    ImportScheduler m_importScheduler;
    
    @Autowired
    ProvisiondConfigurationDao m_dao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Test
    public void createJobAndVerifyImportJobFactoryIsRegistered() throws SchedulerException, InterruptedException {
        
        RequisitionDef def = m_dao.getDefs().get(0);
        
        JobDetail detail = new JobDetail("test", ImportScheduler.JOB_GROUP, ImportJob.class, false, false, false);
        detail.getJobDataMap().put(ImportJob.KEY, def.getImportUrlResource());

        
        class MyBoolWrapper {
            volatile Boolean m_called = false;
            
            public Boolean getCalled() {
                return m_called;
            }
            
            public void setCalled(Boolean called) {
                m_called = called;
            }
        }
        
        final MyBoolWrapper callTracker = new MyBoolWrapper();
        
        m_importScheduler.getScheduler().addTriggerListener(new TriggerListener() {
            
            
            @Override
            public String getName() {
                return "TestTriggerListener";
            }

            @Override
            public void triggerComplete(Trigger trigger, JobExecutionContext context, int triggerInstructionCode) {
                LogUtils.infof(this, "triggerComplete called on trigger listener");
                callTracker.setCalled(true);
            }

            @Override
            public void triggerFired(Trigger trigger, JobExecutionContext context) {
                LogUtils.infof(this, "triggerFired called on trigger listener");
                Job jobInstance = context.getJobInstance();
                
                if (jobInstance instanceof ImportJob) {
                    Assert.assertNotNull( ((ImportJob)jobInstance).getProvisioner());
                    Assert.assertTrue(context.getJobDetail().getJobDataMap().containsKey(ImportJob.KEY));
                    Assert.assertEquals("dns://localhost/localhost", context.getJobDetail().getJobDataMap().get(ImportJob.KEY));
                }
                callTracker.setCalled(true);
            }

            @Override
            public void triggerMisfired(Trigger trigger) {
                LogUtils.infof(this, "triggerMisFired called on trigger listener");
                callTracker.setCalled(true);
            }

            @Override
            public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
                LogUtils.infof(this, "vetoJobExecution called on trigger listener");
                callTracker.setCalled(true);
                return false;
            }
            
        });
        
        Calendar testCal = Calendar.getInstance();
        testCal.add(Calendar.SECOND, 5);
        
        Trigger trigger = new SimpleTrigger("test", ImportScheduler.JOB_GROUP, testCal.getTime());
        trigger.addTriggerListener("TestTriggerListener");
        m_importScheduler.getScheduler().scheduleJob(detail, trigger);
        m_importScheduler.start();
        
        int callCheck = 0;
        while (!callTracker.getCalled() && callCheck++ < 2 ) {
            Thread.sleep(5000);
        }
        
        //TODO: need to fix the interrupted exception that occurs in the provisioner
        
    }
    
    @Test
    @Ignore
    public void dwRemoveCurrentJobsFromSchedule() throws SchedulerException {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public void dwBuildImportSchedule() {
        fail("Not yet implemented");
    }

}
