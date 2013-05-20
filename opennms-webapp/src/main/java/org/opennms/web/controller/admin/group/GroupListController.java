/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
