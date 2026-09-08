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
package org.opennms.smoketest.minion;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MinionDaoHibernate;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.SyslogUtils;
import org.opennms.smoketest.utils.TestContainerUtils;

/**
 * Sends syslog to a Minion over TCP in both RFC 6587 framings and checks the events reach
 * the core.
 *
 * The default Minion profile leaves the TCP port unset, so this builds its own stack with
 * the .cfg that switches it on rather than sharing the one SyslogIT uses.
 */
@Category(org.opennms.smoketest.junit.MinionTests.class)
public class SyslogTcpIT {

    private static final String SYSLOG_UEI =
            "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic";

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinions(MinionProfile.newBuilder()
                    // Destination is relative to the Minion's etc, since the overlay is rsynced into it.
                    .withFile("syslog-tcp/org.opennms.netmgt.syslog.cfg", "org.opennms.netmgt.syslog.cfg")
                    .build())
            .build());

    /**
     * Both framings in one method, counting cumulatively. Splitting them into two tests
     * would have each counting events by creation time within a window the other test may
     * still be inside.
     */
    @Test
    public void canReceiveSyslogOverTcpInBothFramings() throws Exception {
        final int count = 5;
        final Date startOfTest = new Date();
        final String sender = TestContainerUtils.getInternalIpAddress(stack.postgres());

        awaitMinion(startOfTest);

        // Each call opens its own connection, so each batch gets its own framing detection.
        SyslogUtils.sendMessageOverTcp(stack.minion().getSyslogTcpAddress(), sender, count, false);
        awaitEventCount(startOfTest, count);

        SyslogUtils.sendMessageOverTcp(stack.minion().getSyslogTcpAddress(), sender, count, true);
        awaitEventCount(startOfTest, count * 2);
    }

    /**
     * Exact count, not "at least". Over-delivery is what a framing bug produces, and a
     * greater-than assertion would pass straight over one message becoming several.
     */
    private void awaitEventCount(final Date startOfTest, final int expected) throws Exception {
        final Callable<Integer> count = DaoUtils.countMatchingCallable(
                stack.postgres().dao(EventDaoHibernate.class),
                new CriteriaBuilder(OnmsEvent.class)
                        .eq("eventUei", SYSLOG_UEI)
                        .ge("eventCreateTime", startOfTest)
                        .toCriteria());

        await().atMost(2, MINUTES).pollInterval(5, SECONDS).until(count, is(expected));

        // Awaitility stops the moment the count matches, so a duplicate arriving just after
        // would go unnoticed. Give it a chance to show up and assert the count again.
        Thread.sleep(10_000);
        assertThat("more events arrived than were sent", count.call(), is(expected));
    }

    private void awaitMinion(final Date startOfTest) {
        await().atMost(90, SECONDS).pollInterval(5, SECONDS)
                .until(DaoUtils.countMatchingCallable(
                        stack.postgres().getDaoFactory().getDao(MinionDaoHibernate.class),
                        new CriteriaBuilder(OnmsMinion.class)
                                .gt("lastUpdated", startOfTest)
                                .eq("location", stack.minion().getLocation())
                                .toCriteria()),
                        is(1));
    }
}
