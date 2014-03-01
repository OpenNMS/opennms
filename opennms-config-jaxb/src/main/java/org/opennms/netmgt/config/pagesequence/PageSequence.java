/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

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
    private List<Page> m_pages = new ArrayList<Page>();

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
