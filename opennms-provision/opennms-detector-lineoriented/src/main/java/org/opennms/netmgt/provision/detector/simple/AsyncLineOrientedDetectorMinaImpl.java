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

import java.nio.charset.StandardCharsets;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.AsyncBasicDetectorMinaImpl;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.support.codec.LineOrientedCodecFactory;

/**
 * <p>Abstract AsyncLineOrientedDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class AsyncLineOrientedDetectorMinaImpl extends AsyncBasicDetectorMinaImpl<LineOrientedRequest, LineOrientedResponse> {

    /**
     * <p>Constructor for AsyncLineOrientedDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public AsyncLineOrientedDetectorMinaImpl(final String serviceName, final int port) {
        super(serviceName, port);
        setProtocolCodecFilter(new ProtocolCodecFilter(new LineOrientedCodecFactory(StandardCharsets.UTF_8)));
    }

    /**
     * <p>Constructor for AsyncLineOrientedDetector.</p>
     *
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     * @param serviceName a {@link java.lang.String} object.
     */
    public AsyncLineOrientedDetectorMinaImpl(final String serviceName, final int port, final int timeout, final int retries) {
        super(serviceName, port, timeout, retries);
        setProtocolCodecFilter(new ProtocolCodecFilter(new LineOrientedCodecFactory(StandardCharsets.UTF_8)));
    }

    /** {@inheritDoc} */
    @Override
    protected ResponseValidator<LineOrientedResponse> startsWith(final String prefix) {
        return new ResponseValidator<LineOrientedResponse>() {

            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.startsWith(prefix);
            }
            
        };
    }
    
    /** {@inheritDoc} */
    @Override
    public ResponseValidator<LineOrientedResponse> find(final String regex){
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

}
