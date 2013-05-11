/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.capsd.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * Represents an FTP command response.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class FtpResponse {
    private int m_code;
    private String m_response[];

    /**
     * Creates an empty FTP response.
     */
    public FtpResponse() {}

    /**
     * Creates an FTP response with given status code and response string.
     *
     * @param code numeric status code
     * @param response response detail message (one line per array element)
     */
    public FtpResponse(int code, String[] response) {
        m_code = code;
        m_response = response;
    }

    /**
     * Gets the numeric response code.
     *
     * @return numeric status code
     */
    public int getCode() {
        return m_code;
    }

    /**
     * Sets the numeric response code.
     *
     * @param code numeric status code
     */
    public void setCode(int code) {
        m_code = code;
    }

    /**
     * Gets the response string array.
     *
     * @return response detail message (one line per array element)
     */
    public String[] getResponse() {
        return m_response;
    }

    /**
     * Sets the response string array.
     *
     * @param response response detail message (one line per array element)
     */
    public void setResponse(String[] response) {
        m_response = response;
    }
    
    /**
     * Search for a text string in each line of the response result.
     * Note that each line is tested individually.
     *
     * @param contain text to search for (using String.contains(contain))
     * @return true if the search string is found, false otherwise
     */
    public boolean responseContains(String contain) {
        for (String line : m_response) {
            if (line.contains(contain)) {
                return true;
            }
        }
        
        return false;
    }


    /**
     * Converts FTP response to string.
     *
     * @return FTP response as would be sent over FTP
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        int i;
        sb.append(m_code);

        if (m_response.length > 1) {
            sb.append("-");
        }
        if (m_response.length > 0) {
            sb.append(" " + m_response[0]);
        }

        for (i = 1; i < m_response.length; i++) {
            sb.append("\n");
            
            if (i == (m_response.length - 1)) {
                sb.append(m_code);
                sb.append(" ");
                sb.append(m_response[i]);
            } else if (m_response[i].startsWith(m_code + " ")) {
                sb.append(" ");
                sb.append(m_response[i]);
            } else {
                sb.append(m_response[i]);
            }
        }

        return sb.toString();
    }
    
    /**
     * Does this response have a valid code?
     *
     * @return True if the response code is between 100 and 599,
     *         false otherwise.
     */
    public boolean isCodeValid() {
        return getCode() >= 100 && getCode() < 600;
    }

    /**
     * Is this response a successful message?
     *
     * @return True if the response code is between 200 and 299,
     *         false otherwise.
     */
    public boolean isSuccess() {
        return (m_code >= 200 && m_code < 300);
    }

    /**
     * Is this response an intermediate message?
     *
     * @return True if the response code is between 300 and 399,
     *         false otherwise.
     */
    public boolean isIntermediate() {
        return (m_code >= 300 && m_code < 400);
    }

    /**
     * Helper method to send commands to the remote server.
     *
     * @param socket connection to the server
     * @param command command to send, without trailing EOL (CRLF, \r\n).
     * @throws java.io.IOException if we can't write() to the OutputStream for the Socket
     */
    public static void sendCommand(Socket socket, String command) throws IOException {
        socket.getOutputStream().write((command + "\r\n").getBytes());
    }

    /**
     * Reads a server response.
     *
     * @param in input reader
     * @return response from server
     * @throws java.io.IOException if any.
     */
    public static FtpResponse readResponse(BufferedReader in) throws IOException {
        int code;
        List<String> response = new ArrayList<String>();

        String firstResponseLine = in.readLine();
        if (firstResponseLine == null) {
            throw new IOException("End of stream was reached before a response could be read");
            
        }
        
        // XXX this could use better error checking!
        String codeString = firstResponseLine.substring(0, 3);
        response.add(firstResponseLine.substring(4));

        try {
            code = Integer.parseInt(codeString);
        } catch (NumberFormatException e) {
            IOException newE = new IOException("First response line returned a non-numeric result code \"" + codeString + "\": " + firstResponseLine);
            newE.initCause(e);
            throw newE;
        }

        // Is the fourth character a hyphen (if so, it's a continuation)?
        if ("-".equals(firstResponseLine.substring(3, 4))) {
            // The multi-line response ends with a line that begins with this:
            String endMultiLine = code + " ";

            while (true) {
                String subsequentResponse = in.readLine();
                if (subsequentResponse == null) {
                    throw new IOException("End of stream was reached before the complete multi-line response could be read.  What was read: " + StringUtils.collectionToDelimitedString(response, "\n"));
                }
                
                if (subsequentResponse.startsWith(endMultiLine)) {
                    response.add(subsequentResponse.substring(4));
                    break;
                }
                
                response.add(subsequentResponse);
            }
        }

        return new FtpResponse(code, response.toArray(new String[response.size()]));
    }
}
