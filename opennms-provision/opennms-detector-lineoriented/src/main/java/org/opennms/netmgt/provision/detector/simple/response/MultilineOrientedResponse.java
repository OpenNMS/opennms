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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.opennms.core.utils.LogUtils;

/**
 * @author Donald Desloge
 */
public class MultilineOrientedResponse {
    private BufferedReader m_in;
    
    private List<String> m_responseList;
    
    public MultilineOrientedResponse() {
        setResponseList(new ArrayList<String>());
    }
    
    public void addLine(String line) {
        getResponseList().add(line);
    }


    public void receive(BufferedReader in) {
        m_in = in;
    }
    
    public boolean startsWith(String prefix) {
        for(String line : getResponseList()) {
           if(!line.startsWith(prefix)) {
               return false;
           }
        }
        return true;
    }
    
    /**
     * @param beginRange
     * @param endRange
     * @return
     */
    public boolean expectedCodeRange(int beginCodeRange, int endCodeRange) {

        for(String line : getResponseList()) {
            if(!validateCodeRange(getCode(line), beginCodeRange, endCodeRange)) {
                return false;
            }
         }
        
        return true;
            
    }
    
    private String getCode(String firstResponseLine) {
        String codeString = firstResponseLine.substring(0, 3);
        return codeString;
    }

    
    //Kept in here 
    public boolean containedInHTTP(String pattern, String url, boolean isCheckCode, int maxRetCode) {

        
        try {
            
            String response = getEntireResponse(m_in);
            LogUtils.infof(this, "Checking http response, pattern: %s  URL: %s  isCheckCode: %s  MaxRetCode: %s\n", pattern, url, isCheckCode, maxRetCode);
            if (response != null && response.contains(pattern)) {
                LogUtils.infof(this, "Return from server was: " + response);
                if (isCheckCode) {
                                                
                    if (("/".equals(url)) || (isCheckCode == false)) {
                        maxRetCode = 600;
                    }
                    
                    StringTokenizer t = new StringTokenizer(response);
                    t.nextToken();
                    String codeString = t.nextToken();
                                               
                    if (validateCodeRange(codeString, 99, maxRetCode)) {
                        LogUtils.infof(this, "RetCode Passed");
                        return true;
                    }
                } else {
                    LogUtils.infof(this, "isAServer");
                    return true;
                }
            }
        } catch (SocketException e) {
            //e.printStackTrace();
            return false;
        } catch (NumberFormatException e) {

            return false;
        }catch(IOException e) {
            //e.printStackTrace();
            return false;
        }
        
        return false;
    }

    /**
     * @return
     * @throws IOException 
     */
    private String getEntireResponse(BufferedReader in) throws IOException {
        char[] cbuf = new char[1024];
        int chars = 0;
        StringBuffer response = new StringBuffer();
        try {
            while ((chars = in.read(cbuf, 0, 1024)) != -1) {
                String line = new String(cbuf, 0, chars);
                response.append(line);
            }
                
        } catch (java.net.SocketTimeoutException timeoutEx) {
            if (timeoutEx.bytesTransferred > 0) {
                String line = new String(cbuf, 0, timeoutEx.bytesTransferred);
                response.append(line);
            }
        }
        return response.toString();
    }

    /**
     * @param codeString
     * @return
     */
    private boolean validateCodeRange(String codeString, int beginCodeRange, int endCodeRange) {
        try {
            int code = Integer.parseInt(codeString);
            return (code >= beginCodeRange && code <= endCodeRange);
        }catch(Exception e) {
            return false;
        }
        
    }

    /**
     * @param pattern
     * @return
     * @throws IOException 
     */
//    public boolean readStreamUntilContains(String pattern) throws IOException {
//        
//        BufferedReader reader = m_in;
//        StringBuffer buffer = new StringBuffer();
//        
//        boolean patternFound = false;
//        while (!patternFound) {
//            buffer.append((char) reader.read());
//            System.out.println("Return from server: " + buffer.toString());
//            if (buffer.toString().contains(pattern)) {
//                patternFound = true;
//                return patternFound;
//            }
//        }
//        return false;
//    }
    
    public String toString() {
        return getResponseList().isEmpty() ? "MultilineOrientedResponse" : String.format("Response: %s", getResponseList().toArray());
    }

    public void setResponseList(List<String> responseList) {
        m_responseList = responseList;
    }

    public List<String> getResponseList() {
        return m_responseList;
    }
}
