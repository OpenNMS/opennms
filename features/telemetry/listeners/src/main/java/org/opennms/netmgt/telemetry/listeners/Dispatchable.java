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
package org.opennms.netmgt.telemetry.listeners;

import io.netty.buffer.ByteBuf;

/**
 * A listener may define multiple parsers, in order to dispatch it to only one queue,
 * the parser must decide if it can handle the incoming data.
 *
 * A parser implementing the {@link Dispatchable} interface is capable of making this decision.
 *
 * @author mvrueden
 */
public interface Dispatchable {

    /**
     * Returns true if the implementor can handle the incoming data, otherwise false.
     *
     * @param buffer Representing the incoming data
     * @return true if the implementor can handle the data, otherwise false.
     */
    boolean handles(final ByteBuf buffer);
}
