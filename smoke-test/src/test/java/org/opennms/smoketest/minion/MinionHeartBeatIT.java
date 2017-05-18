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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.net.InetSocketAddress;
import java.util.Date;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.MinionDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;

public class MinionHeartBeatIT {
	private static TestEnvironment m_testEnvironment;

	@ClassRule
	public static final TestEnvironment getTestEnvironment() {
		if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
			return new NullTestEnvironment();
		}
		try {
			final TestEnvironmentBuilder builder = TestEnvironment.builder().all();
			OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
			m_testEnvironment = builder.build();
			return m_testEnvironment;
		} catch (final Throwable t) {
			throw new RuntimeException(t);
		}
	}

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    @Test
	public void minionHeartBeatTestForLastUpdated() {

		Date startOfTest = new Date();
		InetSocketAddress pgsql = m_testEnvironment.getServiceAddress(ContainerAlias.POSTGRES, 5432);
		HibernateDaoFactory daoFactory = new HibernateDaoFactory(pgsql);
		MinionDao minionDao = daoFactory.getDao(MinionDaoHibernate.class);
		NodeDao nodeDao = daoFactory.getDao(NodeDaoHibernate.class);

		// The heartbeat runs every minute so if we miss the first one, poll long enough
		// to catch the next one
		await().atMost(90, SECONDS)
			   .pollInterval(5, SECONDS)
			   .until(DaoUtils.countMatchingCallable(minionDao,
													 new CriteriaBuilder(OnmsMinion.class).ge("lastUpdated", startOfTest).toCriteria()),
					  greaterThan(0));

		await().atMost(180, SECONDS)
			   .pollInterval(5, SECONDS)
			   .until(DaoUtils.countMatchingCallable(nodeDao,
													 new CriteriaBuilder(OnmsNode.class).eq("foreignSource", "Minions")
																						.eq("foreignId", "00000000-0000-0000-0000-000000ddba11")
																						.toCriteria()),
					  equalTo(1));

		Assert.assertEquals("MINION", nodeDao.get("Minions:00000000-0000-0000-0000-000000ddba11").getLocation().getLocationName());
	}
}
