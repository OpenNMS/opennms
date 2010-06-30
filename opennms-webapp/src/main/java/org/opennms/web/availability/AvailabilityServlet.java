//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// 2007 Jul 24: Organize imports, add serialVersionUID. - dj@opennms.org
// 2006 Aug 24: Fix MissingParameterExceptions and always return the list of
//              required parameters. - dj@opennms.org
// 2006 May 30: added a way to choose the date to run the availability reports.
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

package org.opennms.web.availability;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.web.MissingParameterException;
import org.opennms.web.ReportMailer;

/**
 * <p>AvailabilityServlet class.</p>
 *
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.6.12
 */
public class AvailabilityServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected String xslFileName;

    protected String pdfxslFileName;

    protected String svgxslFileName;

    // For the purpose of mailing out reports.
    protected String redirectSuccess;

    protected String redirectFailure;

    protected String redirectNoEmail;

    protected String scriptGenerateReport;

    protected String scriptMailReport;

    protected String useScript;

    protected String logo;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();

        this.redirectSuccess = config.getInitParameter("redirect.success");
        this.redirectFailure = config.getInitParameter("redirect.failure");
        this.redirectNoEmail = config.getInitParameter("redirect.noEmail");

        this.scriptGenerateReport = config.getInitParameter("script.generateReport");
        this.scriptMailReport = config.getInitParameter("script.mailReport");
        this.useScript = config.getInitParameter("script.useScript");
        this.logo = config.getInitParameter("report.logo");
        this.logo = getServletContext().getRealPath(this.logo);

        if (this.redirectSuccess == null) {
            throw new ServletException("Missing required init parameter: redirect.success");
        }

        if (this.redirectFailure == null) {
            throw new ServletException("Missing required init parameter: redirect.failure");
        }

        if (this.redirectNoEmail == null) {
            throw new ServletException("Missing required init parameter: redirect.noEmail");
        }

        if (this.scriptGenerateReport == null) {
            throw new ServletException("Missing required init parameter: script.generateReport");
        }

        if (this.scriptMailReport == null) {
            throw new ServletException("Missing required init parameter: script.mailReport");
        }

        if (this.useScript == null) {
            throw new ServletException("Missing required init parameter: script.useScript");
        }

        this.xslFileName = config.getInitParameter("xslt.filename");
        this.pdfxslFileName = config.getInitParameter("pdf.xslt.filename");
        this.svgxslFileName = config.getInitParameter("svg.xslt.filename");

        if (this.xslFileName == null) {
            throw new UnavailableException("Require an xslt.filename init parameter.");
        }

        if (this.pdfxslFileName == null) {
            throw new UnavailableException("Require an pdf.xslt.filename init parameter.");
        }

        if (this.svgxslFileName == null) {
            throw new UnavailableException("Require an svg.xslt.filename init parameter.");
        }
    }

    /** {@inheritDoc} */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String view = request.getParameter("view");
        String format = request.getParameter("format");
	String monthFormat = request.getParameter("monthformat");
        String category = request.getParameter("category");
        String username = request.getRemoteUser();
        String startMonth = request.getParameter("startMonth");
        String startDate = request.getParameter("startDate");
        String startYear = request.getParameter("startYear");

        String[] requiredParameters = new String[] {
                "view",
                "format",
                "category",
                "startMonth",
                "startDate",
                "startYear"
        };
        if (view == null) {
            throw new MissingParameterException("view", requiredParameters);
        }

        if (format == null) {
            throw new MissingParameterException("format", requiredParameters);
        }

        if (category == null) {
            throw new MissingParameterException("category", requiredParameters);
        }

        if (username == null) {
            username = "";
        }

        if (startMonth == null) {
            throw new MissingParameterException("startMonth",
                                                requiredParameters);
        }

        if (startYear == null) {
            throw new MissingParameterException("startYear",
                                                requiredParameters);
        }

        if (startDate == null) {
            throw new MissingParameterException("startDate",
                                                requiredParameters);
        }


        // TODO: Rework this so that initialise doesn't get called and the nasty
        // if then else is done better
        try {

            // Report to be displayed in HTML format.
            if (format.equals("HTML")) {
                ReportMailer reportMailer = new ReportMailer();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                String catFileName = category.replace(' ', '-');
                String filename = ConfigFileConstants.getHome() + "/share/reports/AVAIL-HTML-" + catFileName + fmt.format(new java.util.Date()) + ".html";
				
                reportMailer.initialise(filename, username, scriptGenerateReport, scriptMailReport, category, "HTML", monthFormat, startMonth, startDate, startYear);
                reportMailer.setLogoUrl(logo);
                reportMailer.setCategoryName(category);
                reportMailer.setFormat("HTML");

                // call setter on flag to use the script else use JavaMail
                reportMailer.setUseScript("true".equalsIgnoreCase(useScript));
                String emailAddr = reportMailer.getEmailAddress();
                if (emailAddr == null || emailAddr.trim().length() == 0) {
                    response.sendRedirect(this.redirectNoEmail);
                    return;
                }
                new Thread(reportMailer).start();
                response.sendRedirect(redirectSuccess);
            }
            // Report to be displayed in PDF format.
            else if (format.equals("PDF")) {
                ReportMailer reportMailer = new ReportMailer();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                String catFileName = category.replace(' ', '-');
                String filename = ConfigFileConstants.getHome() + "/share/reports/AVAIL-PDF-" + catFileName + fmt.format(new java.util.Date()) + ".pdf";
                reportMailer.initialise(filename, username, scriptGenerateReport, scriptMailReport, category, "PDF", monthFormat, startMonth, startDate, startYear);
                reportMailer.setLogoUrl(logo);
                reportMailer.setCategoryName(category);
                reportMailer.setFormat("PDF");

                // call setter on flag to use the script else use JavaMail
                reportMailer.setUseScript("true".equalsIgnoreCase(useScript));
                String emailAddr = reportMailer.getEmailAddress();
                if (emailAddr == null || emailAddr.trim().length() == 0) {
                    response.sendRedirect(this.redirectNoEmail);
                    return;
                }
                new Thread(reportMailer).start();
                response.sendRedirect(redirectSuccess);
            } else if (format.equals("SVG")) {
                ReportMailer reportMailer = new ReportMailer();
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
                String catFileName = category.replace(' ', '-');
                String filename = ConfigFileConstants.getHome() + "/share/reports/AVAIL-SVG-" + catFileName + fmt.format(new java.util.Date()) + ".pdf";
                reportMailer.initialise(filename, username, scriptGenerateReport, scriptMailReport, category, "SVG", monthFormat, startMonth, startDate, startYear);
                reportMailer.setLogoUrl(logo);
                reportMailer.setCategoryName(category);
                reportMailer.setFormat("SVG");

                // call setter on flag to use the script else use JavaMail
                reportMailer.setUseScript("true".equalsIgnoreCase(useScript));
                String emailAddr = reportMailer.getEmailAddress();
                if (emailAddr == null || emailAddr.trim().length() == 0) {
                    response.sendRedirect(this.redirectNoEmail);
                    return;
                }
                new Thread(reportMailer).start();
                response.sendRedirect(redirectSuccess);
            }
        } catch (Exception e) {
            throw new ServletException("AvailabilityServlet: ", e);
        }
    }

    /**
     * <p>streamToStream</p>
     *
     * @deprecated Should use {@link org.opennms.web.Util#streamToStream
     *             Util.streamToStream} instead.
     * @param in a {@link java.io.Reader} object.
     * @param out a {@link java.io.Writer} object.
     * @throws java.io.IOException if any.
     */
    protected void streamToStream(Reader in, Writer out) throws IOException {
        char[] b = new char[100];
        int length;

        while ((length = in.read(b)) != -1) {
            out.write(b, 0, length);
        }
    }

}
