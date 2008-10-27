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
 * describe the parameters in the configurationfile.
 * <p>
 * <ol>
 * <li>PDF_EXTENSION :Fileextension for PDF</li>
 * <li>XLS_EXTENSION :Fileextension for XLS</li>
 * <li>CSV_EXTENSION :Fileextension for CSV</li>
 * <li>HTML_EXTENSION :Fileextension for HTML</li>
 * <li>TEMP_REPORTS :Basedirectory for temp-files</li>
 * <li>PDF_TEMP_STORE :temporary location for current PDF-Report</li>
 * <li>XLS_TEMP_STORE :temporary location for current XLS-Report</li>
 * <li>CSV_TEMP_STORE :temporary location for current CSV-Report</li>
 * <li>HTML_TEMP_STORE :temporary location for current HTML-Report</li>
 * </ol>
 * </p>
 * 
 * @author <a href="mailto:r.trommer@open-factory.org">Ronny Trommer</a>
 */
public class JasperReportConstants
{
  /** Fileextension for PDF */
  public static final String PDF_EXTENSION = ".pdf";

  /** Fileextension for XLS */
  public static final String XLS_EXTENSION = ".xls";

  /** Fileextension for CSV */
  public static final String CSV_EXTENSION = ".csv";

  /** Fileextension for HTML */
  public static final String HTML_EXTENSION = ".html";

  /** Basedirectory for temp-files */
  public static final String TEMP_REPORTS = "WEB-INF/tmp";

  /** Temporary location for current PDF-Report */
  public static final String PDF_TEMP_STORE = TEMP_REPORTS + "/" + "pdf";

  /** Temporary location for current XLS-Report */
  public static final String XLS_TEMP_STORE = TEMP_REPORTS + "/" + "xls";

  /** Temporary location for current CSV-Report */
  public static final String CSV_TEMP_STORE = TEMP_REPORTS + "/" + "csv";

  /** Temporary location for current HTML-Report */
  public static final String HTML_TEMP_STORE = TEMP_REPORTS + "/" + "html";

  /** Report type PDF */
  public static final String REPORT_TYPE_PDF = "pdf";

  /** Report type CSV */
  public static final String REPORT_TYPE_CSV = "csv";

  /** Report type XLS */
  public static final String REPORT_TYPE_XLS = "xls";

  /** Report type Flash */
  public static final String REPORT_TYPE_FLASH = "flash";

  /** Content type for PDF-Response */
  public static final String CONTENT_TYPE_PDF = "application/pdf";

  /** Content type for XLS-Response */
  public static final String CONTENT_TYPE_XLS = "application/xls";

  /** Content type for CSV-Response */
  public static final String CONTENT_TYPE_CSV = "application/csv";

  /** Content type for HTML-Response */
  public static final String CONTENT_TYPE_HTML = "application/html";
  
  /** Request-Parameter for report */
  public static final String HTTP_REQUEST_REPORT_NAME = "report";
}
