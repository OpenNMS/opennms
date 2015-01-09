/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.monitor.internal.config;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.opennms.sms.monitor.internal.SequenceException;

/**
 * <p>SequenceConfigFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SequenceConfigFactory {
	private static SequenceConfigFactory m_singleton = null;
	private JAXBContext m_context;

	private SequenceConfigFactory() {
	}

	/**
	 * <p>getInstance</p>
	 *
	 * @return a {@link org.opennms.sms.monitor.internal.config.SequenceConfigFactory} object.
	 */
	public static synchronized SequenceConfigFactory getInstance() {
		if (m_singleton == null) {
			m_singleton = new SequenceConfigFactory();
		}
		return m_singleton;
	}

	/**
	 * <p>initializeContext</p>
	 *
	 * @throws javax.xml.bind.JAXBException if any.
	 */
	protected synchronized void initializeContext() throws JAXBException {
		if (m_context == null) {
			m_context = JAXBContext.newInstance(
				MobileSequenceConfig.class,
				SequenceSessionVariable.class,
				SmsSequenceRequest.class,
				UssdSequenceRequest.class,
				SmsSequenceResponse.class,
				UssdSequenceResponse.class,
				SmsFromRecipientResponseMatcher.class,
				SmsSourceMatcher.class,
				TextResponseMatcher.class,
				UssdSessionStatusMatcher.class
			);
		}
	}

	/**
	 * <p>getMarshaller</p>
	 *
	 * @return a {@link javax.xml.bind.Marshaller} object.
	 * @throws javax.xml.bind.JAXBException if any.
	 */
	protected Marshaller getMarshaller() throws JAXBException {
		synchronized(m_context) {
			Marshaller m = m_context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new MobileSequenceNamespacePrefixMapper());
			return m;
		}
	}

	/**
	 * <p>getUnmarshaller</p>
	 *
	 * @return a {@link javax.xml.bind.Unmarshaller} object.
	 * @throws javax.xml.bind.JAXBException if any.
	 */
	protected Unmarshaller getUnmarshaller() throws JAXBException {
		synchronized(m_context) {
			Unmarshaller u = m_context.createUnmarshaller();
			u.setSchema(null);
			u.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
			return u;
		}
	}

	/**
	 * <p>getSequenceForXml</p>
	 *
	 * @param sequenceXml a {@link java.lang.String} object.
	 * @return a {@link org.opennms.sms.monitor.internal.config.MobileSequenceConfig} object.
	 * @throws org.opennms.sms.monitor.internal.SequenceException if any.
	 */
	public MobileSequenceConfig getSequenceForXml(String sequenceXml) throws SequenceException {
		try {
			initializeContext();
			Reader r = new StringReader(sequenceXml);
			return (MobileSequenceConfig)getUnmarshaller().unmarshal(r);
		} catch (JAXBException e) {
			throw new SequenceException("An error occurred reading the sequence.", e);
		}
	}

	/**
	 * <p>getSequenceForFile</p>
	 *
	 * @param sequenceFile a {@link java.io.File} object.
	 * @return a {@link org.opennms.sms.monitor.internal.config.MobileSequenceConfig} object.
	 * @throws org.opennms.sms.monitor.internal.SequenceException if any.
	 */
	public MobileSequenceConfig getSequenceForFile(File sequenceFile) throws SequenceException {
		try {
			return getSequenceForXml(FileUtils.readFileToString(sequenceFile));
		} catch (IOException e) {
			throw new SequenceException("An error occurred reading the sequence from " + sequenceFile.getPath(), e);
		}
	}
}
