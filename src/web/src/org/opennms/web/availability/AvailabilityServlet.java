//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
//

package org.opennms.web.availability;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.text.SimpleDateFormat;
import org.opennms.core.resource.Vault;
import org.opennms.report.availability.*;
import org.opennms.web.MissingParameterException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.web.ServletInitializer;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.opennms.web.ReportMailer;
import org.apache.xerces.parsers.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Category;

/**
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios</A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class AvailabilityServlet extends HttpServlet
{
	static Category log = Category.getInstance(AvailabilityServlet.class.getName());
	protected String xslFileName;
	protected String pdfxslFileName;
	protected String svgxslFileName;

	// For the purpose of mailing out reports.
        protected String redirectSuccess;
        protected String redirectFailure;
        protected String redirectNoEmail;
        protected String scriptGenerateReport;
        protected String scriptMailReport;
	
	public void init() throws ServletException {
		ServletConfig config = this.getServletConfig();

                this.redirectSuccess = config.getInitParameter("redirect.success");
                this.redirectFailure = config.getInitParameter("redirect.failure");
                this.redirectNoEmail = config.getInitParameter("redirect.noEmail");

                this.scriptGenerateReport = config.getInitParameter("script.generateReport");
                this.scriptMailReport = config.getInitParameter("script.mailReport");

                if( this.redirectSuccess == null ) {
                        throw new ServletException("Missing required init parameter: redirect.success");
                }

                if( this.redirectFailure == null ) {
                        throw new ServletException("Missing required init parameter: redirect.failure");
                }

                if( this.redirectNoEmail == null ) {
                        throw new ServletException("Missing required init parameter: redirect.noEmail");
                }

                if( this.scriptGenerateReport == null ) {
                        throw new ServletException("Missing required init parameter: script.generateReport");
                }

                if( this.scriptMailReport == null ) {
                        throw new ServletException("Missing required init parameter: script.mailReport");
                }

		this.xslFileName = config.getInitParameter( "xslt.filename" );
		this.pdfxslFileName = config.getInitParameter( "pdf.xslt.filename" );
		this.svgxslFileName = config.getInitParameter( "svg.xslt.filename" );

		if( this.xslFileName == null ) {
		    throw new UnavailableException( "Require an xslt.filename init parameter." );
		}

		if( this.pdfxslFileName == null ) {
		    throw new UnavailableException( "Require an pdf.xslt.filename init parameter." );
		}

		if( this.svgxslFileName == null ) {
		    throw new UnavailableException( "Require an svg.xslt.filename init parameter." );
		}
	}


	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
		String view = request.getParameter( "view" );
		String format = request.getParameter( "format" );
		String category = request.getParameter( "category" );
		String username = request.getRemoteUser();
		ServletConfig config = this.getServletConfig();
        
		
		if( view == null ) {
			throw new MissingParameterException( "view" );            
		}

		if( format == null) {
			throw new MissingParameterException( "format" );
		}

		if( category == null) {
			throw new MissingParameterException( "category" );
		}

		if( username == null ) {
			username = "";
		}

		try 
		{

			// Report to be displayed in HTML format.
			if(format.equals("HTML"))		
			{
				ReportMailer reportMailer = new ReportMailer();
                                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                                String catFileName = category.replace(' ', '-');
                                String filename = ConfigFileConstants.getHome() + "/share/reports/AVAIL-HTML-" + catFileName+ fmt.format(new java.util.Date()) +".html";
				reportMailer.initialise(filename, username, scriptGenerateReport, scriptMailReport, category, "HTML");
				String emailAddr = reportMailer.getEmailAddress();
                                if(emailAddr == null || emailAddr.trim().length() == 0)
                                {
                                        response.sendRedirect(this.redirectNoEmail);
                                        return;
                                }
                                new Thread(reportMailer).start();
                                response.sendRedirect(redirectSuccess);
			}
			// Report to be displayed in PDF format.
			else if(format.equals("PDF"))	
			{
				ReportMailer reportMailer = new ReportMailer();
                                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                                String catFileName = category.replace(' ', '-');
                                String filename = ConfigFileConstants.getHome() + "/share/reports/AVAIL-PDF-" + catFileName + fmt.format(new java.util.Date()) +".pdf";
                                reportMailer.initialise(filename, username, scriptGenerateReport, scriptMailReport, category, "PDF");
                                String emailAddr = reportMailer.getEmailAddress();
                                if(emailAddr == null || emailAddr.trim().length() == 0)
                                {
                                        response.sendRedirect(this.redirectNoEmail);
                                        return;
                                }
                                new Thread(reportMailer).start();
                                response.sendRedirect(redirectSuccess);
			}
			else if(format.equals("SVG"))
			{
				ReportMailer reportMailer = new ReportMailer();
				SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
				String catFileName = category.replace(' ', '-');
				String filename = ConfigFileConstants.getHome() + "/share/reports/AVAIL-SVG-" + catFileName + fmt.format(new java.util.Date()) +".pdf";
				reportMailer.initialise(filename, username, scriptGenerateReport, scriptMailReport, category, "SVG");
				String emailAddr = reportMailer.getEmailAddress();
				if(emailAddr == null || emailAddr.trim().length() == 0) 
				{
					response.sendRedirect(this.redirectNoEmail);
                                	return;
                        	}
				new Thread(reportMailer).start();
				response.sendRedirect(redirectSuccess);
			}
		}
		catch( Exception e ) {
			throw new ServletException( "AvailabilityServlet: ", e );
		}
	}

	/** 
	 * @deprecated Should use {@link org.opennms.web.Util#streamToStream 
	 * Util.streamToStream} instead.
	 */
	protected void streamToStream( Reader in, Writer out ) throws IOException 
	{
		char[] b = new char[100];
		int length;

		while((length = in.read(b)) != -1) 
		{
			out.write(b, 0, length);
		}
	}
}
