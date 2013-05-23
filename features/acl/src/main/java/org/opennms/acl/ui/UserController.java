/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
 *
 * From the original copyright headers:
 *
 * Copyright (c) 2009+ desmax74
 * Copyright (c) 2009+ The OpenNMS Group, Inc.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * The author can be contacted at the following email address:
 *
 *     Massimiliano Dess&igrave;
 *     desmax74@yahoo.it
 *******************************************************************************/

package org.opennms.acl.ui;

import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.opennms.acl.domain.GenericUser;
import org.opennms.acl.exception.UserNotfoundException;
import org.opennms.acl.model.Pager;
import org.opennms.acl.service.UserService;
import org.opennms.acl.ui.util.WebUtils;
import org.opennms.acl.util.Constants;
import org.opennms.core.utils.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
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
 * @version $Id: $
 */
@Controller
public class UserController implements InitializingBean {

    /**
     * <p>authorities</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.springframework.web.servlet.ModelAndView} object.
     * @throws java.lang.Exception if any.
     */
    @RequestMapping("/user.authorities.page")
    public ModelAndView authorities(HttpServletRequest req) throws Exception {
        GenericUser user = WebUtils.getUser(req);
        return new ModelAndView("user/authorities", Constants.UI_USER, user.getUserView());
    }

    /**
     * <p>list</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.springframework.web.servlet.ModelAndView} object.
     * @throws java.lang.Exception if any.
     */
    @RequestMapping("/user.list.page")
    public ModelAndView list(HttpServletRequest req) throws Exception {
        Pager pager = WebUtils.getPager(req, userService.getTotalItemsNumber(), 15);
        return new ModelAndView("user/list", Constants.UI_USERS, userService.getEnabledUsers(pager));
    }

    /**
     * <p>detail</p>
     *
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.springframework.web.servlet.ModelAndView} object.
     * @throws java.lang.Exception if any.
     */
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

    /**
     * <p>selection</p>
     *
     * @param ids a {@link java.lang.String} object.
     * @param req a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.springframework.web.servlet.ModelAndView} object.
     * @throws java.lang.Exception if any.
     */
    @RequestMapping("/user.selection.page")
    public ModelAndView selection(@RequestParam("includedHidden") String ids, HttpServletRequest req) throws Exception {
        GenericUser user = WebUtils.getUser(req);
        if (user != null) {
            if (ids != null && ids.length() > 0) {
                user.setNewGroups(WebUtils.extractIdGrantedAuthorityFromString(ids, Constants.COMMA));
            } else {
                user.setNewGroups(Collections.<Integer>emptyList());
            }
            user.save();
        }
        return new ModelAndView(new StringBuilder(Constants.REDIRECT_USER_AUTHORITIES).append("?").append(Constants.USER_SID).append("=").append(user.getId()).toString());
    }

    @Autowired
    private UserService userService;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
}
