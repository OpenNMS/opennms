/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.report.availability;

import java.io.IOException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.javamail.JavaMailer;
import org.opennms.javamail.JavaMailerException;

/**
 * 
 * @author <a href="mailto:sartin@opennms.org">Jonathan Sartin</a>
 */
public class ReportMailer {
	
	private static final String LOG4J_CATEGORY = "OpenNMS.Report";
	
	private Category log;
	
	private String m_filename;
	
	private String m_address;
	
	
	public ReportMailer() {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(AvailabilityReport.class);
	}
	
	
	public ReportMailer(String address, String filename) {
		this.m_address = address;
		this.m_filename = filename;
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(AvailabilityReport.class);
	}
	
	public void send() throws IOException {
        if (m_filename == null || m_address == null) {
            throw new IllegalArgumentException("Cannot take null paramters.");
        }

        try {
            JavaMailer jm = new JavaMailer();
            jm.setTo(m_address);
            jm.setSubject("OpenNMS Availability Report");
            jm.setFileName(m_filename);
            jm.setMessageText("Availability Report Mailed from JavaMailer class.");
            jm.mailSend();
        } catch (JavaMailerException e) {
            log.error("Caught JavaMailer exception sending file: " + m_filename, e);
            throw new IOException("Error sending file: " + m_filename);
        }
    }

	public String getAddress() {
		return m_address;
	}

	public void setAddress(String address) {
		this.m_address = address;
	}

	public String getFilename() {
		return m_filename;
	}

	public void setFilename(String filename) {
		this.m_filename = filename;
	}

}
