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
package org.opennms.web.notification;

/**
 * This exception is used to indicate that the NotificationFactory had a problem
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 * @since 1.8.1
 */
public class NotificationFactoryException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 2374905955140803820L;
    private Throwable rootCause;

    /**
     * <p>Constructor for NotificationFactoryException.</p>
     */
    public NotificationFactoryException() {
        super();
    }

    /**
     * <p>Constructor for NotificationFactoryException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public NotificationFactoryException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for NotificationFactoryException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param rootCause a {@link java.lang.Throwable} object.
     */
    public NotificationFactoryException(String message, Throwable rootCause) {
        super(message);
        this.rootCause = rootCause;
    }

    /**
     * <p>Constructor for NotificationFactoryException.</p>
     *
     * @param rootCause a {@link java.lang.Throwable} object.
     */
    public NotificationFactoryException(Throwable rootCause) {
        super(rootCause.getLocalizedMessage());
        this.rootCause = rootCause;
    }

    /**
     * <p>Getter for the field <code>rootCause</code>.</p>
     *
     * @return a {@link java.lang.Throwable} object.
     */
    public Throwable getRootCause() {
        return rootCause;
    }
}
