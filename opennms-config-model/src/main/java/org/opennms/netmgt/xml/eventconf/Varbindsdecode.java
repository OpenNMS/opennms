/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * This element is used for converting event 
 *  varbind value in static decoded string.
 */
@XmlRootElement(name="varbindsdecode")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_parmid", "m_decodes"})
public class Varbindsdecode implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The identifier of the parameters to be decoded
     */
    @XmlElement(name="parmid", required=true)
    private String m_parmid;

    /**
     * The value to string decoding map
     */
    @XmlElement(name="decode", required=true)
    private List<Decode> m_decodes = new ArrayList<>();

    public String getParmid() {
        return m_parmid;
    }

    public void setParmid(final String parmid) {
        m_parmid = ConfigUtils.assertNotEmpty(parmid, "parmid").intern();
    }

    public List<Decode> getDecodes() {
        return m_decodes;
    }

    public void setDecodes(final List<Decode> decodes) {
        ConfigUtils.assertMinimumSize(decodes, 1, "decode");
        if (m_decodes == decodes) return;
        m_decodes.clear();
        if (decodes != null) m_decodes.addAll(decodes);
    }

    public void addDecode(final Decode decode) {
        m_decodes.add(decode);
    }

    public boolean removeDecode(final Decode decode) {
        return m_decodes.remove(decode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_parmid, m_decodes);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Varbindsdecode) {
            final Varbindsdecode that = (Varbindsdecode) obj;
            return Objects.equals(this.m_parmid, that.m_parmid) &&
                    Objects.equals(this.m_decodes, that.m_decodes);
        }
        return false;
    }

}
