/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2022 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static com.jayway.awaitility.Awaitility.await;
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
