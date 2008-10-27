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

package org.opennms.web.jasperws;

/**
 * This class hold constants for the JasperWebserviceintegration. The constants
 * describe the parameters in the configuration-file.
 * <p>
 * <ol>
 * <li>OPENNMS_ENV :Parameter for environment</li>
 * <li>CONFIG_FILE :location and name of the configuration-file</li>
 * <li>CONFIG_DELIMITER :Delimiter for parameter and values</li>
 * <li>CONFIG_COMMENT :Indicate an comment and ignore this lines</li>
 * <li>CONFIG_URL :Parameter for JasperServer-Webservice-URL</li>
 * <li>CONFIG_REPORT_BASE :Parameter for your Report-Repository</li>
 * <li>CONFIG_USER :Parameter for your user-name for the web-service</li>
 * <li>CONFIG_PASS :Parameter for your user-password for the web-service</li>
 * </ol>
 * </p>
 * 
 * @author <a href="mailto:r.trommer@open-factory.org">Ronny Trommer</a>
 */
public class JasperClientConstants
{
  /** Parameter for environment */
  public static final String OPENNMS_ENV = "OPENNMS_HOME";

  /** Location and name of the config-file */
  public static final String CONFIG_FILE = "etc/jasperclient.enable";

  /** Delimiter for parameter and values */
  public static final String CONFIG_DELIMITER = "=";

  /** Indicate an comment and ignore this lines */
  public static final String CONFIG_COMMENT = "#";

  /** Parameter for JasperServer-Webservice-URL */
  public static final String CONFIG_URL = "repositoryurl";

  /** Parameter for JasperServer-Report base-directory */
  public static final String CONFIG_ARCHIV_BASE = "archiv_base";

  /** Parameter for your Report-Repository */
  public static final String CONFIG_REPORT_BASE = "report_base";

  /** Parameter for your user-name for the web-service */
  public static final String CONFIG_USER = "jasperuser";

  /** Parameter for your user-password for the web-service */
  public static final String CONFIG_PASS = "jasperpassword";

  /** Parameter for access to ReportUnit-Folder */
  public static final String REPORT_UNIT_FOLDER = "folder";
  
  /** Specify a ReportUnit-Folder */
  public static final String REPORT_UNIT_TYPE_FOLDER = "com.jaspersoft.jasperserver.api.metadata.common.domain.Folder";
  
  /** Specify a ReportUnit-Report
  public static final String REPORT_UNIT_TYPE_REPORT = "com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit";
  
  /** HTTP-Request-Parameter for report-folder */
  public static final String HTTP_REQUEST_REPORT_FOLDER = "folder";
}
