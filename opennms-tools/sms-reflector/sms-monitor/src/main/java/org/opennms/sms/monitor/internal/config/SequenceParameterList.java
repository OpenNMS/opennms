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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>SequenceParameterList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="parameters")
public class SequenceParameterList {
	@XmlElement(name="parameter")
	private List<SequenceParameter> m_parameters = Collections.synchronizedList(new ArrayList<SequenceParameter>());

	/**
	 * <p>addParameter</p>
	 *
	 * @param parameter a {@link org.opennms.sms.monitor.internal.config.SequenceParameter} object.
	 */
	public void addParameter(SequenceParameter parameter) {
		m_parameters.add(parameter);
	}

	/**
	 * <p>getParameters</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<SequenceParameter> getParameters() {
		return m_parameters;
	}
	
	/**
	 * <p>setParameters</p>
	 *
	 * @param parameters a {@link java.util.List} object.
	 */
	public void setParameters(List<SequenceParameter> parameters) {
		m_parameters = parameters;
	}
}
