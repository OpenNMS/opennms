//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc. All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 18: Fix multi-line response handling; bug #1875.  Fix from
//              Victor Jerlin <victor.jerlin@involve.com.mt> - dj@opennms.org
// 2004 Apr 28: Modified to extend AbstractTcpPlugin 
// 2003 Jul 18: Fixed exception to enable retries.
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Nov 14: Used non-blocking I/O for speed improvements.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//
//
package org.opennms.netmgt.capsd.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

import org.apache.log4j.Category;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.AbstractTcpPlugin;
import org.opennms.netmgt.capsd.ConnectionConfig;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existance of an FTP server on remote interfaces. The class implements the
 * Plugin interface that allows it to be used along with other plugins by the
 * daemon.
 * </P>
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * 
 * 
 */
public final class FtpPlugin extends AbstractTcpPlugin {

    /**
     * <P>
     * The default port on which the host is checked to see if it supports FTP.
     * </P>
     */
    private static final int DEFAULT_PORT = 21;

    /**
     * Default number of retries for FTP requests.
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for FTP requests.
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds

    /**
     * The regular expression test used to determine if the reply is a multi
     * line reply. A multi line reply is one that starts with "ddd-", and the
     * last is in the form of "ddd " where 'ddd' is the result code.
     * 
     */
    private static final RE MULTILINE_RESULT;

    /**
     * <P>
     * The capability name of the plugin.
     * </P>
     */
    private static final String PROTOCOL_NAME = "FTP";

    static {
        try {
            MULTILINE_RESULT = new RE("^[1-5][0-9]{2}-");
        } catch (RESyntaxException re) {
            throw new java.lang.reflect.UndeclaredThrowableException(re);
        }
    }

    /**
     * @param protocol
     * @param defaultPort
     * @param defaultTimeout
     * @param defaultRetries
     */
    public FtpPlugin() {
        super(PROTOCOL_NAME, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRY);
    }

    /**
     * @param socket
     * @param config
     * @param log
     * @param isAServer
     * @return
     * @throws IOException
     */
    protected boolean checkProtocol(Socket socket, ConnectionConfig config) throws IOException {

        boolean isAServer = false;

        Category log = ThreadCategory.getInstance(getClass());

        try {

            BufferedReader lineRdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Read responses from the server. The initial line should just
            // be a banner, but go ahead and check for multiline response
            // in the form of:
            //
            // 221-You have transferred 0 bytes in 0 files.
            // 221-Total traffic for this session was 102 bytes in 0 transfers.
            // 221 Thank you for using the FTP service on nethost0.
            //
            // Or:
            //
            // 221-Start of header
            // This could be anything
            // 221 End of header
            //
            String result = lineRdr.readLine();

            if (MULTILINE_RESULT.match(result)) {
	        // Ok we have a multi-line response...first three
                // chars of the response line are the return code.
                // The last line of the response will start with
                // return code followed by a space.
                String multiLineRC = "^" + new String(result.getBytes(), 0, 3) + " ";

                /** 
                 * Used to check for the end of a multiline response. The end of a multiline
                 * response is the same 3 digit response code followed by a space
                 */
                RE endMultiLineRe;

                // Create new regExp to look for last line
                // of this mutli line response
                try {
                    endMultiLineRe = new RE(multiLineRC);
                } catch (RESyntaxException ex) {
                    throw new java.lang.reflect.UndeclaredThrowableException(ex);
                }

                do {
                    result = lineRdr.readLine();
                } while (result != null && !endMultiLineRe.match(result));
            }

            if (result == null || result.length() == 0) {
                log.info("Received truncated response from ftp server " + config.getInetAddress().getHostAddress());
                return isAServer;
            }

            // Tokenize the last line result
            //
            StringTokenizer t = new StringTokenizer(result);
            int rc = Integer.parseInt(t.nextToken());
            if (rc > 99 && rc < 600) {
                //
                // FTP should recoginize the QUIT command
                //
                String cmd = "QUIT\r\n";
                socket.getOutputStream().write(cmd.getBytes());

                // Response from QUIT command may be a multi-line response.
                // We are expecting to get a response with an integer return
                // code in the first token. We can't ge sure that the first
                // response will give us what we want. Consider the following
                // reponse for example:
                //
                // 221-You have transferred 0 bytes in 0 files.
                // 221-Total traffic for this session was 102 bytes in 0
                // transfers.
                // 221 Thank you for using the FTP service on nethost0.
                //
                // In this case the final line of the response contains the
                // return
                // code we are looking for.
                result = lineRdr.readLine();

                if (MULTILINE_RESULT.match(result)) {
	            // Ok we have a multi-line response...first three
                    // chars of the response line are the return code.
                    // The last line of the response will start with
                    // return code followed by a space.
                    String multiLineRC = "^" + new String(result.getBytes(), 0, 3) + " ";

                    /** 
                     * Used to check for the end of a multiline response. The end of a multiline
                     * response is the same 3 digit response code followed by a space
                     */
                    RE endMultiLineRe;
    
                    // Create new regExp to look for last line
                    // of this mutli line response
                    try {
                        endMultiLineRe = new RE(multiLineRC);
                    } catch (RESyntaxException ex) {
                        throw new java.lang.reflect.UndeclaredThrowableException(ex);
                    }
    
                    do {
                        result = lineRdr.readLine();
                    } while (result != null && !endMultiLineRe.match(result));
                }

                if (result == null || result.length() == 0) {
                    log.info("Received truncated response from ftp server " + config.getInetAddress().getHostAddress());
                    return isAServer;
                }

                t = new StringTokenizer(result);
                rc = Integer.parseInt(t.nextToken());
                if (rc > 99 && rc < 600)
                    isAServer = true;

            }

        } catch (NumberFormatException e) {
            log.info("FtpPlugin: received invalid result code from server " + config.getInetAddress().getHostAddress(), e);
            isAServer = false;

        }

        return isAServer;
    }
}
