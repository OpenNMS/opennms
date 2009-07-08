/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * Copyright (C) 2009 The OpenNMS Group, Inc.
 * All rights reserved.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.acl.ui;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.opennms.acl.domain.GenericUser;
import org.opennms.acl.exception.UserNotfoundException;
import org.opennms.acl.model.Pager;
import org.opennms.acl.service.UserService;
import org.opennms.acl.ui.util.WebUtils;
import org.opennms.acl.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * User Controller
 * 
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
@Controller
public class UserController {

    @RequestMapping("/user.authorities.page")
    public ModelAndView authorities(HttpServletRequest req) throws Exception {
        GenericUser user = WebUtils.getUser(req);
        return new ModelAndView("user/authorities", Constants.UI_USER, user.getUserView());
    }

    @RequestMapping("/user.list.page")
    public ModelAndView list(HttpServletRequest req) throws Exception {
        Pager pager = WebUtils.getPager(req, userService.getTotalItemsNumber(), 15);
        return new ModelAndView("user/list", Constants.UI_USERS, userService.getEnabledUsers(pager));
    }

    @RequestMapping("/user.detail.page")
    public ModelAndView detail(HttpServletRequest req) throws Exception {
        GenericUser user = WebUtils.getUser(req);
        if (user != null) {
            ModelAndView mav = new ModelAndView("user/detail");
            mav.addObject(Constants.UI_USER, user.getUserView());
            mav.addObject(Constants.UI_USER_GROUPS, user.getGroups());
            mav.addObject(Constants.GROUPS, user.getFreeGroups());
            return mav;
        } else {
            throw new UserNotfoundException("id not found");
        }
    }

    @RequestMapping("/user.selection.page")
    public ModelAndView selection(@RequestParam("includedHidden") String ids, HttpServletRequest req) throws Exception {
        GenericUser user = WebUtils.getUser(req);
        if (user != null && ids != null && ids.length() > 0) {
            user.setNewGroups(WebUtils.extractIdGrantedAuthorityFromString(ids, Constants.COMMA));
        } else {
            user.setNewGroups(new ArrayList<Integer>());
        }
        user.save();
        return new ModelAndView(new StringBuilder(Constants.REDIRECT_USER_AUTHORITIES).append("?").append(Constants.USER_SID).append("=").append(user.getId()).toString());
    }

    @Autowired
    private UserService userService;
}
