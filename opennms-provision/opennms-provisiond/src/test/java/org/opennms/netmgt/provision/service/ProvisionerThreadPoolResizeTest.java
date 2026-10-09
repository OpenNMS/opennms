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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.dao.api.ProvisiondConfigurationDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.core.test.Level;

/**
 * Verifies that a reloadDaemonConfig event targeted at Provisiond applies the
 * configured import, scan, write and rescan thread counts to the running pools.
 */
public class ProvisionerThreadPoolResizeTest {

    /**
     * A single coordinator for the whole class: each instance starts its own actor
     * thread and offers no shutdown, so creating one per test would leak threads.
     * The named executors are re-pointed at fresh pools before every test.
     */
    private static DefaultTaskCoordinator s_coordinator;

    private ScheduledThreadPoolExecutor m_importExecutor;
    private ScheduledThreadPoolExecutor m_scanExecutor;
    private ScheduledThreadPoolExecutor m_writeExecutor;
    private PausibleScheduledThreadPoolExecutor m_rescanExecutor;

    private ProvisiondConfigurationDao m_configDao;
    private ImportScheduler m_importScheduler;
    private EventForwarder m_eventForwarder;

    private Provisioner m_provisioner;

    @BeforeClass
    public static void setUpCoordinator() {
        s_coordinator = new DefaultTaskCoordinator("ProvisionerThreadPoolResizeTest");
        s_coordinator.setDefaultExecutor(Provisioner.SCAN_EXECUTOR);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging(true);

        m_importExecutor = new ScheduledThreadPoolExecutor(8);
        m_scanExecutor = new ScheduledThreadPoolExecutor(10);
        m_writeExecutor = new ScheduledThreadPoolExecutor(8);
        m_rescanExecutor = new PausibleScheduledThreadPoolExecutor(10);

        final Map<String, Executor> executors = new HashMap<>();
        executors.put(Provisioner.IMPORT_EXECUTOR, m_importExecutor);
        executors.put(Provisioner.SCAN_EXECUTOR, m_scanExecutor);
        executors.put(Provisioner.WRITE_EXECUTOR, m_writeExecutor);
        s_coordinator.setExecutors(executors);

        m_configDao = mock(ProvisiondConfigurationDao.class);
        m_importScheduler = mock(ImportScheduler.class);
        m_eventForwarder = mock(EventForwarder.class);

        m_provisioner = new Provisioner();
        m_provisioner.setTaskCoordinator(s_coordinator);
        m_provisioner.setScheduledExecutor(m_rescanExecutor);
        m_provisioner.setImportSchedule(m_importScheduler);
        m_provisioner.setEventForwarder(m_eventForwarder);
        m_provisioner.setProvisiondConfigDao(m_configDao);
    }

    @After
    public void tearDown() {
        m_importExecutor.shutdownNow();
        m_scanExecutor.shutdownNow();
        m_writeExecutor.shutdownNow();
        m_rescanExecutor.shutdownNow();
        MockLogAppender.resetState();
    }

