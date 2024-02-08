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
package org.opennms.netmgt.provision.persist;

/**
 * <p>RuntimePersistenceException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class RuntimePersistenceException extends RuntimeException {

    private static final long serialVersionUID = 8462261466853731107L;

    /**
     * <p>Constructor for RuntimePersistenceException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public RuntimePersistenceException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for RuntimePersistenceException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public RuntimePersistenceException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>Constructor for RuntimePersistenceException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public RuntimePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

}
