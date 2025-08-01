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
package org.opennms.netmgt.collection.persistence.tcp;

import java.util.List;

/**
 * Defines an abstract strategy for manipulating TCP output socket.
 */
public interface TcpOutputStrategy {

    /**
     * Updates the TCP output stream with the supplied values
     *
     * @param path
     *            the hierarchy path for the data
     * @param owner
     *            the owner of the data
     * @paeam time
     *            the timestamp for the data
     * @param dblValues
     *            a list of Double values
     * @param strValues
     *            a list of String values
     * @throws java.lang.Exception
     *             if an error occurs updating the file
     */
    public void updateData(String path, String owner, Long timestamp, List<Double> dblValues, List<String> strValues) throws Exception;
}
