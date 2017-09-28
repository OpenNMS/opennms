/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.fail;

import java.util.Calendar;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.api.ProvisiondConfigurationDao;
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
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/mockForeignSourceContext.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
public class ImportSchedulerTest implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(ImportSchedulerTest.class);
    
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
        detail.getJobDataMap().put(ImportJob.URL, def.getImportUrlResource());
        detail.getJobDataMap().put(ImportJob.RESCAN_EXISTING, def.getRescanExisting());

        
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
                LOG.info("triggerComplete called on trigger listener");
                callTracker.setCalled(true);
            }

            @Override
            public void triggerFired(Trigger trigger, JobExecutionContext context) {
                LOG.info("triggerFired called on trigger listener");
                Job jobInstance = context.getJobInstance();
                
                if (jobInstance instanceof ImportJob) {
                    Assert.assertNotNull( ((ImportJob)jobInstance).getProvisioner());
                    Assert.assertTrue(context.getJobDetail().getJobDataMap().containsKey(ImportJob.URL));
                    Assert.assertEquals("dns://localhost/localhost", context.getJobDetail().getJobDataMap().get(ImportJob.URL));
                    Assert.assertTrue(context.getJobDetail().getJobDataMap().containsKey(ImportJob.RESCAN_EXISTING));
                    Assert.assertEquals("dbonly", context.getJobDetail().getJobDataMap().get(ImportJob.RESCAN_EXISTING));
                }
                callTracker.setCalled(true);
            }

            @Override
            public void triggerMisfired(Trigger trigger) {
                LOG.info("triggerMisFired called on trigger listener");
                callTracker.setCalled(true);
            }

            @Override
            public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
                LOG.info("vetoJobExecution called on trigger listener");
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
