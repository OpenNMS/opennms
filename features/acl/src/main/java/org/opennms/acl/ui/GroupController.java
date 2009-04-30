//============================================================================
//
// Copyright (c) 2009+ desmax74
// Copyright (c) 2009+ The OpenNMS Group, Inc.
// All rights reserved everywhere.
//
// This program was developed and is maintained by Rocco RIONERO
// ("the author") and is subject to dual-copyright according to
// the terms set in "The OpenNMS Project Contributor Agreement".
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
// USA.
//
// The author can be contacted at the following email address:
//
//       Massimiliano Dess&igrave;
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
package org.opennms.acl.ui;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.opennms.acl.domain.Group;
import org.opennms.acl.exception.AuthorityNotFoundException;
import org.opennms.acl.model.Pager;
import org.opennms.acl.service.GroupService;
import org.opennms.acl.ui.util.WebUtils;
import org.opennms.acl.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Group Controller
 * 
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
@Controller
public class GroupController {

    @RequestMapping("/group.list.page")
    public ModelAndView list(HttpServletRequest req) {
        Pager pager = WebUtils.getPager(req, groupService.getTotalItemsNumber(), 15);
        ModelAndView mav = new ModelAndView("group/list");
        mav.addObject(Constants.GROUPS, groupService.getGroups(pager));
        mav.addObject(Constants.PAGER, pager);
        return mav;
    }

    @RequestMapping("/group.detail.page")
    public ModelAndView detail(HttpServletRequest req) {
        Group group = WebUtils.getGroup(req);
        return new ModelAndView("group/detail", Constants.GROUP, group.getGroupView());
    }

    @RequestMapping("/group.delete.page")
    public ModelAndView delete(HttpServletRequest req) {
        Group group = WebUtils.getGroup(req);
        ModelAndView mav = new ModelAndView(Constants.REDIRECT_GROUP_LIST);
        mav.addObject(Constants.MESSAGE, group.remove() ? Constants.MSG_AUTHORITY_DELETE_SUCCESS : Constants.MSG_AUTHORITY_DELETE_FAILURE);
        return mav;
    }

    @RequestMapping("/group.confirm.page")
    public ModelAndView confirmDelete(HttpServletRequest req) {
        Group group = WebUtils.getGroup(req);
        ModelAndView mav = new ModelAndView("group/detail");
        mav.addObject(Constants.GROUP, group.getGroupView());
        mav.addObject(Constants.UI_MODE, Constants.DELETE);
        return mav;
    }

    @RequestMapping("/group.items.page")
    public ModelAndView items(HttpServletRequest req) {
        Group group = WebUtils.getGroup(req);
        if (group != null) {
            ModelAndView mav = new ModelAndView("group/items");
            mav.addObject(Constants.GROUP, group.getGroupView());
            mav.addObject(Constants.UI_ITEMS, group.getFreeAuthorities());
            mav.addObject(Constants.GROUP_AUTHORITIES, group.getAuthorities());
            return mav;
        } else {
            throw new AuthorityNotFoundException("id not found");
        }
    }

    @RequestMapping("/group.selection.page")
    public ModelAndView selection(@RequestParam("includedHidden") String ids, HttpServletRequest req) {
        Group group = WebUtils.getGroup(req);
        if (group != null && ids.length() > 0) {
            group.setNewAuthorities(WebUtils.extractIdGrantedAuthorityFromString(ids, Constants.COMMA));
        } else {
            group.setNewAuthorities(new ArrayList<Integer>());
        }
        group.save();
        return new ModelAndView(new StringBuilder(Constants.REDIRECT_GROUP_LIST).append("?").append(Constants.GROUP_ID).append("=").append(group.getId()).toString());
    }

    @Autowired
    public GroupController(@Qualifier("groupService") GroupService groupService) {
        this.groupService = groupService;
    }

    private final GroupService groupService;
}
