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
import org.opennms.netmgt.provision.detector.simple.support.TcpDetectorHandler;
import org.opennms.netmgt.provision.support.ConversationExchange;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.support.codec.TcpCodecFactory;
import org.opennms.netmgt.provision.support.codec.TcpLineDecoder;

/**
 * <p>TcpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class TcpDetector extends AsyncLineOrientedDetectorMinaImpl {
    
    private static final String DEFAULT_SERVICE_NAME = "TCP";
    private static final int DEFAULT_PORT = 23;
    
    private String m_banner = null;
    
    /**
     * Default constructor
     */
    public TcpDetector() {
        this(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }
    
    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public TcpDetector(final String serviceName, final int port) {
        super(serviceName, port);
        setDetectorHandler(new TcpDetectorHandler());
        setProtocolCodecFilter(new ProtocolCodecFilter(new TcpCodecFactory(StandardCharsets.UTF_8)));
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        if(getBanner() != null) {
            expectBanner(matches(getBanner()));
        }else {
            getConversation().addExchange(testBannerlessConnection());
        }
    }
    
    private static ConversationExchange<LineOrientedRequest, LineOrientedResponse> testBannerlessConnection() {

        return new ConversationExchange<LineOrientedRequest, LineOrientedResponse>() {

            @Override
            public boolean validate(final LineOrientedResponse response) {
                return response.equals(TcpLineDecoder.NO_MESSAGES_RECEIVED);
            }

            @Override
            public LineOrientedRequest getRequest() {
                return null;
            }
        };
    }

    /**
     * <p>matches</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    public static ResponseValidator<LineOrientedResponse> matches(final String regex){
        return new ResponseValidator<LineOrientedResponse>() {

            @Override
            public boolean validate(final LineOrientedResponse response) {
                // Make sure that the response matches the regex and that it is not an instance of the
                // special token that represents that no banner was received.
                return response.matches(regex) && !response.equals(TcpLineDecoder.NO_MESSAGES_RECEIVED);
            }

        };
    }

    /**
     * <p>setBanner</p>
     *
     * @param banner a {@link java.lang.String} object.
     */
    public void setBanner(final String banner) {
        m_banner = banner;
    }

    /**
     * <p>getBanner</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBanner() {
        return m_banner;
    }
    
}
