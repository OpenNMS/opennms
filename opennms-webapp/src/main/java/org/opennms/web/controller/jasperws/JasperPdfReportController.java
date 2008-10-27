//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.web.controller.jasperws;

import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.jasperws.JasperReportConstants;
import org.opennms.web.jasperws.OnmsJasperReport;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * This class represents an successful created Report as
 * PDF-HTTP-Responsestream.
 * 
 * @author <a href="mailto:r.trommer@open-factory.org">Ronny Trommer</a> FIXME:
 *         Find a way to remove doubling code for every reportformat
 */
public class JasperPdfReportController implements Controller
{
  /** Generic report */
  private OnmsJasperReport m_jasperReport;

  /** Reportfilename */
  private String m_reportName;

  /**
   * Handle the Request jasperws/pdfreport.htm
   * 
   * @param request
   *          HTTP-Request
   * @param response
   *          HTTP-Response
   * @return ModelAndView PDF-Report or jasper/reporterror.jsp
   * @throws Exception
   *           Exception
   */
  public ModelAndView handleRequest (HttpServletRequest request,
      HttpServletResponse response) throws Exception
  {
    /* Request the specific report from JasperServer */
    this.m_jasperReport = new OnmsJasperReport (request
        .getParameter (JasperReportConstants.HTTP_REQUEST_REPORT_NAME), this
        .getParameters (request));

    this.m_reportName = this.m_jasperReport.getReportName ()
        + JasperReportConstants.PDF_EXTENSION;

    response.setContentType (JasperReportConstants.CONTENT_TYPE_PDF);
    response.addHeader ("Content-Disposition", "inline; filename="
        + this.m_reportName + "." + JasperReportConstants.PDF_EXTENSION);
    response.addHeader ("Pragma", "public");
    response.addHeader ("Cache-Control", "max-age=0");

    this.m_jasperReport.getPdfReportStream (response.getOutputStream ());

    response.getOutputStream ().flush ();
    response.getOutputStream ().close ();

    /* No ModelAndView should be returned */
    return null;
  }

  /**
   * Extract the parameters from request and do mapping for Jasper-Reports
   * 
   * @param req
   *          HTTP-Request
   * @return HashMap Parameter-Map for JasperServer
   */
  @SuppressWarnings ("unchecked")
  public HashMap<String, String> getParameters (HttpServletRequest req)
  {
    /* HashMap with parameters for JasperServer */
    HashMap<String, String> hm = new HashMap<String, String> ();

    /* Get all parameters from request */
    Enumeration<String> enu = req.getParameterNames ();
    enu.nextElement ();

    /* for all request parameters map to JasperServer parameters */
    while (enu.hasMoreElements ())
    {
      String key = enu.nextElement ();
      hm.put (key, req.getParameter (key));
    }

    /* Parameter-map for JasperServer */
    return hm;
  }
}
