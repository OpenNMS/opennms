/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.flows.classification.error;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;

// Generic error object to handle user friendly error messages
public class Error {

    // Additional (optional) context of the error (e.g. an attribute name)
    private final String context;

    // A unique error key
    private final String key;

    // The error message. May use Message Format syntax.
    private final String message;

    // Arguments to be used in the message format.
    private Object[] arguments;

    public Error(String errorContext, String errorKey, String errorMessage, Object... arguments) {
        this.key = errorKey;
        this.context = errorContext;
        this.message = Objects.requireNonNull(errorMessage);
        this.arguments = arguments;
    }

    public Error(Error error) {
        this(error.getContext(), error.getKey(), error.getMessage(), Arrays.copyOf(error.getArguments(), error.getArguments().length));
    }

    public String getContext() {
        return context;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public String getFormattedMessage() {
        return new MessageFormat(getMessage()).format(getArguments());
    }
}
