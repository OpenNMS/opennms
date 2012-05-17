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
 * The filters for the event, contains one or more filter tags.
 */

@XmlRootElement(name="filters")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
public class Filters implements Serializable {
	private static final long serialVersionUID = 3672883849182860671L;
	private static final Filter[] EMPTY_FILTER_ARRAY = new Filter[0];

	/**
     * The mask element
     */
	// @NotNull
	// @Size(min=1)
    @XmlElement(name="filter", required=true)
    private List<Filter> m_filters = new ArrayList<Filter>();

    public void addFilter(final Filter filter) throws IndexOutOfBoundsException {
        m_filters.add(filter);
    }

    public void addFilter(final int index, final Filter filter) throws IndexOutOfBoundsException {
        m_filters.add(index, filter);
    }

    public Enumeration<Filter> enumerateFilter() {
        return Collections.enumeration(m_filters);
    }

    public Filter getFilter(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_filters.size()) {
            throw new IndexOutOfBoundsException("getFilter: Index value '" + index + "' not in range [0.." + (m_filters.size() - 1) + "]");
        }
        return m_filters.get(index);
    }

    public Filter[] getFilter() {
        return m_filters.toArray(EMPTY_FILTER_ARRAY);
    }

    public List<Filter> getFilterCollection() {
        return m_filters;
    }

    public int getFilterCount() {
        return m_filters.size();
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

    public Iterator<Filter> iterateFilter() {
        return m_filters.iterator();
    }

    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void removeAllFilter() {
        m_filters.clear();
    }

    public boolean removeFilter(final Filter filter) {
        return m_filters.remove(filter);
    }

    public Filter removeFilterAt(final int index) {
        return m_filters.remove(index);
    }

    public void setFilter(final int index, final Filter filter) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_filters.size()) {
            throw new IndexOutOfBoundsException("setFilter: Index value '" + index + "' not in range [0.." + (m_filters.size() - 1) + "]");
        }
        m_filters.set(index, filter);
    }

    public void setFilter(final Filter[] filters) {
        m_filters.clear();
        for (final Filter filter : filters) {
        	m_filters.add(filter);
        }
    }

    public void setFilter(final List<Filter> filters) {
        m_filters.clear();
        m_filters.addAll(filters);
    }

    public void setFilterCollection(final List<Filter> filters) {
        m_filters = filters;
    }

    public static Filters unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Filters) Unmarshaller.unmarshal(Filters.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_filters == null) ? 0 : m_filters.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Filters)) return false;
		final Filters other = (Filters) obj;
		if (m_filters == null) {
			if (other.m_filters != null) return false;
		} else if (!m_filters.equals(other.m_filters)) {
			return false;
		}
		return true;
	}

}
