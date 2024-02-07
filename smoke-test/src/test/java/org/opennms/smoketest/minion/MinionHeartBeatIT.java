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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.MinionDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.DaoUtils;

@Category(MinionTests.class)
public class MinionHeartBeatIT {

	@ClassRule
	public static final OpenNMSStack stack = OpenNMSStack.MINION;

    @Test
	public void minionHeartBeatTestForLastUpdated() {

		final String fs = "Minions";
		final String fid = stack.minion().getId();
		final String location = stack.minion().getLocation();

		final Date startOfTest = new Date();
		final MinionDao minionDao = stack.postgres().dao(MinionDaoHibernate.class);
		final NodeDao nodeDao = stack.postgres().dao(NodeDaoHibernate.class);

		// The heartbeat runs every minute so if we miss the first one, poll long enough
		// to catch the next one
		await().atMost(90, SECONDS)
			   .pollInterval(5, SECONDS)
			   .until(DaoUtils.countMatchingCallable(minionDao, new CriteriaBuilder(OnmsMinion.class)
															 .ge("lastUpdated", startOfTest)
															 .toCriteria()), greaterThan(0));

		await().atMost(180, SECONDS)
			   .pollInterval(5, SECONDS)
			   .until(DaoUtils.countMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class)
															 .eq("foreignSource", fs)
															 .eq("foreignId", fid)
															 .toCriteria()), equalTo(1));

		assertThat(nodeDao.get(fs + ":" + fid).getLocation().getLocationName(), equalTo(location));
	}
}
