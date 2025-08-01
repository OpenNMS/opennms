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
package org.opennms.netmgt.provision.support;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;

/**
 * <p>Client interface.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public interface Client<Request, Response> extends Closeable {
    
    /**
     * <p>connect</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param port a int.
     * @param timeout a int.
     * @param <Request> a Request object.
     * @param <Response> a Response object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception;
    
    /**
     * <p>receiveBanner</p>
     *
     * @return a Response object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    Response receiveBanner() throws IOException, Exception;
    
    /**
     * <p>sendRequest</p>
     *
     * @param request a Request object.
     * @return a Response object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    Response sendRequest(Request request) throws IOException, Exception; 
    
    /**
     * <p>close</p>
     */
    public void close();

}
