/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.pagesequence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Use this container to list the page in the order they are to be accessed
 * for monitoring or (soon) datacollection.
 */

@XmlRootElement(name="page-sequence")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("page-sequence.xsd")
public class PageSequence implements Serializable {
    private static final long serialVersionUID = -6988812331650282380L;

    /**
     * <p>
     * This element specifies all the possible attributes in as fine grained
     * detail as possible. All that is really required (as you can see below)
     * is the "path" attribute. From that one attribute, the IP address passed
     * in through the ServiceMonitor and ServiceCollector interface, the URL
     * will be fully generated using the supplied defaults in this config.
     * Configure attributes these attributes to the level of detail you need
     * to fully control the behavior.
     * </p>
     * <p>
     * A little bit of indirection is possible here with the host attribute.
     * If the host attribute is anything other than the default, that value
     * will be used instead of the IP address passed in through the API
     * (Interface).
     * </p>
     */
    @XmlElement(name="page", required=true)
    private List<Page> m_pages = new ArrayList<>();

    public PageSequence() {
        super();
    }

    public List<Page> getPages() {
        if (m_pages == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_pages);
        }
    }

    public void setPages(final List<Page> pages) {
        m_pages = new ArrayList<Page>(pages);
    }

    public void addPage(final Page page) throws IndexOutOfBoundsException {
        m_pages.add(page);
    }

    public boolean removePage(final Page page) {
        return m_pages.remove(page);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_pages == null) ? 0 : m_pages.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PageSequence)) {
            return false;
        }
        final PageSequence other = (PageSequence) obj;
        if (m_pages == null) {
            if (other.m_pages != null) {
                return false;
            }
        } else if (!m_pages.equals(other.m_pages)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PageSequence [pages=" + m_pages + "]";
    }


}
