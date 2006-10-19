//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.report.availability;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.core.io.Resource;

public class AvailabilityCalculator {

	private static final String LOG4J_CATEGORY = "OpenNMS.Report";

	/**
	 * String of Months
	 */

	public static String[] months = new String[] { "January", "February",
			"March", "April", "May", "June", "July", "August", "September",
			"October", "November", "December" };

	// calendar
	
	private Calendar calendar;
	
	// format for report (calendar or classic)

	private String monthFormat;

	// eventual output format

	private String reportFormat;

	// start date

	private String startDate;

	// start month

	private String startMonth;

	// start year

	private String startYear;

	// URL for logo

	private String logoURL;

	// output file name

	private String outputFileName;

	// author

	private String author;

	// category name

	private String categoryName;
	
	private Resource outputResource;

	/**
	 * Castor object that holds all the information required for the generating
	 * xml to be translated to the pdf.
	 */

	private Report m_report = null;

	private Category log;

	public AvailabilityCalculator() {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		if (log.isDebugEnabled())
			log.debug("Inside AvailabilityCalculator");
		
		m_report = new Report();
		m_report.setAuthor(author);
		
	}

	public void calculate() throws AvailabilityCalculationException {
		
		m_report.setLogo(logoURL);
		ViewInfo viewInfo = new ViewInfo();
		m_report.setViewInfo(viewInfo);
		org.opennms.report.availability.Categories categories = new org.opennms.report.availability.Categories();
		m_report.setCategories(categories);
		try {

			Calendar today = new GregorianCalendar();
			int day = Integer.parseInt(startDate);
			int year = Integer.parseInt(startYear);
			String month = months[Integer.parseInt(startMonth)];
			int hour = today.get(Calendar.HOUR);
			int minute = today.get(Calendar.MINUTE);
			int second = today.get(Calendar.SECOND);
			Created created = new Created();
			created.setDay(day);
			created.setHour(hour);
			created.setMin(minute);
			created.setMonth(month);
			created.setSec(second);
			created.setYear(year);
			created.setContent(today.getTime().getTime());
			m_report.setCreated(created);
			if (log.isDebugEnabled()){
				log.debug("Populating datastructures and calculating availabilty");
				log.debug("category:     " + categoryName);
				log.debug("monthFormat:  " + monthFormat);
				log.debug("reportFormat: " + reportFormat);
			}
			AvailabilityData availData = new AvailabilityData(categoryName,
					m_report, reportFormat, monthFormat,
					calendar, startMonth, startDate, startYear);


	
		} catch (MarshalException me) {
			log.fatal("MarshalException ", me);
			throw new AvailabilityCalculationException(me);
		} catch (ValidationException ve) {
			log.fatal("Validation Exception ", ve);
			throw new AvailabilityCalculationException(ve);
		} catch (IOException ioe) {
			log.fatal("Validation Exception ", ioe);
			throw new AvailabilityCalculationException(ioe);
		} catch (Exception e) {
			log.fatal("Exception ", e);
			throw new AvailabilityCalculationException(e);
		}

	}
	
	public void writeXML() throws AvailabilityCalculationException {
		try {
			marshal(outputResource.getFile());
		} catch (AvailabilityCalculationException e) {
			log.fatal("Unable to marshal report");
			throw new AvailabilityCalculationException(e);
		}  catch (IOException ioe) {
			log.fatal("IO Exception ", ioe);
			throw new AvailabilityCalculationException(ioe);
		}
	}
	
	public void writeXML(String fileName) throws AvailabilityCalculationException {
		try {
			File outputFile = new File(fileName);
			marshal(outputFile);
		} catch (AvailabilityCalculationException e) {
			log.fatal("Unable to marshal report");
			throw new AvailabilityCalculationException(e);
		}
	}
	
	public void marshal(File outputFile) throws AvailabilityCalculationException  {
		try {
			FileWriter fileWriter = new FileWriter(outputFile);
			Marshaller marshaller = new Marshaller(fileWriter);
			marshaller.setSuppressNamespaces(true);
			marshaller.marshal(m_report);
			if (log.isDebugEnabled())
				log	.debug("The xml marshalled from the castor classes is saved in " +
						outputFile.getAbsoluteFile());
			fileWriter.close();
		} catch (MarshalException me) {
			log.fatal("MarshalException ", me);
			throw new AvailabilityCalculationException(me);
		} catch (ValidationException ve) {
			log.fatal("Validation Exception ", ve);
			throw new AvailabilityCalculationException(ve);
		} catch (IOException ioe) {
			log.fatal("IO Exception ", ioe);
			throw new AvailabilityCalculationException(ioe);
		}
	}

	public String getLogoURL() {
		return logoURL;
	}

	public void setLogoURL(String logoURL) {
		this.logoURL = logoURL;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getStartMonth() {
		return startMonth;
	}

	public void setStartMonth(String startMonth) {
		this.startMonth = startMonth;
	}

	public String getStartYear() {
		return startYear;
	}

	public void setStartYear(String startYear) {
		this.startYear = startYear;
	}

	public String getOutputFileName() {
		return outputFileName;
	}
	
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getMonthFormat() {
		return monthFormat;
	}

	public void setMonthFormat(String monthFormat) {
		this.monthFormat = monthFormat;
	}

	public String getReportFormat() {
		return reportFormat;
	}

	public void setReportFormat(String reportFormat) {
		this.reportFormat = reportFormat;
	}

	public Report getReport() {
		return m_report;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	public void setOutputResource(Resource outputResource) {
		this.outputResource = outputResource;
	}

}
