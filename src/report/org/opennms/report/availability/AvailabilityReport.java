//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.report.availability;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.report.datablock.PDFWriter;

/**
 * AvailabilityReport generates the Availability report in pdf format
 * 
 * @author      <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios</A>
 * @author      <A HREF="http://www.oculan.com">Oculan</A>
 */
public class AvailabilityReport extends Object
{
         /**
          * The log4j category used to log debug messsages
          * and statements.
          */
         private static final String LOG4J_CATEGORY = "OpenNMS.Report";

	/**
	 * Castor object that holds all the information required for the generating 
	 * xml to be translated to the pdf.
	 */
	private Report m_report = null;

	/**
	 * Default constructor 
	 */
	public AvailabilityReport(String author)
	{
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
                Category log = ThreadCategory.getInstance(this.getClass());
		if(log.isDebugEnabled())
	                log.debug("Inside AvailabilityReport");

		Calendar today 	= new GregorianCalendar();
		int day        	= today.get(Calendar.DAY_OF_MONTH);
	        int year       	= today.get(Calendar.YEAR);
		SimpleDateFormat smpMonth = new SimpleDateFormat("MMMMMMMMMMM");
		String month = smpMonth.format(new java.util.Date(today.getTime().getTime()));
                //int month     = today.get(Calendar.MONTH) + 1;
                int hour     	= today.get(Calendar.HOUR);
                int minute     	= today.get(Calendar.MINUTE);
                int second     	= today.get(Calendar.SECOND);
		Created created = new Created();
		created.setDay(day);
		created.setHour(hour);
		created.setMin(minute);
		created.setMonth(month);
		created.setSec(second);
		created.setYear(year);
		created.setContent(today.getTime().getTime());

		m_report = new Report();
		m_report.setCreated(created);
		m_report.setAuthor(author);

		if(log.isDebugEnabled())
		{
			log.debug("Leaving AvailabilityReport");
		}
	}

	/**
	 * This when invoked generates the data into report castor classes.
	 *
	 * @param logourl location of the logo to be displayed on the report
	 * @param categoryName of the logo to be displayed on the report
	 * @param reportFormat Report Format ("SVG" / all)
	 *
	 */
	public void getReportData(String logourl, String categoryName, String reportFormat) 
		throws ValidationException, MarshalException, IOException, Exception
	{
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance(this.getClass());
		if(log.isDebugEnabled())
		{
			log.debug("inside getReportData");
			log.debug("Category name  " + categoryName);
			log.debug("Report format   " + reportFormat);
			log.debug("logo  " + logourl);
		}

		m_report.setLogo( logourl );
		ViewInfo viewInfo = new ViewInfo();
		m_report.setViewInfo( viewInfo );
                org.opennms.report.availability.Categories categories =
                                        new org.opennms.report.availability.Categories();
		m_report.setCategories(categories);
		AvailabilityData availData = new AvailabilityData(categoryName, m_report, reportFormat);
		File file = new File(ConfigFileConstants.getHome() + "/share/reports/AvailReport.xml");
                try
                {
                        FileWriter fileWriter = new FileWriter(file);
                        Marshaller.marshal( m_report, fileWriter );
                        if(log.isDebugEnabled())
                                log.debug("The xml marshalled from the castor classes is saved in " + ConfigFileConstants.getHome() + "/share/reports/AvailReport.xml");
			fileWriter.close();
		}
                catch(ValidationException validex)
                {
			if(log.isEnabledFor(Priority.FATAL))
                                log.fatal("ValidationException " , validex);
                }
                catch(MarshalException marex)
                {
			if(log.isEnabledFor(Priority.FATAL))
                                log.fatal("MarshalException " , marex);
                }
                catch(IOException ioe)
                {
			if(log.isEnabledFor(Priority.FATAL))
                                log.fatal("IOException " , ioe);
                }		
	}

