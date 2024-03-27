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

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThan;

import java.io.IOException;
import java.util.Date;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;

/**
 * Verifies that we can issue scans on the Minion and generate newSuspect events.
 *
 * @author jwhite
 */
@Category(MinionTests.class)
public class DiscoveryIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
			.withOpenNMS(OpenNMSProfile.newBuilder()
					.withFile("empty-discovery-configuration.xml", "etc/discovery-configuration.xml")
					.build())
            .withMinions(MinionProfile.newBuilder()
                    // Enable ICMP support for this test
                    .withIcmpSupportEnabled(true)
                    .build())
            .build());

    @Test
    public void canDiscoverRemoteNodes() throws IOException, InterruptedException {
        Date startOfTest = new Date();

        final HttpHost opennmsHttpHost = new HttpHost(stack.opennms().getContainerIpAddress(), stack.opennms().getWebPort());

        HttpClient instance = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy()) // Ignore the 302 response to the POST
                .build();

        Executor executor = Executor.newInstance(instance)
                .auth(opennmsHttpHost, OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD)
                .authPreemptive(opennmsHttpHost);

        // Configure Discovery with the specific address of our Tomcat server
        // No REST endpoint is currently available to configure the Discovery daemon
        // so we resort to POSTin nasty form data
        executor.execute(Request.Post(String.format("http://%s:%d/opennms/admin/discovery/actionDiscovery?action=AddSpecific",
                opennmsHttpHost.getHostName(), opennmsHttpHost.getPort()))
            .bodyForm(Form.form()
                    .add("specificipaddress", stack.opennms().getContainerIpAddress())
                    .add("specifictimeout", "2000")
                    .add("specificretries", "1")
                    .add("initialsleeptime", "30000")
                    .add("restartsleeptime", "86400000")
                    .add("foreignsource", "NODES")
                    .add("location", stack.minion().getLocation())
                    .add("retries", "1")
                    .add("timeout", "2000")
                    .build())).returnContent();

        executor.execute(Request.Post(String.format("http://%s:%d/opennms/admin/discovery/actionDiscovery?action=SaveAndRestart",
                opennmsHttpHost.getHostName(), opennmsHttpHost.getPort()))
            .bodyForm(Form.form()
                    .add("initialsleeptime", "1")
                    .add("restartsleeptime", "86400000")
                    .add("foreignsource", "NODES")
                    .add("location", stack.minion().getLocation())
                    .add("retries", "1")
                    .add("timeout", "2000")
                    .build())).returnContent();

        HibernateDaoFactory daoFactory = stack.postgres().getDaoFactory();
        EventDao eventDao = daoFactory.getDao(EventDaoHibernate.class);

        Criteria criteria = new CriteriaBuilder(OnmsEvent.class)
                .eq("eventUei", EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI)
                .ge("eventTime", startOfTest)
                .toCriteria();

        await().atMost(1, MINUTES).pollInterval(10, SECONDS)
                .until(DaoUtils.countMatchingCallable(eventDao, criteria), greaterThan(0));
    }
}
