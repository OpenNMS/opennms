/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;

/**
 * Verifies that we can issue scans on the Minion and generate newSuspect events.
 *
 * @author jwhite
 */
public class DiscoveryIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
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
