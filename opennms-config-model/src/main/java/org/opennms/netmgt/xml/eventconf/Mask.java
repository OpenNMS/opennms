/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * The Mask for event configuration: The mask contains one
 *  or more 'maskelements' which uniquely identify an event.
 */
@XmlRootElement(name="mask")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_maskElements", "m_varbinds"})
public class Mask implements Serializable {
	private static final long serialVersionUID = 6447323136878359636L;
	private static final Varbind[] EMPTY_VARBIND_ARRAY = new Varbind[0];
	private static final Maskelement[] EMPTY_MASKELEMENT_ARRAY = new Maskelement[0];

	/**
     * The mask element
     */
	// @NotNull
	// @Size(min=1)
	@XmlElement(name="maskelement", required=true)
    private List<Maskelement> m_maskElements = new ArrayList<Maskelement>();

    /**
     * The varbind element
     */
	@XmlElement(name="varbind")
    private List<Varbind> m_varbinds = new ArrayList<Varbind>();


    public void addMaskelement(final Maskelement element) throws IndexOutOfBoundsException {
        m_maskElements.add(element);
    }

    public void addMaskelement(final int index, final Maskelement element) throws IndexOutOfBoundsException {
        m_maskElements.add(index, element);
    }

    public void addVarbind(final Varbind varbind) throws IndexOutOfBoundsException {
        m_varbinds.add(varbind);
    }

    public void addVarbind(final int index, final Varbind varbind) throws IndexOutOfBoundsException {
        m_varbinds.add(index, varbind);
    }

    public Enumeration<Maskelement> enumerateMaskelement() {
        return Collections.enumeration(m_maskElements);
    }

    public Enumeration<Varbind> enumerateVarbind() {
        return Collections.enumeration(m_varbinds);
    }

    public Maskelement getMaskelement(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_maskElements.size()) {
            throw new IndexOutOfBoundsException("getMaskelement: Index value '" + index + "' not in range [0.." + (m_maskElements.size() - 1) + "]");
        }
        return m_maskElements.get(index);
    }

    public Maskelement[] getMaskelement() {
        return m_maskElements.toArray(EMPTY_MASKELEMENT_ARRAY);
    }

    public List<Maskelement> getMaskelementCollection() {
        return m_maskElements;
    }

    public int getMaskelementCount() {
        return m_maskElements.size();
    }

    public Varbind getVarbind(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_varbinds.size()) {
            throw new IndexOutOfBoundsException("getVarbind: Index value '" + index + "' not in range [0.." + (m_varbinds.size() - 1) + "]");
        }
        return m_varbinds.get(index);
    }

    public Varbind[] getVarbind() {
        return m_varbinds.toArray(EMPTY_VARBIND_ARRAY);
    }

    public List<Varbind> getVarbindCollection() {
        return m_varbinds;
    }

    public int getVarbindCount() {
        return m_varbinds.size();
    }

    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    public Iterator<Maskelement> iterateMaskelement() {
        return m_maskElements.iterator();
    }

    public Iterator<Varbind> iterateVarbind() {
        return m_varbinds.iterator();
    }

    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void removeAllMaskelement() {
        m_maskElements.clear();
    }

    public void removeAllVarbind() {
        m_varbinds.clear();
    }

    public boolean removeMaskelement(final Maskelement element) {
        return m_maskElements.remove(element);
    }

    public Maskelement removeMaskelementAt(final int index) {
        return m_maskElements.remove(index);
    }

    public boolean removeVarbind(final Varbind varbind) {
        return m_varbinds.remove(varbind);
    }

    public Varbind removeVarbindAt(final int index) {
        return m_varbinds.remove(index);
    }

    public void setMaskelement(final int index, final Maskelement element) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_maskElements.size()) {
            throw new IndexOutOfBoundsException("setMaskelement: Index value '" + index + "' not in range [0.." + (m_maskElements.size() - 1) + "]");
        }
        m_maskElements.set(index, element);
    }

    public void setMaskelement(final Maskelement[] elements) {
        m_maskElements.clear();
        for (final Maskelement element : elements) {
        	m_maskElements.add(element);
        }
    }

    public void setMaskelement(final List<Maskelement> elements) {
        if (m_maskElements == elements) return;
        m_maskElements.clear();
        m_maskElements.addAll(elements);
    }

    public void setMaskelementCollection(final List<Maskelement> elements) {
        setMaskelement(elements);
    }

    public void setVarbind(final int index, final Varbind varbind) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_varbinds.size()) {
            throw new IndexOutOfBoundsException("setVarbind: Index value '" + index + "' not in range [0.." + (m_varbinds.size() - 1) + "]");
        }
        m_varbinds.set(index, varbind);
    }

    public void setVarbind(final Varbind[] varbinds) {
        m_varbinds.clear();
        for (final Varbind varbind : varbinds) {
        	m_varbinds.add(varbind);
        }
    }

    public void setVarbind(final List<Varbind> varbinds) {
        if (m_varbinds == varbinds) return;
        m_varbinds.clear();
        m_varbinds.addAll(varbinds);
    }

    public void setVarbindCollection(final List<Varbind> varbinds) {
        setVarbind(varbinds);
    }

    public static Mask unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Mask) Unmarshaller.unmarshal(Mask.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_maskElements == null) ? 0 : m_maskElements.hashCode());
		result = prime * result + ((m_varbinds == null) ? 0 : m_varbinds.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Mask)) return false;
		final Mask other = (Mask) obj;
		if (m_maskElements == null) {
			if (other.m_maskElements != null) return false;
		} else if (!m_maskElements.equals(other.m_maskElements)) {
			return false;
		}
		if (m_varbinds == null) {
			if (other.m_varbinds != null) return false;
		} else if (!m_varbinds.equals(other.m_varbinds)) {
			return false;
		}
		return true;
	}
	
	public EventMatcher constructMatcher() {
		EventMatcher[] matchers = new EventMatcher[getMaskelementCount()+getVarbindCount()];
		int index = 0;
		for(Maskelement maskElement : m_maskElements) {
			matchers[index] = maskElement.constructMatcher();
			index++;
 		}
		
		for(Varbind varbind : m_varbinds) {
			matchers[index] = varbind.constructMatcher();
			index++;
		}
		
		return EventMatchers.and(matchers);
	}
	
	public Maskelement getMaskElement(String mename) {
		for(Maskelement element : m_maskElements) {
			if (mename.equals(element.getMename())) {
				return element;
			}
		}
		return null;
	}

	public List<String> getMaskElementValues(String mename) {
		Maskelement element = getMaskElement(mename);
		return element == null ? null : element.getMevalueCollection();
	}
	

}
