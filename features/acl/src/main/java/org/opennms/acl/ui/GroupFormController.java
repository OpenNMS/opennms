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

import org.opennms.acl.model.GroupDTO;
import org.opennms.acl.service.GroupService;
import org.opennms.acl.ui.validator.GroupValidator;
import org.opennms.acl.util.Constants;
import org.opennms.core.utils.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

/**
 * Group Form Controller to insert or update a Group
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
@Controller
@RequestMapping("/group.edit.page")
public class GroupFormController implements InitializingBean {

    /**
     * <p>processSubmit</p>
     *
     * @param group a {@link org.opennms.acl.model.GroupDTO} object.
     * @param result a {@link org.springframework.validation.BindingResult} object.
     * @param status a {@link org.springframework.web.bind.support.SessionStatus} object.
     * @return a {@link java.lang.String} object.
     */
    @RequestMapping(method = RequestMethod.POST)
    protected String processSubmit(@ModelAttribute("group") GroupDTO group, BindingResult result, SessionStatus status) {
        String mav = groupForm;
        groupValidator.validate(group, result);
        if (!result.hasErrors()) {
            groupService.save(group);
            status.setComplete();
            mav = Constants.REDIRECT_GROUP_LIST;
        }
        return mav;
    }

    /**
     * <p>initBinder</p>
     *
     * @param binder a {@link org.springframework.web.bind.WebDataBinder} object.
     * @throws java.lang.Exception if any.
     */
    @InitBinder()
    public void initBinder(WebDataBinder binder) throws Exception {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    /**
     * <p>setupForm</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @param model a {@link org.springframework.ui.ModelMap} object.
     * @return a {@link java.lang.String} object.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String setupForm(@RequestParam(required = false, value = "gid") Integer id, ModelMap model) {
        model.addAttribute(Constants.GROUP, id == null ? new GroupDTO() : groupService.getGroup(id));
        return groupForm;
    }

    @Autowired
    private GroupService groupService;
    @Autowired
    @Qualifier("groupValidator")
    private GroupValidator groupValidator;
    private final String groupForm = "group/form";

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
}
