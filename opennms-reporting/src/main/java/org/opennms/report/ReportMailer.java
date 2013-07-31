/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.report;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.core.logging.Logging;
import org.opennms.javamail.JavaMailer;
import org.opennms.javamail.JavaMailerException;

/**
 * <p>ReportMailer class.</p>
 *
 * @author <a href="mailto:sartin@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 */
public class ReportMailer {
    private static final Logger LOG = LoggerFactory.getLogger(ReportMailer.class);
    
	private static final String LOG4J_CATEGORY = "reports";
	
	private String m_filename;
	
	private String m_address;

	private String m_subject;
	
	/**
	 * <p>Constructor for ReportMailer.</p>
	 */
	public ReportMailer() {
	    this(null, null, null);
	}

	
	/**
	 * <p>Constructor for ReportMailer.</p>
	 *
	 * @param address a {@link java.lang.String} object.
	 * @param filename a {@link java.lang.String} object.
	 * @param subject a {@link java.lang.String} object.
	 */
	public ReportMailer(String address, String filename, String subject) {
		this.m_address = address;
		this.m_filename = filename;
		this.m_subject = subject;
		
		// TODO wrap the methods is probably better
		Logging.putPrefix(LOG4J_CATEGORY);
	}
	
	/**
	 * <p>send</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	public void send() throws IOException {
        if (m_filename == null || m_address == null) {
            throw new IllegalArgumentException("Cannot take null paramters.");
        }

        try {
            JavaMailer jm = new JavaMailer();
            jm.setTo(m_address);
            jm.setSubject(m_subject);
            jm.setFileName(m_filename);
            jm.setMessageText(m_subject + " Mailed from JavaMailer class.");
            jm.mailSend();
        } catch (JavaMailerException e) {
            LOG.error("Caught JavaMailer exception sending file: {}", m_filename, e);
            throw new IOException("Error sending file: " + m_filename);
        }
    }

	/**
	 * <p>getAddress</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getAddress() {
		return m_address;
	}

	/**
	 * <p>setAddress</p>
	 *
	 * @param address a {@link java.lang.String} object.
	 */
	public void setAddress(String address) {
		this.m_address = address;
	}

	/**
	 * <p>getFilename</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFilename() {
		return m_filename;
	}

	/**
	 * <p>setFilename</p>
	 *
	 * @param filename a {@link java.lang.String} object.
	 */
	public void setFilename(String filename) {
		this.m_filename = filename;
	}

}
