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

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.MultilineHttpResponse;
import org.opennms.netmgt.provision.support.AsyncBasicDetectorMinaImpl;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.support.codec.MultilineHttpProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Abstract MultilineHttpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class MultilineHttpDetector extends AsyncBasicDetectorMinaImpl<LineOrientedRequest, MultilineHttpResponse> {
    
    private static final Logger LOG = LoggerFactory.getLogger(MultilineHttpDetector.class);
    private static final String DEFAULT_SERVICE_NAME = "HTTP";
    private static final int DEFAULT_PORT = 80;
    private static String DEFAULT_URL="/";
    private static int DEFAULT_MAX_RET_CODE = 399;
    
    private String m_url;
    private int m_maxRetCode;
    private boolean m_checkRetCode = false;
    
    /**
     * <p>Constructor for MultilineHttpDetector.</p>
     */
    public MultilineHttpDetector(){
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
        contructDefaults();
    }
    
    /**
     * <p>Constructor for MultilineHttpDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public MultilineHttpDetector(final String serviceName, final int port) {
        super(serviceName, port);
        contructDefaults();
    }
    
    private void contructDefaults() {
        setProtocolCodecFilter(new ProtocolCodecFilter(new MultilineHttpProtocolFactory()));
        setUrl(DEFAULT_URL);
        setMaxRetCode(DEFAULT_MAX_RET_CODE);
    }
    
    

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        send(request(httpCommand("GET")), contains(DEFAULT_SERVICE_NAME, getUrl(), isCheckRetCode(), getMaxRetCode()));
    }
    
    /**
     * <p>httpCommand</p>
     *
     * @param command a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected String httpCommand(final String command) {
        
        return String.format("%s %s  HTTP/1.0\r\n\r\n", command, getUrl());
    }
    
    /**
     * <p>request</p>
     *
     * @param command a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest} object.
     */
    protected static LineOrientedRequest request(final String command) {
        return new LineOrientedRequest(command);
    }
    
    /**
     * <p>contains</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @param url a {@link java.lang.String} object.
     * @param isCheckCode a boolean.
     * @param maxRetCode a int.
     * @return a {@link org.opennms.netmgt.provision.support.AsyncClientConversation.ResponseValidator} object.
     */
    protected static ResponseValidator<MultilineHttpResponse> contains(final String pattern, final String url, final boolean isCheckCode, final int maxRetCode){
        return new ResponseValidator<MultilineHttpResponse>(){

            @Override
            public boolean validate(final MultilineHttpResponse message) {
                
                try {
                    return message.validateResponse(pattern, url, isCheckCode, maxRetCode);
                } catch (final Exception e) {
                    LOG.debug("Unable to validate response", e);
                    return false;
                }
            }
            
        };
    }

    /**
     * <p>setUrl</p>
     *
     * @param url a {@link java.lang.String} object.
     */
    public void setUrl(final String url) {
        m_url = url;
    }

    /**
     * <p>getUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUrl() {
        return m_url;
    }

    /**
     * <p>setMaxRetCode</p>
     *
     * @param maxRetCode a int.
     */
    public void setMaxRetCode(final int maxRetCode) {
        m_maxRetCode = maxRetCode;
    }

    /**
     * <p>getMaxRetCode</p>
     *
     * @return a int.
     */
    public int getMaxRetCode() {
        return m_maxRetCode;
    }

    /**
     * <p>setCheckRetCode</p>
     *
     * @param checkRetCode a boolean.
     */
    public void setCheckRetCode(final boolean checkRetCode) {
        m_checkRetCode = checkRetCode;
    }

    /**
     * <p>isCheckRetCode</p>
     *
     * @return a boolean.
     */
    public boolean isCheckRetCode() {
        return m_checkRetCode;
    }

}
