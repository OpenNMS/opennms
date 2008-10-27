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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.opennms.web.jasperws.JasperClientConstants;

import org.opennms.web.jasperws.OnmsJasperClient;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles the HTTP-Requests. Instantiate the OpenNMS-JasperClient
 * and build the requests for the Jasper-Web-Service.
 * <p>
 * <ol>
 * <li>FIXME: The handling to traverse the folder is not very well implemented,
 * it works but should be redesigned.</li>
 * <li>TODO: If anyone has an idea to get this servlets in a better structur, do
 * not hesitate to contact me. Thats my first servlet in OpenNMS and Spring :)</li>
 * </ol>
 * </p>
 * 
 * @author <a href="mailto:r.trommer@open-factory.org">Ronny Trommer alias
 *         Indigo</a>
 */
public class JasperClientController implements Controller
{
  /** Logger for this class and subclasses */
  private final Log logger = LogFactory.getLog (getClass ());

  /** JasperServer-Client */
  private OnmsJasperClient m_jsclient;

  /** Requested list with ReportUnits */
  private List<ResourceDescriptor> m_rdList;

  /** Report and Folder for as model in view jsclient.jsp */
  private Map<String, List<ResourceDescriptor>> m_model;

  /** URI for specific folder */
  private String m_uri = "";

  /**
   * Handle the Request jasperws/jsclient.htm and the response. Get the session
   * context and initialize URIs.
   * 
   * @param request
   *          HTTP request
   * @param response
   *          HTTP response
   * @return ModelAndView WEB-INF/jsp/jasper/jsclient.jsp
   * @throws ServletException
   *           ServletException
   * @throws IOException
   *           IOException
   * @throws Exception
   *           Parameter not set
   */
  public ModelAndView handleRequest (HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException,
      Exception
  {
    /*
     * Map for the report entries
     */
    this.m_model = new HashMap<String, List<ResourceDescriptor>> ();

    /*
     * Try to load the JasperConfiguration and connect to the JasperServer with
     * Web-service-client.
     */
    try
    {
      this.m_jsclient = new OnmsJasperClient ();
      logger.debug ("Init: create configuration");
    } catch (Exception e)
    {
      logger.error ("Creating OnmsJasperClient failed!");
      logger.error ("Error: " + e.getMessage ());
    }

    if (request.getParameter (JasperClientConstants.HTTP_REQUEST_REPORT_FOLDER) != null)
    {
      this.m_uri = request
          .getParameter (JasperClientConstants.HTTP_REQUEST_REPORT_FOLDER);
      logger.debug ("Request URI set to: " + this.m_uri);
    } else
    {
      this.m_uri = this.m_jsclient.getReportBase ();
    }

    /*
     * Execute the request to JasperServer and retrieve a list with all
     * ReportUnits.
     */
    this.m_jsclient.setReportUri (this.m_uri);
    this.m_rdList = this.m_jsclient.getDescriptorList ();
    logger.debug ("DescriptorList - size: " + this.m_rdList.size ());

    /*
     * Put the list of all reports in the Model
     */
    this.m_model.put ("folderEntries", this.m_rdList);

    /*
     * Return the Model to jasper/jsclient.jsp
     */
    return new ModelAndView ("jasperws/jsclient", "model", this.m_model);
  }
}