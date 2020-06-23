/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MinionDaoHibernate;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.SyslogUtils;

/**
 * Verifies that syslog messages sent to the Minion generate
 * events in OpenNMS.
 *
 * @author Seth
 * @author jwhite
 */
public class SyslogIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINION;

    @Test
    public void canReceiveSyslogMessages() throws Exception {
        final Date startOfTest = new Date();

        // Send a syslog packet to the Minion syslog listener
        SyslogUtils.sendMessage(stack.minion().getSyslogAddress(), "myhost", 1);

        // Parsing the message correctly relies on the customized syslogd-configuration.xml that is part of the OpenNMS image
        final EventDao eventDao = stack.postgres().dao(EventDaoHibernate.class);
        final Criteria criteria = new CriteriaBuilder(OnmsEvent.class)
                .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                // eventCreateTime is the storage time of the event in the database so 
                // it should be after the start of this test
                .ge("eventCreateTime", startOfTest)
                .toCriteria();

        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.countMatchingCallable(eventDao, criteria), greaterThan(0));
    }

    @Test
    public void testNewSuspect() throws Exception {
        final Date startOfTest = new Date();

        final String location = stack.minion().getLocation();
        final String sender = OpenNMSContainer.DB_ALIAS;

        // Wait for the minion to show up
        await().atMost(90, SECONDS).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(stack.postgres().dao(MinionDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsMinion.class)
                                                             .gt("lastUpdated", startOfTest)
                                                             .eq("location", location)
                                                             .toCriteria()),
                      is(1));

        // Send the initial message
        SyslogUtils.sendMessage(stack.minion().getSyslogAddress(), sender, 1);

        // Wait for the syslog message
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(stack.postgres().dao(EventDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsEvent.class)
                                                             .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                                             .ge("eventCreateTime", startOfTest)
                                                             .toCriteria()),
                      is(1));

        //Wait for a new suspect
        try {
            final OnmsEvent event = await()
                    .atMost(1, MINUTES).pollInterval(5, SECONDS)
                    .until(DaoUtils.findMatchingCallable(stack.postgres().dao(EventDaoHibernate.class),
                            new CriteriaBuilder(OnmsEvent.class)
                                    .eq("eventUei", "uei.opennms.org/internal/discovery/newSuspect")
                                    .ge("eventTime", startOfTest)
                                    .isNull("node")
                                    .toCriteria()),
                            notNullValue());
            assertThat(event.getDistPoller().getLocation(), is(location));
        } catch (Exception e) {
            Thread.sleep(TimeUnit.DAYS.toMillis(1));
        }

        // Wait for a node to be added
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(stack.postgres().dao(EventDaoHibernate.class),
                        new CriteriaBuilder(OnmsEvent.class)
                                .eq("eventUei", "uei.opennms.org/nodes/nodeAdded")
                                .ge("eventTime", startOfTest)
                                .toCriteria()),
                        notNullValue());

        // Send the second message
        SyslogUtils.sendMessage(stack.minion().getSyslogAddress(), sender, 1);

        // Wait for the second message with the node assigned
        final OnmsEvent eventWithNode = await().atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(stack.postgres().dao(EventDaoHibernate.class),
                        new CriteriaBuilder(OnmsEvent.class)
                                .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                .ge("eventCreateTime", startOfTest)
                                .isNotNull("node")
                                .toCriteria()),
                        notNullValue());
    }

}
