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

import org.opennms.netmgt.provision.detector.simple.client.TrivialTimeClient;
import org.opennms.netmgt.provision.detector.simple.request.TrivialTimeRequest;
import org.opennms.netmgt.provision.detector.simple.response.TrivialTimeResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;

/**
 * <p>TrivialTimeDetector class.</p>
 *
 * @author Alejandro Galue <agalue@sync.com.ve>
 * @version $Id: $
 */

public class TrivialTimeDetector extends BasicDetector<TrivialTimeRequest, TrivialTimeResponse> {

    private static final String DEFAULT_SERVICE_NAME = "TrivialTime";

    /**
     * Default layer-4 protocol to use
     */
    private static final String DEFAULT_PROTOCOL = "tcp"; // Use TCP by default

    /**
     * Default port.
     */
    private static final int DEFAULT_PORT = 37;

    /**
     * Default permissible skew between the remote and local clocks
     */
    private static final int DEFAULT_ALLOWED_SKEW = 30; // 30 second skew

    private String protocol = DEFAULT_PROTOCOL;
    private int allowedSkew = DEFAULT_ALLOWED_SKEW;

    /**
     * Default constructor
     */
    public TrivialTimeDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public TrivialTimeDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        send(request(), validate());
    }

    private TrivialTimeRequest request() {
        return new TrivialTimeRequest();
    }

    private static ResponseValidator<TrivialTimeResponse> validate() {
        return new ResponseValidator<TrivialTimeResponse>() {
            @Override
            public boolean validate(final TrivialTimeResponse response) {
                return response.isAvailable();
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    protected Client<TrivialTimeRequest, TrivialTimeResponse> getClient() {
        final TrivialTimeClient client = new TrivialTimeClient(getProtocol(), getAllowedSkew());
        client.setRetries(getRetries());
        return client;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getAllowedSkew() {
        return allowedSkew;
    }

    public void setAllowedSkew(int allowedSkew) {
        this.allowedSkew = allowedSkew;
    }

}
