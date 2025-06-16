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
package org.opennms.smoketest;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.opennms.netmgt.events.api.EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI;

import java.io.PrintStream;
import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.daemon.DaemonReloadEnum;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReloadDaemonIT {
    private static final Logger LOG = LoggerFactory.getLogger(ReloadDaemonIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Test
    public void reloadDaemonTest() throws Exception {
        for (DaemonReloadEnum daemonReloadEnum : DaemonReloadEnum.values()) {
            reloadDaemon(daemonReloadEnum.getDaemonName());
        }
    }

    public void reloadDaemon(final String daemonName) throws Exception {
        final Date startOfTest = new Date();

        try (final SshClient sshClient = new SshClient(stack.opennms().getSshAddress(), "admin", "admin")) {
            final PrintStream pipe = sshClient.openShell();
            pipe.println("opennms:reload-daemon " + daemonName);
            pipe.println("logout");
            await().atMost(30, SECONDS).until(sshClient.isShellClosedCallable());
        }

        final EventDao eventDao = stack.postgres().getDaoFactory().getDao(EventDaoHibernate.class);
        await().atMost(2, MINUTES).pollInterval(2, SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                long eventCount = eventDao.findMatching(
                        new CriteriaBuilder(OnmsEvent.class)
                                .alias("eventParameters", "eventParameters")
                                .ge("eventCreateTime", startOfTest)
                                .eq("eventUei", RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI)
                                .toCriteria()
                ).stream()
                        .filter(e -> e.getEventParameters().stream()
                                .anyMatch(p -> EventConstants.PARM_DAEMON_NAME.equals(p.getName()) && daemonName.equals(p.getValue())))
                        .count();
                LOG.info("Waiting for {} event (count = {})", daemonName, eventCount);
                return eventCount > 0;
            }
        });
    }
}
