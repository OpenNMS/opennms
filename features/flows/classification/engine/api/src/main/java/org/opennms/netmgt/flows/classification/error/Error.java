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

    private final ErrorTemplate template;

    // Arguments to be used in the message format.
    private final Object[] arguments;

    public Error(String context, ErrorTemplate errorTemplate, Object... arguments) {
        this.context = context;
        this.template = Objects.requireNonNull(errorTemplate);
        this.arguments = arguments;
    }

    private Object[] getArguments() {
        return arguments;
    }

    public String getFormattedMessage() {
        return new MessageFormat(template.getMessage()).format(getArguments());
    }

    public ErrorTemplate getTemplate() {
        return template;
    }

    public String getContext() {
        return context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Error error = (Error) o;
        return Objects.equals(template, error.template)
                && Arrays.equals(arguments, error.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(template, arguments);
    }
}
