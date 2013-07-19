/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.ackd;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Location for user to define readers and they're schedules.
 * 
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "readers")
@XmlAccessorType(XmlAccessType.FIELD)
public class Readers implements Serializable {
    private static final long serialVersionUID = 7999107140580085865L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _readerList.
     */
    @XmlElement(name = "reader")
    private List<Reader> _readerList = new ArrayList<Reader>(0);

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Readers() {
        super();
    }

    public Readers(final Reader reader) {
        super();
        addReader(reader);
    }

    public Readers(final List<Reader> readers) {
        super();
        setReader(readers);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * 
     * 
     * @param vReader
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addReader(final Reader vReader)
            throws IndexOutOfBoundsException {
        this._readerList.add(vReader);
    }

    /**
     * 
     * 
     * @param index
     * @param vReader
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addReader(final int index, final Reader vReader)
            throws IndexOutOfBoundsException {
        this._readerList.add(index, vReader);
    }

    /**
     * Method enumerateReader.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Reader> enumerateReader() {
        return Collections.enumeration(this._readerList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Readers) {

            Readers temp = (Readers) obj;
            if (this._readerList != null) {
                if (temp._readerList == null)
                    return false;
                else if (!(this._readerList.equals(temp._readerList)))
                    return false;
            } else if (temp._readerList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getReader.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.ackd.Reader at the
     *         given index
     */
    public Reader getReader(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._readerList.size()) {
            throw new IndexOutOfBoundsException("getReader: Index value '"
                    + index + "' not in range [0.."
                    + (this._readerList.size() - 1) + "]");
        }

        return (Reader) _readerList.get(index);
    }

    /**
     * Method getReader.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public Reader[] getReader() {
        Reader[] array = new Reader[0];
        return (Reader[]) this._readerList.toArray(array);
    }

    /**
     * Method getReaderCollection.Returns a reference to '_readerList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Reader> getReaderCollection() {
        return this._readerList;
    }

    /**
     * Method getReaderCount.
     * 
     * @return the size of this collection
     */
    public int getReaderCount() {
        return this._readerList.size();
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        if (_readerList != null) {
            result = 37 * result + _readerList.hashCode();
        }

        return result;
    }

    /**
     * Method iterateReader.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Reader> iterateReader() {
        return this._readerList.iterator();
    }

    /**
     */
    public void removeAllReader() {
        this._readerList.clear();
    }

    /**
     * Method removeReader.
     * 
     * @param vReader
     * @return true if the object was removed from the collection.
     */
    public boolean removeReader(final Reader vReader) {
        return _readerList.remove(vReader);
    }

    /**
     * Method removeReaderAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Reader removeReaderAt(final int index) {
        return this._readerList.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param vReader
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setReader(final int index, final Reader vReader)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._readerList.size()) {
            throw new IndexOutOfBoundsException("setReader: Index value '"
                    + index + "' not in range [0.."
                    + (this._readerList.size() - 1) + "]");
        }

        this._readerList.set(index, vReader);
    }

    /**
     * 
     * 
     * @param vReaderArray
     */
    public void setReader(final Reader[] vReaderArray) {
        // -- copy array
        _readerList.clear();

        for (int i = 0; i < vReaderArray.length; i++) {
            this._readerList.add(vReaderArray[i]);
        }
    }

    /**
     * Sets the value of '_readerList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vReaderList
     *            the Vector to copy.
     */
    public void setReader(final List<Reader> vReaderList) {
        // copy vector
        this._readerList.clear();

        this._readerList.addAll(vReaderList);
    }
}
