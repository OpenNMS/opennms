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
package org.opennms.web.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;

public class ExceptionUtils {

    /**
     * Recursively attempts to cast the given {@link Throwable} and it's cause
     * to an {@link Throwable} of the given type.
     *
     * @param t
     * @param type
     * @return null of no suitable cause was found.
     * @throws ServletException
     */
    public static <T extends Throwable> T getRootCause(Throwable t, Class<T> type) throws ServletException {
        if (t == null) {
            throw new ServletException("Null exceptions are not supported.");
        }

        // Can we cast the exception directly?
        if (t.getClass().isAssignableFrom(type)) {
            return type.cast(t);
        }

        // Recurse with the root cause
        if (t instanceof ServletException) {
            final ServletException se = (ServletException)t;
            final Throwable cause = se.getRootCause();
            if (cause != null) {
                return getRootCause(cause, type);
            }
        }

        throw new ServletException("Unsupported exception of type " + t.getClass().getCanonicalName(), t);
    }

    public static String getFullStackTrace(Throwable throwable){
        if(throwable == null){
            return "Throwable=null";
        }
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
