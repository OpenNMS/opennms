//
// Copyright (C) 2001 Oculan Corp.
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
//      Brian Weaver   <weave@opennms.org>
//      http://www.opennms.org/
//
//

package org.opennms.web.mail;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Category;
import org.opennms.web.MissingParameterException;
import org.opennms.web.Util;
import org.opennms.core.resource.Vault;


/**
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios</A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A> 
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class MailerServlet extends HttpServlet
{
    protected static final String[] REQUIRED_FIELDS = new String[] { "sendto", "subject", "username", "msg" };
    protected Category log = Category.getInstance("WEB.MAIL");
    
    protected String redirectSuccess;
    protected String mailProgram;
    
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();
        
        this.redirectSuccess = config.getInitParameter("redirect.success");
        this.mailProgram = config.getInitParameter("mail.program");
        
        if( this.redirectSuccess == null ) {
            throw new ServletException("Missing required init parameter: redirect.success");
        }
        
        if( this.mailProgram == null ) {
            throw new ServletException("Missing required init parameter: mail.program");
        }
    }
    

    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        String sendto   = request.getParameter("sendto");
        String subject  = request.getParameter("subject");
        String msg      = request.getParameter("msg");
        String username = request.getRemoteUser();

        this.log.debug( "To: " + sendto + ", Subject: " + subject + ", message: " + msg + ", username: " + username );        

        if( sendto == null) {
            throw new MissingParameterException( "sendto", REQUIRED_FIELDS );
        }

        if( subject == null) {
            throw new MissingParameterException( "subject", REQUIRED_FIELDS );
        }

        if( msg == null) {
            throw new MissingParameterException( "msg", REQUIRED_FIELDS );
        }
        
        if( username == null ) {
            username = "";
        }

        String[] cmdArgs = { this.mailProgram , "-s" , subject, sendto };
        Process process = Runtime.getRuntime().exec( cmdArgs );
        
        //send the message to the stdin of the mail command
        PrintWriter stdinWriter = new PrintWriter(process.getOutputStream());
        stdinWriter.print( msg );
        stdinWriter.close();

        //get the stderr to see if the command failed
        BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        if( err.ready() ) {
            //get the error message
            StringWriter tempErr = new StringWriter();            
            Util.streamToStream(err, tempErr);
            String errorMessage = tempErr.toString();
            
            //log the error message
            this.log.warn("Read from stderr: " + errorMessage);
            
            //send the error message to the client
            response.setContentType( "text/plain" );
            PrintWriter out = response.getWriter();            
            Util.streamToStream( new StringReader(errorMessage), out );
            out.close();            
        }
        else {
            response.sendRedirect(this.redirectSuccess);
        }
    }
}
