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
package org.opennms.netmgt.provision.server.exchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>Exchange interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface Exchange {
    /**
     * <p>sendRequest</p>
     *
     * @param out a {@link java.io.OutputStream} object.
     * @return a boolean.
     * @throws java.io.IOException if any.
     */
    boolean sendRequest(OutputStream out) throws IOException;
    /**
     * <p>processResponse</p>
     *
     * @param in a {@link java.io.BufferedReader} object.
     * @return a boolean.
     * @throws java.io.IOException if any.
     */
    boolean processResponse(BufferedReader in) throws IOException;
    /**
     * <p>matchResponseByString</p>
     *
     * @param input a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean matchResponseByString(String input);
}
