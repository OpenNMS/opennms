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

package org.opennms.sms.monitor.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.sms.monitor.session.SessionVariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SequenceSessionVariable class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="SequenceSessionVariableType", propOrder={"m_name", "m_className", "m_parameters"})
@XmlRootElement(name="session-variable")
public class SequenceSessionVariable {
    private static final Logger LOG = LoggerFactory.getLogger(SequenceSessionVariable.class);
	@XmlAttribute(name="name")
	private String m_name;

	@XmlAttribute(name="class")
	private String m_className;

	@XmlElementWrapper(name="parameters", required=false)
	@XmlElement(name="parameter")
	private List<SequenceParameter> m_parameters;

	@XmlTransient
    private SessionVariableGenerator m_generator;
	
	/**
	 * <p>Constructor for SequenceSessionVariable.</p>
	 */
	public SequenceSessionVariable() {
	}

	/**
	 * <p>Constructor for SequenceSessionVariable.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param className a {@link java.lang.String} object.
	 */
	public SequenceSessionVariable(String name, String className) {
		setName(name);
		setClassName(className);
	}
	
	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_name;
	}
	/**
	 * <p>setName</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * <p>getClassName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName() {
		return m_className;
	}
	/**
	 * <p>setClassName</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setClassName(String name) {
		m_className = name;
	}

	/**
	 * <p>addParameter</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 */
	public void addParameter(String key, String value) {
		if (m_parameters == null) {
			m_parameters = Collections.synchronizedList(new ArrayList<SequenceParameter>());
		}
		m_parameters.add(new SequenceParameter(key, value));
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
	 * <p>getParametersAsMap</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String,String> getParametersAsMap() {
		Map<String,String> m = new HashMap<String,String>();
		if (m_parameters != null) {
			for (SequenceParameter p : m_parameters) {
				m.put(p.getKey(), p.getValue());
			}
		}
		return m;
	}
	
	/**
	 * <p>setParameters</p>
	 *
	 * @param parameters a {@link java.util.List} object.
	 */
	public void setParameters(List<SequenceParameter> parameters) {
		m_parameters = parameters;
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("name", getName())
			.append("class", getClassName())
			.append("parameters", getParameters())
			.toString();
	}

    /**
     * <p>getGenerator</p>
     *
     * @return a {@link org.opennms.sms.monitor.session.SessionVariableGenerator} object.
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.lang.InstantiationException if any.
     * @throws java.lang.IllegalAccessException if any.
     */
    public SessionVariableGenerator getGenerator() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        
        if (m_generator == null) {
            Class<?> c = Class.forName(getClassName());
            if (SessionVariableGenerator.class.isAssignableFrom(c)) {
                SessionVariableGenerator generator = (SessionVariableGenerator)c.newInstance();
                generator.setParameters(getParametersAsMap());
                m_generator = generator;
    
            } else {
                LOG.warn("unable to get instance of session class: {}", c);
            }
        }
        return m_generator;
    }

    /**
     * <p>checkOut</p>
     *
     * @param properties a {@link java.util.Properties} object.
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.lang.InstantiationException if any.
     * @throws java.lang.IllegalAccessException if any.
     */
    public void checkOut(Properties properties) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        
        SessionVariableGenerator generator = getGenerator();
        
        if (generator != null) {
            
            String value = generator.checkOut();
            if (value == null) {
                value = "";
            }
            properties.setProperty(getName(), value);
        }
    }

    /**
     * <p>checkIn</p>
     *
     * @param properties a {@link java.util.Properties} object.
     */
    public void checkIn(Properties properties) {
        SessionVariableGenerator generator = m_generator;
        if (generator != null) {
            generator.checkIn(properties.getProperty(getName()));
        }
    }
}
