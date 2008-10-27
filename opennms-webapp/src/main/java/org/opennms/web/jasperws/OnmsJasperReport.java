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

import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opennms.web.jasperws.JasperReportConstants;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;

import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import com.jaspersoft.jasperserver.irplugin.JServer;

/**
 * This class request a specific report from the JasperServer. The report can be
 * converted as PDF, XLS, CSV, HTML with given Methods.
 * 
 * @author <a href="mailto:r.trommer@open-factory.org">Ronny Trommer</a>
 * 
 *         TODO: Convert in a stream without saving in temporary folder FIXME:
 *         Find a way to remove double code in getPdfReport (), getXlsReport ()
 *         FIXME: HTML-Report is not useable with resources like embedded
 *         images.
 */
public class OnmsJasperReport implements Serializable
{
  /** Serial ID */
  private static final long serialVersionUID = -8037337412971872431L;

  /** Your JasperServer */
  private JServer m_jserver;

  /** Path in web-application */
  private String m_servletContextPath;

  /** Name of the specific report */
  private String m_reportName;

  /** Resources from JasperServer */
  private ResourceDescriptor m_rd;

  /** Java-generic format for your report */
  private JasperPrint m_jprint;

  /** PDF-Exporter */
  private JRPdfExporter m_pdfExporter;

  /** CSV-Exporter */
  private JRCsvExporter m_csvExporter;

  /** XLS-Exporter */
  private JRXlsExporter m_xlsExporter;

  /** HTML-Exporter */
  private JRHtmlExporter m_htmlExporter;

  /** Configuration for your JasperServer */
  private JasperConfiguration m_jconfig;

  /** TODO: Further use for reports with parameter */
  private HashMap<String, String> m_parameterMap;

  /** Logger for this class and subclasses */
  private final Log logger = LogFactory.getLog (getClass ());

  /**
   * Constructor to get a specific report in different formats.
   * 
   * @param uri
   *          String URI to report
   * @param parameters
   *          Parameters from request
   * @throws Exception
   *           JasperConfiguration exceptions
   */
  public OnmsJasperReport (String uri, HashMap<String, String> parameters)
      throws Exception
  {
    /** Your JasperServer */
    this.m_jserver = new JServer ();
    this.m_rd = new ResourceDescriptor ();

    /** Load your JasperServer-Configuration */
    this.m_jconfig = new JasperConfiguration ();

    /** parameters from HTTP-Request */
    this.m_parameterMap = parameters;

    /** Set your configuration for the web-service */
    this.m_jserver.setUsername (this.m_jconfig.getJasperUser ());
    this.m_jserver.setPassword (this.m_jconfig.getJasperPassword ());
    this.m_jserver.setUrl (this.m_jconfig.getRepositoryUrl ());

    this.m_rd.setWsType (ResourceDescriptor.TYPE_REPORTUNIT);
    this.m_rd.setUriString (uri);

    /*
     * Try to use the JasperServer-Web-service
     */
    try
    {
      logger.info ("URI-String: " + this.m_rd.getUriString ());
      this.m_jprint = this.m_jserver.getWSClient ().runReport (this.m_rd,
          this.m_parameterMap);
    } catch (Exception e)
    {
      /* Connection failed, user, password, report-name wrong etc. */
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
    logger.info (this.m_jprint.getName ());
    this.m_reportName = this.m_jprint.getName ();
  }

  /**
   * Method convert the report from JPrint to PDF-Format and return the
   * filename.
   * 
   * @return File-location from generated PDF-Report
   */
  public String getPdfReport ()
  {
    /* Create full path and filename for the report */
    String file = this.m_servletContextPath + "/"
        + JasperReportConstants.PDF_TEMP_STORE + "/" + this.m_reportName
        + JasperReportConstants.PDF_EXTENSION;

    /* Convert the specific report in PDF-Format */
    this.m_pdfExporter = new JRPdfExporter ();
    this.m_pdfExporter.setParameter (JRExporterParameter.JASPER_PRINT,
        this.m_jprint);

    /*
     * Convert to a PDF-file
     */
    this.m_pdfExporter
        .setParameter (JRExporterParameter.OUTPUT_FILE_NAME, file);

    /*
     * Try to export in PDF-File
     */
    try
    {
      this.m_pdfExporter.exportReport ();
    } catch (JRException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }

    return file;
  }

  /**
   * Method convert the report from JPrint to XLS-Format and return the
   * filename.
   * 
   * @return File-location from generated XLS-Report
   */
  public String getXlsReport ()
  {
    /* Create full path and filename for the report */
    String file = this.m_servletContextPath + "/"
        + JasperReportConstants.XLS_TEMP_STORE + "/" + this.m_reportName
        + JasperReportConstants.XLS_EXTENSION;

    /* Convert the specific report in XLS-Format */
    this.m_xlsExporter = new JRXlsExporter ();
    this.m_xlsExporter.setParameter (JRExporterParameter.JASPER_PRINT,
        this.m_jprint);
    this.m_xlsExporter
        .setParameter (JRExporterParameter.OUTPUT_FILE_NAME, file);

    /*
     * Try to export in XLS-File
     */
    try
    {
      this.m_xlsExporter.exportReport ();
    } catch (JRException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }

    return file;
  }

  /**
   * Method convert the report from jprint to CSV-Format and return the
   * filename.
   * 
   * @return Filelocation from generated CSV-Report
   */
  public String getCsvReport ()
  {
    /* Create full path and filename for the report */
    String file = this.m_servletContextPath + "/"
        + JasperReportConstants.CSV_TEMP_STORE + "/" + this.m_reportName
        + JasperReportConstants.CSV_EXTENSION;

    /* Convert the specific report in CSV-Format */
    this.m_csvExporter = new JRCsvExporter ();
    this.m_csvExporter.setParameter (JRExporterParameter.JASPER_PRINT,
        this.m_jprint);
    this.m_csvExporter
        .setParameter (JRExporterParameter.OUTPUT_FILE_NAME, file);

    /*
     * Try to export in CSV-File
     */
    try
    {
      this.m_csvExporter.exportReport ();
    } catch (JRException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
    return file;
  }

  /**
   * Method convert the report from JPrint to HTML-Format and return the
   * filename.
   * 
   * @return File-location from generated HTML-Report
   */
  public String getHtmlReport ()
  {
    /* Create full path and filename for the report */
    String file = this.m_servletContextPath + "/"
        + JasperReportConstants.HTML_TEMP_STORE + "/" + this.m_reportName
        + JasperReportConstants.HTML_EXTENSION;

    /* Convert the specific report in HTML-Format */
    this.m_htmlExporter = new JRHtmlExporter ();
    this.m_htmlExporter.setParameter (JRExporterParameter.JASPER_PRINT,
        this.m_jprint);
    this.m_htmlExporter.setParameter (JRExporterParameter.OUTPUT_FILE_NAME,
        file);

    /*
     * Try to export in HTML-File
     */
    try
    {
      this.m_htmlExporter.exportReport ();
    } catch (JRException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
    return file;
  }

  /**
   * Method to return a PDF-Stream without saving in a temporary file
   * 
   * @param outputStream
   *          PDF-Report as stream
   */
  public void getPdfReportStream (OutputStream outputStream)
  {
    try
    {
      JasperExportManager.exportReportToPdfStream (this.m_jprint, outputStream);
    } catch (JRException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
  }

  /**
   * Method returns the name from requested report.
   * 
   * @return Report-name
   */
  public String getReportName ()
  {
    return this.m_reportName;
  }
}