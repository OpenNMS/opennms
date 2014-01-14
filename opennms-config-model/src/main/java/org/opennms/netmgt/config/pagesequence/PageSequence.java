/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.pagesequence;

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
 * Use this container to list the page in the order they are to be
 * accessed for monitoring
 *  or (soon) datacollection.
 */

@XmlRootElement(name="page-sequence")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("page-sequence.xsd")
public class PageSequence implements Serializable {
    private static final long serialVersionUID = 3465352139008679743L;

    private static final Page[] EMPTY_PAGE_LIST = new Page[0];
    
    /**
     * This element specifies all the possible attributes in as
     * fine grained detail as possible. All that
     *  is really required (as you can see below) is the "path"
     * attribute. From that one attribute,
     *  the IP address passed in through the ServiceMonitor and
     * ServiceCollector interface, the URL will be
     *  fully generated using the supplied defaults in this config.
     * Configure attributes these attributes to
     *  the level of detail you need to fully control the behavior.
     *  
     *  A little bit of indirection is possible here with the host
     * attribute. If the host attribute is anything
     *  other than the default, that value will be used instead of
     * the IP address passed in through the API (Interface).
     *  
     *  
     */
    @XmlElement(name="page", required=true)
    private List<Page> m_pages = new ArrayList<Page>();


    public PageSequence() {
        super();
    }


    /**
     * 
     * 
     * @param page
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPage(final Page page) throws IndexOutOfBoundsException {
        m_pages.add(page);
    }

    /**
     * 
     * 
     * @param index
     * @param page
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPage(final int index, final Page page) throws IndexOutOfBoundsException {
        m_pages.add(index, page);
    }

    /**
     * Method enumeratePage.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Page> enumeratePage() {
        return Collections.enumeration(m_pages);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;
        
        if (obj instanceof PageSequence) {
            final PageSequence temp = (PageSequence)obj;
            if (m_pages != null) {
                if (temp.m_pages == null) {
                    return false;
                } else if (!(m_pages.equals(temp.m_pages))) {
                    return false;
                }
            } else if (temp.m_pages != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Method getPage.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Page at the given inde
     */
    public Page getPage(final int index) throws IndexOutOfBoundsException {
        return m_pages.get(index);
    }

    /**
     * Method getPage.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Page[] getPage() {
        return m_pages.toArray(EMPTY_PAGE_LIST);
    }

    /**
     * Method getPageCollection.Returns a reference to 'm_pages'.
     * No type checking is performed on any modifications to the
     * Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Page> getPageCollection() {
        return new ArrayList<Page>(m_pages);
    }

    /**
     * Method getPageCount.
     * 
     * @return the size of this collection
     */
    public int getPageCount() {
        return m_pages.size();
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;
        
        if (m_pages != null) {
           result = 37 * result + m_pages.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
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

    /**
     * Method iteratePage.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Page> iteratePage() {
        return m_pages.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws IOException if an IOException occurs during
     * marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllPage() {
        m_pages.clear();
    }

    /**
     * Method removePage.
     * 
     * @param page
     * @return true if the object was removed from the collection.
     */
    public boolean removePage(final Page page) {
        return m_pages.remove(page);
    }

    /**
     * Method removePageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Page removePageAt(final int index) {
        return m_pages.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param page
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPage(final int index, final Page page) throws IndexOutOfBoundsException {
        m_pages.set(index, page);
    }

    /**
     * 
     * 
     * @param pages
     */
    public void setPage(final Page[] pages) {
        m_pages.clear();
        for (final Page page : pages) {
            m_pages.add(page);
        }
    }

    /**
     * Sets the value of 'm_pages' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param pages the Vector to copy.
     */
    public void setPage(final List<Page> pages) {
        if (pages != m_pages) {
            m_pages.clear();
            m_pages.addAll(pages);
        }
    }

    /**
     * Sets the value of 'm_pages' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param pages the Vector to set.
     */
    public void setPageCollection(final List<Page> pages) {
        m_pages = new ArrayList<Page>(pages);
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * PageSequence
     */
    public static PageSequence unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (PageSequence) Unmarshaller.unmarshal(PageSequence.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

    @Override
    public String toString() {
        return "PageSequence[pages=" + m_pages + "]";
    }
}
