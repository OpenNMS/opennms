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
package org.opennms.acl.ui.validator;

import org.opennms.acl.model.UserDTO;
import org.opennms.acl.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * <p>UserValidator class.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
@Component("userValidator")
public class UserValidator implements Validator {

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        return UserDTO.class.isAssignableFrom(clazz);
    }

    /** {@inheritDoc} */
    public void validate(Object command, Errors err) {
        UserDTO user = (UserDTO) command;
        ValidationUtils.rejectIfEmptyOrWhitespace(err, "username", "username.required.value", "username is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(err, "password", "password.required.value", "password is required.");
        if (user.isNew() && null != userService.getIdUser(user.getUsername())) {
            err.rejectValue("username", "error.username.already.present");
        }

        if (!user.getPassword().equals("") && user.getPassword().length() < 6) {
            err.rejectValue("password", "error.password.length", "password too short");
        }
    }

    /**
     * <p>Setter for the field <code>userService</code>.</p>
     *
     * @param userService a {@link org.opennms.acl.service.UserService} object.
     */
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private UserService userService;
}
