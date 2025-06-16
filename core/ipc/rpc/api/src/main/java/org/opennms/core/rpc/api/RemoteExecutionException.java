/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
