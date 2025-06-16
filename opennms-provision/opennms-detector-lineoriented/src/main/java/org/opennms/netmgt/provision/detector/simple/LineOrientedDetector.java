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
package org.opennms.netmgt.provision.detector.simple;

import org.opennms.netmgt.provision.detector.simple.client.LineOrientedClient;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;

/**
 * <p>Abstract LineOrientedDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class LineOrientedDetector extends BasicDetector<LineOrientedRequest, LineOrientedResponse> {

    /**
     * <p>Constructor for LineOrientedDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public LineOrientedDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
    /**
     * <p>Constructor for LineOrientedDetector.</p>
     *
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     * @param serviceName a {@link java.lang.String} object.
     */
    public LineOrientedDetector(final String serviceName, final int port, final int timeout, final int retries) {
        super(serviceName, port, timeout, retries);

    }

    /**
     * <p>startsWith</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public static ResponseValidator<LineOrientedResponse> startsWith(final String pattern) {
        return new ResponseValidator<LineOrientedResponse>() {
            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.startsWith(pattern);
            }
            
        };
    }
    
    /**
     * <p>equals</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public static ResponseValidator<LineOrientedResponse> equals(final String pattern) {
        return new ResponseValidator<LineOrientedResponse>() {
            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.equals(pattern);
            }
            
        };
    }
    
    /**
     * <p>matches</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public static ResponseValidator<LineOrientedResponse> matches(final String regex){
        return new ResponseValidator<LineOrientedResponse>() {

            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.matches(regex);
            }
            
        };
    }
    
    /**
     * <p>find</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.ResponseValidator} object.
     */
    public static ResponseValidator<LineOrientedResponse> find(final String regex){
        return new ResponseValidator<LineOrientedResponse>() {

            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.find(regex);
            }
          
            
        };
    }
    
    /**
     * <p>request</p>
     *
     * @param command a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest} object.
     */
    public LineOrientedRequest request(final String command) {
        return new LineOrientedRequest(command);
    }
    
    /**
     * <p>expectClose</p>
     */
    public void expectClose() {
        send(LineOrientedRequest.Null, LineOrientedDetector.equals(null));
    }
    
    /** {@inheritDoc} */
    @Override
    protected Client<LineOrientedRequest, LineOrientedResponse> getClient() {
        return new LineOrientedClient();
    }

}
