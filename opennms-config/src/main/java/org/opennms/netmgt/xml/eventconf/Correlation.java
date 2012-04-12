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

package org.opennms.netmgt.xml.eventconf;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * The event correlation information
 */
@XmlRootElement(name="correlation")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
public class Correlation implements Serializable {
	private static final long serialVersionUID = -6184862237793330801L;

	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	/**
     * The state determines if event is correlated
     */
	// @Pattern(regexp="(on|off)")
	@XmlAttribute(name="state")
    private String m_state;

    /**
     * The correlation path
     */
	// @Pattern(regexp="(suppressDuplicates|cancellingEvent|suppressAndCancel|pathOutage")
	@XmlAttribute(name="path")
    private String m_path;

    /**
     * A canceling UEI for this event
     */
	// @Size(min=0)
	@XmlElement(name="cuei")
    private List<String> m_cueis = new ArrayList<String>();

    /**
     * The minimum count for this event
     */
	@XmlElement(name="cmin")
    private String m_cmin;

    /**
     * The maximum count for this event
     */
	@XmlElement(name="cmax")
    private String m_cmax;

    /**
     * The correlation time for this event
     */
	@XmlElement(name="ctime")
    private String m_ctime;

    public void addCuei(final String cuei) throws IndexOutOfBoundsException {
        m_cueis.add(cuei.intern());
    }

    public void addCuei(final int index, final String cuei) throws IndexOutOfBoundsException {
        m_cueis.add(index, cuei.intern());
    }

    public Enumeration<String> enumerateCuei() {
        return Collections.enumeration(m_cueis);
    }

    public String getCmax() {
        return m_cmax;
    }

    public String getCmin() {
        return m_cmin;
    }

    public String getCtime() {
        return m_ctime;
    }

    public String getCuei(final int index) throws IndexOutOfBoundsException {
        return m_cueis.get(index);
    }

    public String[] getCuei() {
        return m_cueis.toArray(EMPTY_STRING_ARRAY);
    }

    public List<String> getCueiCollection() {
        return m_cueis;
    }

    public int getCueiCount() {
        return m_cueis.size();
    }

    public String getPath() {
        return m_path == null? "suppressDuplicates" : m_path; // XSD default is suppressDuplicates
    }

    public String getState() {
        return m_state == null? "off" : m_state; // XSD default is off
    }

    /**
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    public Iterator<String> iterateCuei() {
        return m_cueis.iterator();
    }

    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void removeAllCuei() {
        m_cueis.clear();
    }

    public boolean removeCuei(final String cuei) {
        return m_cueis.remove(cuei);
    }

    public String removeCueiAt(final int index) {
        return m_cueis.remove(index);
    }

    public void setCmax(final String cmax) {
        m_cmax = cmax.intern();
    }

    public void setCmin(final String cmin) {
        m_cmin = cmin.intern();
    }

    public void setCtime(final String ctime) {
        m_ctime = ctime.intern();
    }

    public void setCuei(final int index, final String cuei) throws IndexOutOfBoundsException {
        m_cueis.set(index, cuei.intern());
    }

    public void setCuei(final String[] cueis) {
        m_cueis.clear();
        for (final String cuei : cueis) {
        	m_cueis.add(cuei.intern());
        }
    }

    public void setCuei(final List<String> cueis) {
        m_cueis.clear();
        for (final String cuei : cueis) {
        	m_cueis.add(cuei.intern());
        }
    }

    public void setCueiCollection(final List<String> cueis) {
    	setCuei(cueis);
    }

    public void setPath(final String path) {
        m_path = path.intern();
    }

    public void setState(final String state) {
        m_state = state.intern();
    }

    public static Correlation unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Correlation) Unmarshaller.unmarshal(Correlation.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_cmax == null) ? 0 : m_cmax.hashCode());
		result = prime * result + ((m_cmin == null) ? 0 : m_cmin.hashCode());
		result = prime * result + ((m_ctime == null) ? 0 : m_ctime.hashCode());
		result = prime * result + ((m_cueis == null) ? 0 : m_cueis.hashCode());
		result = prime * result + ((m_path == null) ? 0 : m_path.hashCode());
		result = prime * result + ((m_state == null) ? 0 : m_state.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Correlation)) return false;
		final Correlation other = (Correlation) obj;
		if (m_cmax == null) {
			if (other.m_cmax != null) return false;
		} else if (!m_cmax.equals(other.m_cmax)) {
			return false;
		}
		if (m_cmin == null) {
			if (other.m_cmin != null) return false;
		} else if (!m_cmin.equals(other.m_cmin)) {
			return false;
		}
		if (m_ctime == null) {
			if (other.m_ctime != null) return false;
		} else if (!m_ctime.equals(other.m_ctime)) {
			return false;
		}
		if (m_cueis == null) {
			if (other.m_cueis != null) return false;
		} else if (!m_cueis.equals(other.m_cueis)) {
			return false;
		}
		if (m_path == null) {
			if (other.m_path != null) return false;
		} else if (!m_path.equals(other.m_path)) {
			return false;
		}
		if (m_state == null) {
			if (other.m_state != null) return false;
		} else if (!m_state.equals(other.m_state)) {
			return false;
		}
		return true;
	}

}
