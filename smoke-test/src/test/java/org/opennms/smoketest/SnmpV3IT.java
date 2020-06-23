/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnmpV3IT {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpV3IT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Test
    public void testSnmpV3Traps() {
        Date startOfTest = new Date();
        final InetSocketAddress snmpAddress = stack.opennms().getNetworkProtocolAddress(NetworkProtocol.SNMP);
        HibernateDaoFactory daoFactory = stack.postgres().getDaoFactory();
        AlarmDao alarmDao = daoFactory.getDao(AlarmDaoHibernate.class);

        Criteria criteria = new CriteriaBuilder(OnmsAlarm.class)
                .eq("uei", "uei.opennms.org/generic/traps/EnterpriseDefault").ge("lastEventTime", startOfTest)
                .toCriteria();

        try {
            executor.scheduleWithFixedDelay(() -> {
                try {
                    sendV3Trap(snmpAddress);
                } catch (Exception e) {
                    LOG.error("Exception while sending trap.", e);
                }
            }, 0, 5, TimeUnit.SECONDS);
            // Check if there is at least one alarm
            await().atMost(30, SECONDS).pollInterval(5, SECONDS).pollDelay(5, SECONDS)
                    .until(DaoUtils.countMatchingCallable(alarmDao, criteria), greaterThanOrEqualTo(1));
            // Check if multiple traps are getting received not just the first one
            await().atMost(30, SECONDS).pollInterval(5, SECONDS).pollDelay(5, SECONDS)
                    .until(DaoUtils.findMatchingCallable(alarmDao, new CriteriaBuilder(OnmsAlarm.class)
                                    .eq("uei", "uei.opennms.org/generic/traps/EnterpriseDefault").ge("counter", 3).toCriteria()),
                            notNullValue());
        } finally {
            // Make sure we always shutdown the thread pool, even when the test fails
            executor.shutdownNow();
        }
    }

    private void sendV3Trap(InetSocketAddress snmpAddress) throws Exception {
        SnmpTrapBuilder pdu = SnmpUtils.getV3TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"),
                SnmpUtils.getValueFactory().getObjectId(SnmpObjId.get(".1.3.6.1.6.3.1.1.5.4.0")));
        pdu.send(InetAddressUtils.str(snmpAddress.getAddress()), snmpAddress.getPort(), "traptest");
        LOG.info("V3 trap sent successfully");
    }

}
