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
package org.opennms.protocols.nsclient;

/**
 * This object implements the internal exceptions used by the
 * <code>NsclientManager</code> system.
 *
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 * @version $Id: $
 */
public class NsclientException extends Exception {

    private static final long serialVersionUID = -5403442488340618492L;

    /**
     * Constructor.
     */
    public NsclientException() {
        super();
    }

    /**
     * Constructor, sets the message pertaining to the exception problem.
     *
     * @param message
     *            the message pertaining to the exception problem.
     */
    public NsclientException(String message) {
        super(message);
    }

    /**
     * Constructor, sets the message pertaining to the exception problem and
     * the root cause exception (if applicable.)
     *
     * @param message
     *            the message pertaining to the exception problem.
     * @param cause
     *            the exception that caused this exception to be generated.
     */
    public NsclientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor, sets the exception that caused this exception to be
     * generated.
     *
     * @param cause
     *            the exception that caused this exception to be generated.
     */
    public NsclientException(Throwable cause) {
        super(cause);
    }

}
