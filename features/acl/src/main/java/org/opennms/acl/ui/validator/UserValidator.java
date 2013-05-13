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
    @Override
    public boolean supports(Class clazz) {
        return UserDTO.class.isAssignableFrom(clazz);
    }

    /** {@inheritDoc} */
    @Override
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
