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
 * The SNMP information from the trap
 */
@XmlRootElement(name="snmp")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_id", "m_idText", "m_version", "m_specific", "m_generic", "m_community"})
public class Snmp implements Serializable {
	private static final long serialVersionUID = 7180451834403181827L;

	/**
     * The SNMP enterprise ID
     */
	// @NotNull
	@XmlElement(name="id", required=true)
    private String m_id;

    /**
     * The SNMP enterprise ID text
     */
	@XmlElement(name="idtext", required=false)
    private String m_idText;

    /**
     * The SNMP version
     */
	// @NotNull
	@XmlElement(name="version", required=true)
    private String m_version;

    /**
     * The specific trap number
     */
	@XmlElement(name="specific", required=false)
    private Integer m_specific;

    /**
     * The generic trap number
     */
	@XmlElement(name="generic", required=false)
    private Integer m_generic;

    /**
     * The community name
     */
	@XmlElement(name="community", required=false)
    private String m_community;

    public void deleteGeneric() {
        m_generic = null;
    }

    public void deleteSpecific() {
        m_specific = null;
    }

    public String getCommunity() {
        return m_community;
    }

    /** The generic trap number. */
    public Integer getGeneric(
    ) {
        return m_generic;
    }

    /** The SNMP enterprise ID */
    public String getId() {
        return m_id;
    }

    /** The SNMP enterprise ID text */
    public String getIdtext() {
        return m_idText;
    }

    /** The specific trap number */
    public Integer getSpecific() {
        return m_specific;
    }

    /** The SNMP version */
    public String getVersion() {
        return m_version;
    }

    /**
     * @return true if at least one Generic has been added
     */
    public boolean hasGeneric() {
        return m_generic != null;
    }

    /**
     * @return true if at least one Specific has been added
     */
    public boolean hasSpecific() {
        return m_specific != null;
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

    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void setCommunity(final String community) {
        m_community = community;
    }

    public void setGeneric(final int generic) {
        m_generic = generic;
    }

    public void setId(final String id) {
        m_id = id;
    }

    public void setIdtext(final String idText) {
        m_idText = idText;
    }

    public void setSpecific(final int specific) {
        m_specific = specific;
    }

    public void setVersion(final String version) {
        m_version = version;
    }

    public static Snmp unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Snmp) Unmarshaller.unmarshal(Snmp.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_community == null) ? 0 : m_community.hashCode());
		result = prime * result + ((m_generic == null) ? 0 : m_generic.hashCode());
		result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
		result = prime * result + ((m_idText == null) ? 0 : m_idText.hashCode());
		result = prime * result + ((m_specific == null) ? 0 : m_specific.hashCode());
		result = prime * result + ((m_version == null) ? 0 : m_version.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Snmp)) return false;
		final Snmp other = (Snmp) obj;
		if (m_community == null) {
			if (other.m_community != null) return false;
		} else if (!m_community.equals(other.m_community)) {
			return false;
		}
		if (m_generic == null) {
			if (other.m_generic != null) return false;
		} else if (!m_generic.equals(other.m_generic)) {
			return false;
		}
		if (m_id == null) {
			if (other.m_id != null) return false;
		} else if (!m_id.equals(other.m_id)) {
			return false;
		}
		if (m_idText == null) {
			if (other.m_idText != null) return false;
		} else if (!m_idText.equals(other.m_idText)) {
			return false;
		}
		if (m_specific == null) {
			if (other.m_specific != null) return false;
		} else if (!m_specific.equals(other.m_specific)) {
			return false;
		}
		if (m_version == null) {
			if (other.m_version != null) return false;
		} else if (!m_version.equals(other.m_version)) {
			return false;
		}
		return true;
	}

}
