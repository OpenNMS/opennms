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
package org.opennms.netmgt.provision.detector.smb;

import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.smb.client.SmbClient;
import org.opennms.netmgt.provision.detector.smb.response.NbtAddressResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
/**
 * <p>SmbDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */

public class SmbDetector extends BasicDetector<LineOrientedRequest, NbtAddressResponse> {

    private static final String DEFAULT_SERVICE_NAME = "SMB";
    private static final int DEFAULT_RETRIES = 0;
    private static final int DEFAULT_TIMEOUT = 1000;
    private static final int DEFAULT_PORT = 0;

    /**
     * Default constructor
     */
    public SmbDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    /**
     * Constructor for instantiating a non-default service name of this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     */
    public SmbDetector(String serviceName) {
        super(serviceName, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    
    /**
     * Constructor for overriding defaults
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param timeout a int.
     * @param retries a int.
     */
    public SmbDetector(String serviceName, int timeout, int retries) {
        super(serviceName, DEFAULT_PORT, timeout, retries);
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(validateAddressIsNotSame());
        
    }
    
    private static ResponseValidator<NbtAddressResponse> validateAddressIsNotSame(){
        return new ResponseValidator<NbtAddressResponse>() {

            @Override
            public boolean validate(NbtAddressResponse response) {
                return response.validateAddressIsNotSame();
            }
            
        };
    }
    
    /** {@inheritDoc} */
    @Override
    protected Client<LineOrientedRequest, NbtAddressResponse> getClient() {
        return new SmbClient();
    }


}
