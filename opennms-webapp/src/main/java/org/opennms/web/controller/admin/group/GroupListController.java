package org.opennms.web.controller.admin.group;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.groups.Group;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class GroupListController extends AbstractController {

	private GroupManager m_groupManager;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Group> groups = m_groupManager.getGroups();
	    return new ModelAndView("admin/groups/list", "groups", groups.values());
	}

	public GroupManager getGroupManager() {
		return m_groupManager;
	}

	public void setGroupManager(GroupManager groupManager) {
		m_groupManager = groupManager;
	}

}