    @Test
    public void reloadResizesAllFourPools() throws Exception {
        configureThreads(3, 20, 2, 4);

        m_provisioner.handleReloadConfigEvent(reloadEventFor("Provisiond"));

        assertThat(m_importExecutor.getCorePoolSize(), equalTo(3));
        assertThat(m_scanExecutor.getCorePoolSize(), equalTo(20));
        assertThat(m_writeExecutor.getCorePoolSize(), equalTo(2));
        assertThat(m_rescanExecutor.getCorePoolSize(), equalTo(4));

        // The import schedule is rebuilt first, since that is what reloads the configuration
        verify(m_importScheduler).rebuildImportSchedule();
        assertThat(sentUei(), equalTo(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI));
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void reloadLeavesPoolsAloneWhenSizesAreUnchanged() throws Exception {
        configureThreads(8, 10, 8, 10);

        m_provisioner.handleReloadConfigEvent(reloadEventFor("Provisiond"));

        assertThat(m_importExecutor.getCorePoolSize(), equalTo(8));
        assertThat(m_scanExecutor.getCorePoolSize(), equalTo(10));
        assertThat(m_writeExecutor.getCorePoolSize(), equalTo(8));
        assertThat(m_rescanExecutor.getCorePoolSize(), equalTo(10));
        assertThat(sentUei(), equalTo(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI));
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void reloadClampsSizesBelowOneToASingleThread() throws Exception {
        configureThreads(0, -5, 1, 0);

        m_provisioner.handleReloadConfigEvent(reloadEventFor("Provisiond"));

        assertThat(m_importExecutor.getCorePoolSize(), equalTo(1));
        assertThat(m_scanExecutor.getCorePoolSize(), equalTo(1));
        assertThat(m_writeExecutor.getCorePoolSize(), equalTo(1));
        assertThat(m_rescanExecutor.getCorePoolSize(), equalTo(1));
        assertThat(sentUei(), equalTo(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI));
        MockLogAppender.assertLogMatched(Level.WARN, "configured import thread count 0 is less than 1");
        MockLogAppender.assertNoErrorOrGreater();
    }

    @Test
    public void reloadIgnoresEventsTargetedAtOtherDaemons() throws Exception {
        configureThreads(3, 20, 2, 4);

        m_provisioner.handleReloadConfigEvent(reloadEventFor("Pollerd"));

        assertThat(m_importExecutor.getCorePoolSize(), equalTo(8));
        assertThat(m_scanExecutor.getCorePoolSize(), equalTo(10));
        assertThat(m_writeExecutor.getCorePoolSize(), equalTo(8));
        assertThat(m_rescanExecutor.getCorePoolSize(), equalTo(10));
        verify(m_importScheduler, never()).rebuildImportSchedule();
        verify(m_eventForwarder, never()).sendNow(any(Event.class));
    }

    @Test
    public void unregisteredExecutorNamesAreSkippedRatherThanFallingBackToTheDefault() throws Exception {
        // Only the default ("scan") executor is registered; the coordinator's getExecutor()
        // would hand that pool back for "import" and "write" as well.
        final Map<String, Executor> onlyScan = new HashMap<>();
        onlyScan.put(Provisioner.SCAN_EXECUTOR, m_scanExecutor);
        s_coordinator.setExecutors(onlyScan);
        configureThreads(3, 20, 2, 4);

        m_provisioner.handleReloadConfigEvent(reloadEventFor("Provisiond"));

        assertThat(m_scanExecutor.getCorePoolSize(), equalTo(20));
        assertThat(m_importExecutor.getCorePoolSize(), equalTo(8));
        assertThat(m_writeExecutor.getCorePoolSize(), equalTo(8));
        assertThat(m_rescanExecutor.getCorePoolSize(), equalTo(4));
        MockLogAppender.assertLogMatched(Level.WARN, "no import executor is registered");
        MockLogAppender.assertLogMatched(Level.WARN, "no write executor is registered");
        assertThat(sentUei(), equalTo(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI));
    }

    @Test
    public void executorsThatAreNotThreadPoolsAreReportedAndLeftAlone() throws Exception {
        final Executor directExecutor = Runnable::run;
        s_coordinator.addOrUpdateExecutor(Provisioner.WRITE_EXECUTOR, directExecutor);
        configureThreads(3, 20, 2, 4);

        m_provisioner.handleReloadConfigEvent(reloadEventFor("Provisiond"));

        assertThat(m_importExecutor.getCorePoolSize(), equalTo(3));
        assertThat(m_scanExecutor.getCorePoolSize(), equalTo(20));
        assertThat(m_writeExecutor.getCorePoolSize(), equalTo(8));
        MockLogAppender.assertLogMatched(Level.WARN, "write executor is a");
        assertThat(sentUei(), equalTo(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI));
    }

    @Test
    public void resizeWarnsWhenNoConfigDaoIsSet() throws Exception {
        m_provisioner.setProvisiondConfigDao(null);

        m_provisioner.resizeThreadPools();

        assertThat(m_importExecutor.getCorePoolSize(), equalTo(8));
        assertThat(m_rescanExecutor.getCorePoolSize(), equalTo(10));
        MockLogAppender.assertLogMatched(Level.WARN, "no provisiond configuration DAO is set");
    }

    @Test
    public void reloadFailureWithShortMessageStillSendsFailedEvent() throws Exception {
        doThrow(new IllegalStateException("boom")).when(m_importScheduler).rebuildImportSchedule();

        m_provisioner.handleReloadConfigEvent(reloadEventFor("Provisiond"));

        final Event sent = sentEvent();
        assertThat(sent.getUei(), equalTo(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI));
        assertThat(parm(sent, EventConstants.PARM_REASON), equalTo("boom"));
    }

    @Test
    public void describeFailureHandlesMissingAndLongMessages() {
        assertThat(Provisioner.describeFailure(new IllegalStateException()), equalTo(IllegalStateException.class.getName()));
        assertThat(Provisioner.describeFailure(new IllegalStateException("")), equalTo(IllegalStateException.class.getName()));

        final String longMessage = "x".repeat(500);
        assertThat(Provisioner.describeFailure(new IOException(longMessage)).length(), equalTo(128));
    }

    private void configureThreads(final int importThreads, final int scanThreads, final int writeThreads, final int rescanThreads) throws IOException {
        when(m_configDao.getImportThreads()).thenReturn(importThreads);
        when(m_configDao.getScanThreads()).thenReturn(scanThreads);
        when(m_configDao.getWriteThreads()).thenReturn(writeThreads);
        when(m_configDao.getRescanThreads()).thenReturn(rescanThreads);
    }

    private String sentUei() {
        final Event sent = sentEvent();
        assertThat(parm(sent, EventConstants.PARM_DAEMON_NAME), equalTo("Provisiond"));
        return sent.getUei();
    }

    private Event sentEvent() {
        final ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(m_eventForwarder).sendNow(captor.capture());
        return captor.getValue();
    }

    private static String parm(final Event event, final String name) {
        for (final Parm parm : event.getParmCollection()) {
            if (name.equals(parm.getParmName())) {
                return parm.getValue().getContent();
            }
        }
        return null;
    }

    private static IEvent reloadEventFor(final String daemonName) {
        final EventBuilder builder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test");
        builder.addParam(EventConstants.PARM_DAEMON_NAME, daemonName);
        return ImmutableMapper.fromMutableEvent(builder.getEvent());
    }
}