	/**
	 * Generate PDF from castor classes.
	 */
	public void generatePDF(String pdfFileName, OutputStream out, String format) throws Exception
	{
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
                Category log = ThreadCategory.getInstance(AvailabilityReport.class);
		if(log.isDebugEnabled())
			log.debug("inside generatePDF");
		File file = new File(ConfigFileConstants.getHome() + "/share/reports/AvailReport.xml");
		try
		{
			if(log.isDebugEnabled())
				log.debug("The xml marshalled from the castor classes is saved in " + ConfigFileConstants.getHome() + "/share/reports/AvailReport.xml");
			FileReader fileReader = new FileReader( file );
			PDFWriter pdfWriter = new PDFWriter(pdfFileName);
			Calendar calendar = new GregorianCalendar();
			long timeMillis = calendar.getTime().getTime();
			if(!format.equals("HTML"))
			{
				pdfWriter.generatePDF( fileReader, out, ConfigFileConstants.getHome() + "/share/reports/avail-"+ timeMillis +".fot" );
			}
			else
				pdfWriter.generateHTML( fileReader, out );
		}
                catch(Exception e)
                {
			if(log.isEnabledFor(Priority.FATAL))
	                        log.fatal("Exception " + e);
			throw e;
                }
		if(log.isInfoEnabled())
			log.info("leaving generatePDF");
	}

	/**
	 * Main method
	 */
	public static void main(String args[])
        {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
                Category log = ThreadCategory.getInstance(AvailabilityReport.class);
                String logourl = System.getProperty("image");
                String categoryName = System.getProperty("catName");
		if(categoryName == null || categoryName.equals(""))
			categoryName = "all";
                String format = System.getProperty("format");
		if(format == null || format.equals(""))
			format = "SVG";

		try {
			generateReport(logourl, categoryName, format);
		} catch (Exception e) {
			log.error("Caught Exception generating report", e);
		}
	}

	/**
	 * @param logourl
	 * @param categoryName
	 * @param format
	 */
	public static void generateReport(String logourl, String categoryName, String format) throws Exception {

		// This report will be invoked by the mailer script.
		// Only SVG formatted reports are needed.
		// 
		Category log = ThreadCategory.getInstance(AvailabilityReport.class);
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		String catFileName = categoryName.replace(' ', '-');
		String pdfFileName = null;
		if(format.equals("SVG"))
			pdfFileName = ConfigFileConstants.getHome() + "/share/reports/AVAIL-SVG-" + catFileName + fmt.format(new java.util.Date()) +".pdf";
		else if(format.equals("PDF"))
			pdfFileName = ConfigFileConstants.getHome() + "/share/reports/AVAIL-PDF-" + catFileName + fmt.format(new java.util.Date()) +".pdf";
		else if(format.equals("HTML"))
			pdfFileName = ConfigFileConstants.getHome() + "/share/reports/AVAIL-HTML-" + catFileName + fmt.format(new java.util.Date()) +".html";
		try
		{
			AvailabilityReport report = new AvailabilityReport("Unknown");	
			report.getReportData(logourl, categoryName, format);
			if(log.isInfoEnabled())
				log.info("Generated Report Data... ");
			File file = new File(pdfFileName);
			FileOutputStream pdfFileWriter = new FileOutputStream(file);
			String xslFileName = null;
			if(format.equals("SVG"))
				xslFileName = ConfigFileConstants.getFilePathString() +  ConfigFileConstants.getFileName(ConfigFileConstants.REPORT_SVG_XSL);
			else if(format.equals("PDF"))
				xslFileName = ConfigFileConstants.getFilePathString() +  ConfigFileConstants.getFileName(ConfigFileConstants.REPORT_PDF_XSL);
			else if(format.equals("HTML"))
				xslFileName = ConfigFileConstants.getFilePathString() +  ConfigFileConstants.getFileName(ConfigFileConstants.REPORT_HTML_XSL);
			report.generatePDF(xslFileName, pdfFileWriter, format); 
			if(log.isInfoEnabled())
			{
				log.debug("xsl -> " + xslFileName + " pdfFileName -> " + pdfFileName + " format -> " + format );
				log.info("Generated Report ... and saved as " + pdfFileName);
			}
		}
		catch(IOException ioe)
		{
			if(log.isEnabledFor(Priority.FATAL))
	                        log.fatal("IOException " , ioe);
		}
		catch(Exception e)
		{
			if(log.isEnabledFor(Priority.FATAL))
	                        log.fatal("Exception  " , e);
		}
	}
}

