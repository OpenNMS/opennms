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

import com.google.common.collect.Lists;
import org.junit.*;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.api.ProvisiondConfigurationDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath:/META-INF/opennms/applicationContext-snmp-profile-mapper.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-dns.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/mockForeignSourceContext.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
public class ImportSchedulerIT implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(ImportSchedulerIT.class);
    
    @Autowired
    ImportJobFactory m_factory;
    
    @Autowired
    Provisioner m_provisioner;
    
    @Autowired
    ImportScheduler m_importScheduler;
    
    @Autowired
    ProvisiondConfigurationDao m_dao;

    @Autowired
    MockEventIpcManager m_mockEventIpcManager;

    @Autowired
    private SecureCredentialsVault secureCredentialsVault;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Before
    public void setUp() throws IOException, JAXBException {
        MockLogAppender.setupLogging();
        secureCredentialsVault.setCredentials("requisition", new Credentials("admin", "admin"));
    }

    @After
    public void tearDown() {
        try {
            final ListenerManager listenerManager = m_importScheduler.getScheduler().getListenerManager();
            final List<String> triggerListeners = listenerManager.getTriggerListeners().stream().map(tl -> tl.getName()).collect(Collectors.toList());
            triggerListeners.forEach(tlName -> listenerManager.removeTriggerListener(tlName));
        } catch (final Exception e) {
            LOG.warn("Failed to clean up existing trigger listeners.", e);
        }
        try {
            m_importScheduler.getScheduler().clear();
        } catch (SchedulerException e) {
            LOG.warn("Failed to clear existing scheduler.", e);
        }
    }

    @Test
    @JUnitTemporaryDatabase
    public void createJobAndVerifyImportJobFactoryIsRegistered() throws SchedulerException, InterruptedException, IOException {
        
        RequisitionDef def = m_dao.getDefs().get(0);
        
        JobDetail detail = JobBuilder.newJob(ImportJob.class).withIdentity("test", ImportScheduler.JOB_GROUP).storeDurably(false).requestRecovery(false).build();
        detail.getJobDataMap().put(ImportJob.URL, def.getImportUrlResource().orElse(null));
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
        
        m_importScheduler.getScheduler().getListenerManager().addTriggerListener(new TriggerListener() {
            
            
            @Override
            public String getName() {
                return "TestTriggerListener";
            }

            @Override
            public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
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

        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("test", ImportScheduler.JOB_GROUP).startAt(testCal.getTime()).build();
        m_importScheduler.getScheduler().scheduleJob(detail, trigger);
        m_importScheduler.start();
        
        int callCheck = 0;
        while (!callTracker.getCalled() && callCheck++ < 2 ) {
            Thread.sleep(5000);
        }
        
        //TODO: need to fix the interrupted exception that occurs in the provisioner
        
    }

    @Test
    @JUnitTemporaryDatabase
    public void buildImportSchedule() throws SchedulerException, InterruptedException, IOException {
        // Add a simple definition to the configuration that attempts
        // to import a non existent file every 5 seconds
        RequisitionDef def = new RequisitionDef();
        // Every 5 seconds
        def.setCronSchedule("*/5 * * * * ? *");
        def.setImportName("test");
        def.setImportUrlResource("file:///tmp/should-not-exist.xml");
        def.setRescanExisting(Boolean.FALSE.toString());

        m_dao.getConfig().setRequisitionDefs(Lists.newArrayList(def));

        // The import should start, and then fail
        EventAnticipator anticipator = m_mockEventIpcManager.getEventAnticipator();
        EventBuilder builder = new EventBuilder(EventConstants.IMPORT_STARTED_UEI, "Provisiond");
        anticipator.anticipateEvent(builder.getEvent());

        builder = new EventBuilder(EventConstants.IMPORT_FAILED_UEI, "Provisiond");
        anticipator.anticipateEvent(builder.getEvent());

        // Go
        m_importScheduler.buildImportSchedule();
        m_importScheduler.start();

        // Verify
        anticipator.waitForAnticipated(10*1000);
        anticipator.verifyAnticipated();
    }

    @Test
    @JUnitTemporaryDatabase
    public void buildHttpImportSchedule() throws SchedulerException, IOException, InterruptedException {
        JobDetail detail = JobBuilder.newJob(ImportJob.class).withIdentity("test", ImportScheduler.JOB_GROUP).storeDurably(false).requestRecovery(false).build();
        detail.getJobDataMap().put(ImportJob.URL, "http://${scv:requisition:username}:${scv:requisition:password}@localhost:8980/opennms/rest/requisitions/test");
        detail.getJobDataMap().put(ImportJob.RESCAN_EXISTING, Boolean.FALSE.toString());
        String expectedUrl = "http://admin:admin@localhost:8980/opennms/rest/requisitions/test";

        CountDownLatch latch = new CountDownLatch(1);

        m_importScheduler.getScheduler().getListenerManager().addTriggerListener(new TriggerListener() {
            @Override
            public String getName() {
                return "TestTriggerListener";
            }

            @Override
            public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
                LOG.info("triggerComplete called on trigger listener");
            }

            @Override
            public void triggerFired(Trigger trigger, JobExecutionContext context) {
                LOG.info("triggerFired called on trigger listener");
                Job jobInstance = context.getJobInstance();

                if (jobInstance instanceof ImportJob) {
                    ImportJob importJob = (ImportJob) jobInstance;
                    String actualUrl = importJob.interpolate(context.getJobDetail().getJobDataMap().getString(ImportJob.URL));
                    Assert.assertEquals("Interpolated URL did not match expected value.", expectedUrl, actualUrl);
                }
                latch.countDown();
            }

            @Override
            public void triggerMisfired(Trigger trigger) {
                LOG.info("triggerMisFired called on trigger listener");
                Assert.fail("Trigger misfired — job was not executed.");
            }

            @Override
            public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
                LOG.info("vetoJobExecution called on trigger listener");
                return false;
            }
        });

        Calendar testCal = Calendar.getInstance();
        testCal.add(Calendar.SECOND, 5);
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("test", ImportScheduler.JOB_GROUP).startAt(testCal.getTime()).build();
        m_importScheduler.getScheduler().scheduleJob(detail, trigger);
        m_importScheduler.start();

        // Wait max 30 seconds for the listener to be called
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        Assert.assertTrue("Trigger listener was never called.", completed);
    }
}
