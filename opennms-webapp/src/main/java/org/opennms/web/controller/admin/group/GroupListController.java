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
package org.opennms.web.controller.admin.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.groups.Group;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>GroupListController class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class GroupListController extends AbstractController {

	private GroupManager m_groupManager;

	/** {@inheritDoc} */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Group> groups = m_groupManager.getGroups();
		List<Group> groupList = new ArrayList<Group>(groups.values());
		Collections.sort(groupList, new Comparator<Group>() {
                    @Override
		    public int compare(Group g1, Group g2) {
		        return g1.getName().toLowerCase().compareTo(g2.getName().toLowerCase());
		    }
		});
		return new ModelAndView("admin/userGroupView/groups/list", "groups", groupList);
	}

	/**
	 * <p>getGroupManager</p>
	 *
	 * @return a {@link org.opennms.netmgt.config.GroupManager} object.
	 */
	public GroupManager getGroupManager() {
		return m_groupManager;
	}

	/**
	 * <p>setGroupManager</p>
	 *
	 * @param groupManager a {@link org.opennms.netmgt.config.GroupManager} object.
	 */
	public void setGroupManager(GroupManager groupManager) {
		m_groupManager = groupManager;
	}

}
