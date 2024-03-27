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
package org.opennms.netmgt.provision.detector.simple.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>MultilineOrientedResponse class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class MultilineOrientedResponse {
    
    private static final Logger LOG = LoggerFactory.getLogger(MultilineOrientedResponse.class);
    private BufferedReader m_in;
    
    private List<String> m_responseList = new ArrayList<>();
    
    /**
     * <p>Constructor for MultilineOrientedResponse.</p>
     */
    public MultilineOrientedResponse() {
    }
    
    /**
     * <p>addLine</p>
     *
     * @param line a {@link java.lang.String} object.
     */
    public void addLine(final String line) {
        getResponseList().add(line);
    }


    /**
     * <p>receive</p>
     *
     * @param in a {@link java.io.BufferedReader} object.
     */
    public void receive(final BufferedReader in) {
        m_in = in;
    }
    
    /**
     * <p>startsWith</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean startsWith(final String prefix) {
        for(final String line : getResponseList()) {
           if(!line.startsWith(prefix)) {
               return false;
           }
        }
        return true;
    }
    
    /**
     * <p>expectedCodeRange</p>
     *
     * @param beginCodeRange a int.
     * @param endCodeRange a int.
     * @return a boolean.
     */
    public boolean expectedCodeRange(final int beginCodeRange, final int endCodeRange) {

        for(final String line : getResponseList()) {
            if(!validateCodeRange(getCode(line), beginCodeRange, endCodeRange)) {
                return false;
            }
         }
        
        return true;
            
    }
    
    private String getCode(final String firstResponseLine) {
        return firstResponseLine.substring(0, 3);
    }

    
    //Kept in here 
    /**
     * <p>containedInHTTP</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @param url a {@link java.lang.String} object.
     * @param isCheckCode a boolean.
     * @param maxRetCode a int.
     * @return a boolean.
     */
    public boolean containedInHTTP(final String pattern, final String url, final boolean isCheckCode, final int maxRetCode) {
        int checkMaxRetCode = maxRetCode;
        
        try {
            
            final String response = getEntireResponse(m_in);
            LOG.debug("Checking http response, pattern: {}  URL: {}  isCheckCode: {}  MaxRetCode: {}\n", pattern, url, isCheckCode, checkMaxRetCode);
            if (response != null && response.contains(pattern)) {
                LOG.debug("Return from server was: {}", response);
                if (isCheckCode) {
                                                
                    if (("/".equals(url)) || (isCheckCode == false)) {
                        checkMaxRetCode = 600;
                    }
                    
                    final StringTokenizer t = new StringTokenizer(response);
                    t.nextToken();
                    final String codeString = t.nextToken();

                    if (validateCodeRange(codeString, 99, checkMaxRetCode)) {
                        LOG.debug("RetCode Passed");
                        return true;
                    }
                } else {
                    LOG.debug("isAServer");
                    return true;
                }
            }
        } catch (final Exception e) {
            return false;
        }
        
        return false;
    }

    /**
     * @return
     * @throws IOException 
     */
    private String getEntireResponse(final BufferedReader in) throws IOException {
        final char[] cbuf = new char[1024];
        int chars = 0;
        final StringBuilder response = new StringBuilder();
        try {
            while ((chars = in.read(cbuf, 0, 1024)) != -1) {
                response.append(cbuf, 0, chars);
            }
                
        } catch (final java.net.SocketTimeoutException timeoutEx) {
            if (timeoutEx.bytesTransferred > 0) {
                response.append(cbuf, 0, timeoutEx.bytesTransferred);
            }
        }
        return response.toString();
    }

    /**
     * @param codeString
     * @return
     */
    private boolean validateCodeRange(final String codeString, final int beginCodeRange, final int endCodeRange) {
        try {
            final int code = Integer.parseInt(codeString);
            return (code >= beginCodeRange && code <= endCodeRange);
        } catch(final Exception e) {
            return false;
        }
        
    }

    @Override
    public String toString() {
        return getResponseList().isEmpty() ? "MultilineOrientedResponse" : String.format("Response: %s", getResponseList().toArray());
    }

    /**
     * <p>setResponseList</p>
     *
     * @param responseList a {@link java.util.List} object.
     */
    public void setResponseList(final List<String> responseList) {
        m_responseList = responseList;
    }

    /**
     * <p>getResponseList</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getResponseList() {
        return m_responseList;
    }
}
