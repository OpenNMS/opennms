/*
 * This file is part of the OpenNMS(R) Application. OpenNMS(R) is Copyright
 * (C) 2008 The OpenNMS Group, Inc. All rights reserved. OpenNMS(R) is a
 * derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified and included code are below. OpenNMS(R) is a registered
 * trademark of The OpenNMS Group, Inc. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place - Suite 330, Boston, MA 02111-1307, USA. For more information
 * contact: OpenNMS Licensing <license@opennms.org> http://www.opennms.org/
 * http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.simple.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.opennms.core.utils.LogUtils;

/**
 * <p>MultilineOrientedResponse class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class MultilineOrientedResponse {
    private BufferedReader m_in;
    
    private List<String> m_responseList = new ArrayList<String>();
    
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
            LogUtils.debugf(this, "Checking http response, pattern: %s  URL: %s  isCheckCode: %s  MaxRetCode: %s\n", pattern, url, isCheckCode, checkMaxRetCode);
            if (response != null && response.contains(pattern)) {
                LogUtils.debugf(this, "Return from server was: %s", response);
                if (isCheckCode) {
                                                
                    if (("/".equals(url)) || (isCheckCode == false)) {
                        checkMaxRetCode = 600;
                    }
                    
                    final StringTokenizer t = new StringTokenizer(response);
                    t.nextToken();
                    final String codeString = t.nextToken();

                    if (validateCodeRange(codeString, 99, checkMaxRetCode)) {
                        LogUtils.debugf(this, "RetCode Passed");
                        return true;
                    }
                } else {
                    LogUtils.debugf(this, "isAServer");
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
        final StringBuffer response = new StringBuffer();
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
