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
package org.opennms.netmgt.provision.detector;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Donald Desloge
 */
public class MultilineOrientedResponse {
    private BufferedReader m_in;
    private String m_lineEscape = " ";
    private String m_multilineIndicator = "-";

    public void receive(BufferedReader in) {
        m_in = in;
    }
    
    public boolean startsWith(String pattern) {
        String line = null;
        do {
           try {
               line = getLine();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
           
           if(!line.startsWith(pattern)) { return false; };
           
       }while(line !=null && line.length() > 0 && "-".equals(line.substring(3,4)));            
        
        return true;
    }
    
    /**
     * @param beginRange
     * @param endRange
     * @return
     */
    public boolean expectedCodeRange(int beginRange, int endRange) {
        try {  
            
            List<String> response = new ArrayList<String>();
    
            String firstResponseLine = getLine();
            System.out.println("MultilineOrientedResponse from server: " + firstResponseLine);
            
            String codeString = getCode(firstResponseLine);
            
            if(!validateCode(codeString)) { return false; }
            System.out.printf("MultilineOrientedResponse Code: %s\n", codeString);
            response.add(firstResponseLine.substring(4));
    
    
            // Is the fourth character a hyphen (if so, it's a continuation)?
            if (m_multilineIndicator.equals(firstResponseLine.substring(3, 4))) {
                // The multi-line response ends with a line that begins with this:
                String endMultiLine = String.format("%s%s", codeString, m_lineEscape);
    
                while (true) {
                    String subsequentResponse = null;
                    
                    subsequentResponse = getLine();
                    
                    System.out.printf("MultilineOrientedResponse subsequentResponse: %s", subsequentResponse);
    
                    if (subsequentResponse.startsWith(endMultiLine)) {
                        response.add(subsequentResponse.substring(4));
                        break;
                    }
    
                    response.add(subsequentResponse);
                }
            }
            System.out.println("MultilineOrientedResponse code string: " + codeString);
            return validateCodeRange(codeString, beginRange, endRange);
            
      }catch(IOException e) {
          //e.printStackTrace();
          return false;
      }
            
    }
    
    //HTTP multiline response
    public boolean containedInHTTP(String pattern, String url, boolean isCheckCode, int maxRetCode) {
        try {
            
            String response = getEntireResponse(m_in);
            System.out.printf("Checking http response, pattern: %s  URL: %s  isCheckCode: %s  MaxRetCode: %s\n", pattern, url, isCheckCode, maxRetCode);
            if (response != null && response.contains(pattern)) {
                System.out.println("Return from server was: " + response);
                if (isCheckCode) {
                                                
                    if (("/".equals(url)) || (isCheckCode == false)) {
                        maxRetCode = 600;
                    }
                    
                    StringTokenizer t = new StringTokenizer(response.toString());
                    t.nextToken();
                    String codeString = t.nextToken();
                                               
                    if (validateCodeRange(codeString, 99, maxRetCode)) {
                        System.out.println("RetCode Passed");
                        return true;
                    }
                } else {
                    System.out.println("isAServer");
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
        String line = response.toString();
        return response.toString();
    }

    /**
     * @param firstResponseLine
     * @return
     */
    private String getCode(String firstResponseLine) {
        String codeString = firstResponseLine.substring(0, 3);
        return codeString;
    }

    /**
     * @param codeString
     * @return
     */
    private boolean validateCode(String codeString)  {
        try {    
            Integer.parseInt(codeString);
        }catch(NumberFormatException e) {
            return false;
        }
        
        return true;
    }

    /**
     * @return
     * @throws IOException
     */
    private String getLine() throws IOException {
        String line = m_in.readLine();
        if(line != null) { 
            return line; 
       }else {
            throw new IOException("End of stream was reached before a response could be read");
        }
    }

    /**
     * @param codeString
     * @return
     */
    private boolean validateCodeRange(String codeString, int beginCodeRange, int endCodeRange) {
        int code = Integer.parseInt(codeString);
        return (code >= beginCodeRange && code <= endCodeRange);
    }

    public void until(String lineEcapse) {
        m_lineEscape = lineEcapse;
    }
    
    public void whileIndicator(String indicator) {
        m_multilineIndicator = indicator;
    }

    public boolean equals(String response) {
        return true; // (response == null ? m_response == null :
                     // response.equals(m_response));
    }

    /**
     * @param pattern
     * @return
     * @throws IOException 
     */
    public boolean readStreamUntilContains(String pattern) throws IOException {
        
        BufferedReader reader = m_in;
        StringBuffer buffer = new StringBuffer();
        
        boolean patternFound = false;
        while (!patternFound) {
            buffer.append((char) reader.read());
            System.out.println("Return from server: " + buffer.toString());
            if (buffer.toString().contains(pattern)) {
                patternFound = true;
                return patternFound;
            }
        }
        return false;
    }
}
