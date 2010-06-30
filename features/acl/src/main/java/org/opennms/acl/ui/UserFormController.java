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

import org.opennms.acl.model.UserDTO;
import org.opennms.acl.service.UserService;
import org.opennms.acl.ui.validator.UserValidator;
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
 * User Form Controller to insert or update a managed user
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
@Controller
@RequestMapping("/user.edit.page")
public class UserFormController {

    /**
     * <p>processSubmit</p>
     *
     * @param user a {@link org.opennms.acl.model.UserDTO} object.
     * @param result a {@link org.springframework.validation.BindingResult} object.
     * @param status a {@link org.springframework.web.bind.support.SessionStatus} object.
     * @return a {@link java.lang.String} object.
     */
    @RequestMapping(method = RequestMethod.POST)
    protected String processSubmit(@ModelAttribute("user") UserDTO user, BindingResult result, SessionStatus status) {
        String mav = userForm;
        validator.validate(user, result);
        if (!result.hasErrors()) {
            userService.save(user);
            status.setComplete();
            mav = Constants.REDIRECT_USER_LIST;
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
    public String setupForm(@RequestParam(required = false, value = "sid") Integer id, ModelMap model) {
        UserDTO user;
        if (id == null) {
            user = new UserDTO();
        } else {
            user = (UserDTO) userService.getUserCredentials(id.toString());
            user.setPassword("");
        }
        model.addAttribute(Constants.USER, user);
        return userForm;
    }

    @Autowired
    private UserService userService;
    @Autowired
    @Qualifier("userValidator")
    private UserValidator validator;
    private final String userForm = "user/form";
}
