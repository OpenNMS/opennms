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
package org.opennms.netmgt.alarmd.api;

/**
 * North bound Interface API Exception
 * <p>Intention is to wrap all throwables as a Runtime Exception.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class NorthbounderException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new northbounder exception.
     *
     * @param t the throwable
     */
    public NorthbounderException(Throwable t) {
        super(t);
    }

    /**
     * Instantiates a new northbounder exception.
     *
     * @param message the message
     */
    public NorthbounderException(String message) {
        super(message);
    }

    /**
     * Instantiates a new northbounder exception.
     *
     * @param message the message
     * @param t the t
     */
    public NorthbounderException(String message, Throwable t) {
        super(message, t);
    }

}
