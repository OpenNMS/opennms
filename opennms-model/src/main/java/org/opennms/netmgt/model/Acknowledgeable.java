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
package org.opennms.netmgt.model;

import java.util.Date;

/**
 * Entities that have the capability of being acknowledge should implement this interface for
 * Ackd acknowledgment behavior.
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public interface Acknowledgeable {
    
    /**
     * <p>acknowledge</p>
     *
     * @param ackUser a {@link java.lang.String} object.
     */
    void acknowledge(String ackUser);
    /**
     * <p>unacknowledge</p>
     *
     * @param ackUser a {@link java.lang.String} object.
     */
    void unacknowledge(String ackUser);
    /**
     * <p>clear</p>
     *
     * @param ackUser a {@link java.lang.String} object.
     */
    void clear(String ackUser);
    /**
     * <p>escalate</p>
     *
     * @param ackUser a {@link java.lang.String} object.
     */
    void escalate(String ackUser);
    
    /**
     * <p>getType</p>
     *
     * @return a {@link org.opennms.netmgt.model.AckType} object.
     */
    AckType getType();

    /**
     * <p>getAckId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    Integer getAckId();

    /**
     * <p>getAckUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getAckUser();

    /**
     * <p>getAckTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    Date getAckTime();
    
    /**
     * Might be null but probably supported already by most implementations, but still, here for convenience.  Also
     * guarantees that this is available in this API if the model changes where the node is not directly related and de-facto
     * support is removed.
     *
     * @return the related OnmsNode, null if non available or doesn't make sense
     */
    OnmsNode getNode();

    OnmsSeverity getSeverity();

}
