/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.api;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Thrown when an error occurred processing the request on the remote
 * system.
 *
 * @author jesse
 */
public class RemoteExecutionException extends Exception {

    private static final long serialVersionUID = 2002562170814461170L;

    public RemoteExecutionException(String message) {
        super(message);
    }

    /**
     * Utility function for converting a {@link Throwable} to a {@link String}.
     *
     * @param t the exception
     * @return a string that contains the exception message and the stack trace
     */
    public static String toErrorMessage(Throwable t) {
        if (t == null) {
            return null;
        }

        final StringWriter strackTrace = new StringWriter();
        final PrintWriter pw = new PrintWriter(strackTrace);
        t.printStackTrace(pw);
        return strackTrace.toString();
    }

}
