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
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.OpenNMSStack;
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
        SnmpV3TrapBuilder pdu = SnmpUtils.getV3TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"),
                SnmpUtils.getValueFactory().getObjectId(SnmpObjId.get(".1.3.6.1.6.3.1.1.5.4.0")));
        pdu.send(InetAddressUtils.str(snmpAddress.getAddress()), snmpAddress.getPort(), SnmpConfiguration.AUTH_PRIV, "traptest",
        "0p3nNMSv3", "SHA-256", "0p3nNMSv3", "DES");
        LOG.info("V3 trap sent successfully");
    }

}
