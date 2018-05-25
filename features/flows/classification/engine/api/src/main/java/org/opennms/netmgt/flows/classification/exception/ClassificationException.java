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

package org.opennms.netmgt.flows.classification.exception;

import java.util.Objects;

import org.opennms.netmgt.flows.classification.error.Error;
import org.opennms.netmgt.flows.classification.error.ErrorTemplate;

// Generic Exception related to classifications
public class ClassificationException extends RuntimeException {

    // The user-friendly error message
    private final Error error;

    public ClassificationException(String context, ErrorTemplate template, Object... arguments) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(template);
        this.error = new Error(context, template, arguments);
    }

    public Error getError() {
        return error;
    }

    @Override
    public String getMessage() {
        return error.getFormattedMessage();
    }
}
