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

import static org.junit.Assert.fail;

import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.ProvisiondConfigurationDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    JUnitSnmpAgentExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitTemporaryDatabase()
public class ImportSchedulerTest {
    
    @Autowired
    ImportJobFactory m_factory;
    
    @Autowired
    Provisioner m_provisioner;
    
    @Autowired
    ImportScheduler m_importScheduler;
    
    @Autowired
    ProvisiondConfigurationDao m_dao;

    @Before
    public void verifyWiring() {
        Assert.assertNotNull(m_importScheduler);
        Assert.assertNotNull(m_factory);
        Assert.assertNotNull(m_provisioner);
        Assert.assertNotNull(m_dao);
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
            
            
            public String getName() {
                return "TestTriggerListener";
            }

            public void triggerComplete(Trigger trigger, JobExecutionContext context, int triggerInstructionCode) {
                System.err.println("triggerComplete called on trigger listener");
                callTracker.setCalled(true);
            }

            public void triggerFired(Trigger trigger, JobExecutionContext context) {
                System.err.println("triggerFired called on trigger listener");
                Job jobInstance = context.getJobInstance();
                
                if (jobInstance instanceof ImportJob) {
                    Assert.assertNotNull( ((ImportJob)jobInstance).getProvisioner());
                    Assert.assertTrue(context.getJobDetail().getJobDataMap().containsKey(ImportJob.KEY));
                    Assert.assertEquals("dns://localhost/localhost", context.getJobDetail().getJobDataMap().get(ImportJob.KEY));
                }
                callTracker.setCalled(true);
            }

            public void triggerMisfired(Trigger trigger) {
                System.err.println("triggerMisFired called on trigger listener");
                callTracker.setCalled(true);
            }

            public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
                System.err.println("vetoJobExecution called on trigger listener");
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
