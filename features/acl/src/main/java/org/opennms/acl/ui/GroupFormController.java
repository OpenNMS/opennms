/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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

import org.opennms.acl.model.GroupDTO;
import org.opennms.acl.service.GroupService;
import org.opennms.acl.ui.validator.GroupValidator;
import org.opennms.acl.util.Constants;
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
 */
@Controller
@RequestMapping("/group.edit.page")
public class GroupFormController {

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

    @InitBinder()
    public void initBinder(WebDataBinder binder) throws Exception {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

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
}
