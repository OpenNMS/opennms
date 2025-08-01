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
package org.opennms.netmgt.dao.mock;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.PathOutageDao;
import org.opennms.netmgt.model.OnmsPathOutage;

public class MockPathOutageDao extends AbstractMockDao<OnmsPathOutage, Integer> implements PathOutageDao {

	private AtomicInteger m_id = new AtomicInteger(0);

	@Override
	protected void generateId(final OnmsPathOutage outage) {
		outage.setNodeId(m_id.incrementAndGet());
	}

	@Override
	protected Integer getId(final OnmsPathOutage outage) {
		return outage.getNodeId();
	}

	@Override
	public List<Integer> getNodesForPathOutage(InetAddress ipAddress, String serviceName) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public List<Integer> getNodesForPathOutage(OnmsPathOutage pathOutage) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public List<Integer> getAllNodesDependentOnAnyServiceOnInterface(InetAddress ipAddress) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public List<Integer> getAllNodesDependentOnAnyServiceOnNode(int nodeId) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public List<String[]> getAllCriticalPaths() {
		throw new UnsupportedOperationException("Not yet implemented!");
	}
}
