//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Add serialVersionUID, refactor logging. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.opennms.core.utils.StreamUtils;
import org.opennms.web.MissingParameterException;

/**
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class MailerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected static final String[] REQUIRED_FIELDS = new String[] { "sendto", "subject", "username", "msg" };

    protected String redirectSuccess;

    protected String mailProgram;

    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();

        this.redirectSuccess = config.getInitParameter("redirect.success");
        this.mailProgram = config.getInitParameter("mail.program");

        if (this.redirectSuccess == null) {
            throw new ServletException("Missing required init parameter: redirect.success");
        }

        if (this.mailProgram == null) {
            throw new ServletException("Missing required init parameter: mail.program");
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sendto = request.getParameter("sendto");
        String subject = request.getParameter("subject");
        String msg = request.getParameter("msg");
        String username = request.getRemoteUser();

        if (log().isDebugEnabled()) {
            log().debug("To: " + sendto + ", Subject: " + subject + ", message: " + msg + ", username: " + username);
        }

        if (sendto == null) {
            throw new MissingParameterException("sendto", REQUIRED_FIELDS);
        }

        if (subject == null) {
            throw new MissingParameterException("subject", REQUIRED_FIELDS);
        }

        if (msg == null) {
            throw new MissingParameterException("msg", REQUIRED_FIELDS);
        }

        if (username == null) {
            username = "";
        }

        String[] cmdArgs = { this.mailProgram, "-s", subject, sendto };
        Process process = Runtime.getRuntime().exec(cmdArgs);

        // send the message to the stdin of the mail command
        PrintWriter stdinWriter = new PrintWriter(process.getOutputStream());
        stdinWriter.print(msg);
        stdinWriter.close();

        // get the stderr to see if the command failed
        BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        if (err.ready()) {
            // get the error message
            StringWriter tempErr = new StringWriter();
            StreamUtils.streamToStream(err, tempErr);
            String errorMessage = tempErr.toString();

            // log the error message
            log().warn("Read from stderr: " + errorMessage);

            // send the error message to the client
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            StreamUtils.streamToStream(new StringReader(errorMessage), out);
            out.close();
        } else {
            response.sendRedirect(this.redirectSuccess);
        }
    }
    
    private Logger log() {
        return Logger.getLogger("WEB.MAIL");
    }
}
