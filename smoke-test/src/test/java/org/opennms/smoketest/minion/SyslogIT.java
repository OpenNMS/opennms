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

import java.net.Inet4Address;
import java.util.Date;

import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MinionDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MonitoredServiceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;

/**
 * Verifies that syslog messages sent to the Minion generate
 * events in OpenNMS.
 *
 * @author Seth
 * @author jwhite
 */
public class SyslogIT extends AbstractSyslogTestCase {

    @Test
    public void canReceiveSyslogMessages() throws Exception {
        final Date startOfTest = new Date();

        // Send a syslog packet to the Minion syslog listener
        sendMessage(ContainerAlias.MINION, "myhost", 1);

        // Parsing the message correctly relies on the customized syslogd-configuration.xml that is part of the OpenNMS image
        final EventDao eventDao = getDaoFactory().getDao(EventDaoHibernate.class);
        final Criteria criteria = new CriteriaBuilder(OnmsEvent.class)
                .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                // eventCreateTime is the storage time of the event in the database so 
                // it should be after the start of this test
                .ge("eventCreateTime", startOfTest)
                .toCriteria();

        await().atMost(1, MINUTES).pollInterval(5, SECONDS).until(DaoUtils.countMatchingCallable(eventDao, criteria), greaterThan(0));
    }

    @Test
    public void testNewSuspect() throws Exception {
        final Date startOfTest = new Date();

        final String sender = testEnvironment.getContainerInfo(ContainerAlias.SNMPD).networkSettings().ipAddress();

        // Wait for the minion to show up
        await().atMost(90, SECONDS).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(getDaoFactory().getDao(MinionDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsMinion.class)
                                                             .gt("lastUpdated", startOfTest)
                                                             .eq("location", "MINION")
                                                             .toCriteria()),
                      is(1));

        // Send the initial message
        sendMessage(ContainerAlias.MINION, sender, 1);

        // Wait for the syslog message
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(getDaoFactory().getDao(EventDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsEvent.class)
                                                             .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                                             .ge("eventCreateTime", startOfTest)
                                                             .toCriteria()),
                      is(1));

        //Wait for the new suspect
        final OnmsEvent event = await()
                .atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(getDaoFactory().getDao(EventDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsEvent.class)
                                                             .eq("eventUei", "uei.opennms.org/internal/discovery/newSuspect")
                                                             .ge("eventTime", startOfTest)
                                                             .eq("ipAddr", Inet4Address.getByName(sender))
                                                             .isNull("node")
                                                             .toCriteria()),
                       notNullValue());
        assertThat(event.getDistPoller().getLocation(), is("MINION"));

        // Check if the node was detected
        final OnmsNode node = await()
                .atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(getDaoFactory().getDao(NodeDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsNode.class)
                                                             .eq("label", "snmpd")
                                                             .toCriteria()),
                       notNullValue());
        assertThat(node.getLocation().getLocationName(), is("MINION"));

        // Check if the service was discovered
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
               .until(() -> getDaoFactory().getDao(MonitoredServiceDaoHibernate.class)
                                           .getPrimaryService(node.getId(), "SNMP"),
                      notNullValue());

        // Send the second message
        sendMessage(ContainerAlias.MINION, sender, 1);

        // Wait for the second message with the node assigned
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(getDaoFactory().getDao(EventDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsEvent.class)
                                                             .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                                             .ge("eventCreateTime", startOfTest)
                                                             .eq("node", node)
                                                             .toCriteria()),
                      is(1));
    }
}
