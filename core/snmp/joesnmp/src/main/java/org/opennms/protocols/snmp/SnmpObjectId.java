/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.snmp;

import java.io.Serializable;

import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * Defines the SNMP object identifier class for naming variables. An object
 * identifier is a sequence of numbers that correspond to branches in the
 * Management Information Base (MIB). Each vendor is free to define their own
 * branch of the tree. The SnmpObjectId class provides an interface for naming
 * those tree instances.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <A HREF="mailto:naz@personalgenie.com">Nazario Irizarry, Jr. </A>
 * 
 */
public class SnmpObjectId extends Object implements SnmpSyntax, Cloneable, Serializable {
    /**
     * Deifnes the version of the serialization format.
     * 
     */
    static final long serialVersionUID = 2633631219460364065L;

    /**
     * The array of object identifiers minimum length for a valid object id is 2
     * (.0.0)
     */
    private int[] m_data;

    /**
     * Converts a textual object identifier to an array of integer values.
     * 
     * @param idstr
     *            An object identifier string
     * 
     * @return Returns an array of integers converted from the string. If an
     *         error occurs then a null is returned.
     */
    private static int[] convert(String idstr) {
        //
        // ids is the counter
        // idArray is the array of characters
        //
        int numIds = 0;
        char[] idArray = idstr.toCharArray();

        //
        // if the length is equal to zero then
        // the number of ids is equal to zero :)
        //
        if (idArray.length == 0) {
            int[] tmp = new int[2];
            tmp[0] = tmp[1] = 0;
            return tmp;
        }

        //
        // if the object string does not start
        // with a dot then we need to increment
        // the id count
        //
        if (idArray[0] != '.')
            numIds++;

        //
        // count the number of objects
        //
        int x = 0;
        while (x < idArray.length) {
            if (idArray[x++] == '.')
                ++numIds;
        }

        //
        // check for bad strings
        //
        if (numIds == 0) {
            int[] tmp = new int[2];
            tmp[0] = tmp[1] = 0;
            return tmp;
        }

        //
        // get an array to store objects into
        //
        int[] objects = new int[numIds];
        int objectsNdx = 0; // reset the ids counter
        int idArrayNdx = 0; // set the objects ndx counter

        //
        // if the string begins with a dot(.) then
        // increment the ndx
        //
        if (idArray[0] == '.')
            ++idArrayNdx;

        //
        // create an object id variable and
        // set it equal to zero
        //
        int oid = 0;
        while (idArrayNdx < idArray.length) {
            //
            // if there is a dot(.) then
            // store the id
            //
            if (idArray[idArrayNdx] == '.') {
                objects[objectsNdx++] = oid;
                oid = 0;
            } else {
                //
                // multiply the object id by 10
                //
                oid *= 10;
                switch (idArray[idArrayNdx]) {
                case '1':
                    oid += 1;
                    break;
                case '2':
                    oid += 2;
                    break;
                case '3':
                    oid += 3;
                    break;
                case '4':
                    oid += 4;
                    break;
                case '5':
                    oid += 5;
                    break;
                case '6':
                    oid += 6;
                    break;
                case '7':
                    oid += 7;
                    break;
                case '8':
                    oid += 8;
                    break;
                case '9':
                    oid += 9;
                    break;
                }
            }
            ++idArrayNdx;
        }

        //
        // save the last object id
        //
        objects[objectsNdx++] = oid;
        return objects;
    }

    /**
     * Defines the SNMP SMI type for this particular object.
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_OBJECTID;

    /**
     * Creates a default empty object identifier.
     * 
     */
    public SnmpObjectId() {
        m_data = new int[2];
        m_data[0] = m_data[1] = 0;
    }

    /**
     * Creates an object identifier from the passed array of identifiers. If the
     * passed argument is null then a default object id (.0.0) is created for
     * the instance.
     * 
     * @param data
     *            The array of object identifiers
     * 
     */
    public SnmpObjectId(int[] data) {
        this();
        if (data != null) {
            m_data = new int[data.length];
            System.arraycopy(data, 0, m_data, 0, data.length);
        }
    }

    /**
     * Creates a duplicate object. The passed object identifier is copied into
     * the newly created object.
     * 
     * @param second
     *            The object to copy
     * 
     */
    public SnmpObjectId(SnmpObjectId second) {
        this(second.m_data);
    }

    /**
     * Creates an object identifier from the pased dotted decimal object
     * identifier string. The string is converted to the internal
     * representation. If the conversion fails then a default (.0.0) object
     * identifier is assigned to the object.
     * 
     * @param strOid
     *            The dotted decimal object identifier string
     * 
     */
    public SnmpObjectId(String strOid) {
        m_data = convert(strOid);
        if (m_data == null) {
            m_data = new int[2];
            m_data[0] = m_data[1] = 0;
        }
    }

    /**
     * Gets the number of object identifiers in the object.
     * 
     * @return Returns the number of object identifiers
     * 
     */
    public int getLength() {
        return m_data.length;
    }
    
    /**
     * Returns the value of the last object identifier component value
     */
    public int getLastIdentifier() {
        return m_data[m_data.length-1];
    }

    /**
     * Gets the array of object identifiers from the object. The instance is
     * returned as a reference. The caller should not make any modifications to
     * the returned list.
     * 
     * @return Returns the list of identifiers
     */
    public int[] getIdentifiers() {
        return m_data;
    }

    /**
     * Sets the object to the passed object identifier
     * 
     * @param data
     *            The new object identifier
     * 
     */
    public void setIdentifiers(int[] data) {
        if (data != null) {
            m_data = new int[data.length];
            System.arraycopy(data, 0, m_data, 0, data.length);
        } else {
            m_data = new int[2];
            m_data[0] = m_data[1] = 0;
        }
    }

    /**
     * Sets the object to the passed dotted decimal object identifier string.
     * 
     * @param strOid
     *            The dotted decimal object identifier.
     * 
     */
    public void setIdentifiers(String strOid) {
        m_data = null;
        if (strOid != null) {
            m_data = convert(strOid);
        }
        if (m_data == null) {
            m_data = new int[2];
            m_data[0] = m_data[1] = 0;
        }
    }

    /**
     * Appends the specified identifiers to the current object.
     * 
     * @param ids
     *            The array of identifiers to append
     * 
     */
    public void append(int[] ids) {
        if (ids != null && ids.length != 0) {
            int[] tmp = new int[m_data.length + ids.length];

            System.arraycopy(m_data, 0, tmp, 0, m_data.length);

            System.arraycopy(ids, 0, tmp, m_data.length, ids.length);

            m_data = tmp;
        }
    }

    /**
     * Converts the passed string to an object identifier and appends them to
     * the current object.
     * 
     * @param strOids
     *            The dotted decimal identifiers to append
     * 
     */
    public void append(String strOids) {
        int[] tmp = convert(strOids);
        append(tmp);
    }

    /**
     * Appends the passed SnmpObjectId object to self.
     * 
     * @param second
     *            The object to append to self
     * 
     */
    public void append(SnmpObjectId second) {
        append(second.m_data);
    }

    /**
     * Prepends the passed set of identifiers to the front of the object.
     * 
     * @param ids
     *            The list of identifiers
     * 
     */
    public void prepend(int[] ids) {
        if (ids != null && ids.length != 0) {
            int[] tmp = new int[m_data.length + ids.length];

            System.arraycopy(ids, 0, tmp, 0, ids.length);

            System.arraycopy(m_data, 0, tmp, ids.length, m_data.length);

            m_data = tmp;
        }
    }

    /**
     * Converts the passed string to an object identifier and prepends them to
     * the current object.
     * 
     * @param strOids
     *            The dotted decimal identifiers to prepend
     * 
     */
    public void prepend(String strOids) {
        int[] tmp = convert(strOids);
        prepend(tmp);
    }

    /**
     * Prepends the passed SnmpObjectId object to self.
     * 
     * @param second
     *            The object to prepend to self
     * 
     */
    public void prepend(SnmpObjectId second) {
        prepend(second.m_data);
    }

    /**
     * Lexigraphically compares the object identifer to the array of
     * identifiers. If the object is lexigraphically less than ids a negative
     * number is returned. A positive number is returned if self is greater than
     * the passed identifers and a zero is returned if they are equal. The
     * length of the identifiers do not have to be equal.
     * 
     * @param ids
     *            The array if identifier to compare
     * 
     * @return Returns zero if the ids are equal. Less than zero if the object
     *         is less than 'ids' and greater than zero if the object is greater
     *         than 'ids'.
     * 
     */
    public int compare(int[] ids) {
        //
        // compare A(self) to
        // B(ids) irrelivant of length
        //
        int a, aNdx = 0;
        int b, bNdx = 0;
        int rc = 0;

        //
        // loop so long as either array index
        // has not reached the end of it's array
        //
        // This solves the problem of one object id
        // being longer than another
        //
        while (aNdx < m_data.length || bNdx < ids.length) {
            //
            // get the id from self. If the end of
            // self has been reached then it will
            // be equal to zero. If not then the
            // id will be offset by 1
            //
            a = 0;
            if (aNdx < m_data.length)
                a = m_data[aNdx++] + 1;

            //
            // Just like 'a', only for the array ids.
            //
            b = 0;
            if (bNdx < ids.length)
                b = ids[bNdx++] + 1;

            //
            // compare
            //
            rc = a - b;
            if (rc != 0)
                break;
        }
        return rc;
    }

    /**
     * Lexigraphically compares the object identifer to the array of
     * identifiers. If the object is lexigraphically less than ids a negative
     * number is returned. A positive number is returned if self is greater than
     * the passed identifers and a zero is returned if they are equal. The
     * length of the identifiers do not have to be equal.
     * 
     * @param ids
     *            The array if identifier to compare.
     * @param dist
     *            The maximum number of ids to compare.
     * 
     * @return Returns zero if the ids are equal. Less than zero if the object
     *         is less than 'ids' and greater than zero if the object is greater
     *         than 'ids'.
     * 
     */
    public int compare(int[] ids, int dist) {
        //
        // compare A(self) to
        // B(ids) irrelivant of length
        //
        int a, aNdx = 0;
        int b, bNdx = 0;
        int rc = 0;

        //
        // loop so long as either array index
        // has not reached the end of it's array
        //
        // This solves the problem of one object id
        // being longer than another
        //
        while ((aNdx < m_data.length || bNdx < ids.length) && --dist >= 0) {
            //
            // get the id from self. If the end of
            // self has been reached then it will
            // be equal to zero. If not then the
            // id will be offset by 1
            //
            a = 0;
            if (aNdx < m_data.length)
                a = m_data[aNdx++] + 1;

            //
            // Just like 'a', only for the array ids.
            //
            b = 0;
            if (bNdx < ids.length)
                b = ids[bNdx++] + 1;

            //
            // compare
            //
            rc = a - b;
            if (rc != 0)
                break;
        }
        return rc;
    }

    /**
     * Lexigraphically compares the object identifer to the passed object
     * identifer. If the object is lexigraphically less than 'cmp' a negative
     * number is returned. A positive number is returned if self is greater than
     * the passed identifer and a zero is returned if they are equal. The length
     * of the identifiers do not have to be equal.
     * 
     * @param cmp
     *            The object identifier to compare
     * 
     * @return Returns zero if the ids are equal. Less than zero if the object
     *         is less than 'cmp' and greater than zero if the object is greater
     *         than 'cmp'.
     * 
     */
    public int compare(SnmpObjectId cmp) {
        return compare(cmp.m_data);
    }

    /**
     * <P>
     * Compares the passed object identifier against self to determine if self
     * is the root of the passed object. If the passed object is in the same
     * root tree as self then a true value is returned. Otherwise a false value
     * is returned from the object.
     * </P>
     * 
     * @param leaf
     *            The object to be tested
     * 
     * @return True if leaf is in the tree.
     */
    public boolean isRootOf(SnmpObjectId leaf) {
        return (compare(leaf.m_data, m_data.length) == 0);
    }

    /**
     * Test for equality. Returns true if 'o' is an instance of an SnmpObjectId
     * and is equal to self.
     * 
     * @param o
     *            The object to be tested for equality.
     * 
     * @return True if the object is an SnmpObjectId and is equal to self. False
     *         otherwise.
     * 
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof SnmpObjectId) {
            return (compare(((SnmpObjectId) o).m_data) == 0);
        } else if (o instanceof String) {
            return (compare(convert((String) o)) == 0);
        } else if (o instanceof int[]) {
            return (compare((int[]) o) == 0);
        }
        return false;
    }

    /**
     * Converts the object identifier to a dotted decimal string representation.
     * 
     * @return Returns the dotted decimal object id string.
     * 
     */
    @Override
    public String toString() {
        //
        // assume two digit ids, plus one dot(.) per id.
        //
        StringBuffer buf = new StringBuffer();
        buf.ensureCapacity(m_data.length * 3);

        for (int x = 0; x < m_data.length; x++) {
            buf.append('.');
            if (m_data[x] >= 0) {
                buf.append(m_data[x]); // Same as String.valueOf(int)
            } else {
                long oid = (long) m_data[x] & 0xffffffffL;
                buf.append(oid); // Same as String.valueOf(long)
            }
        }

        return buf.toString();
    }

    /**
     * Returns a computed hash code value for the object identifier. If the
     * value of the object identifier is changed through any method the hash
     * value for the identifer will change. Care must be exercised by developers
     * to ensure that ObjectIds do not change when inserted into managers that
     * track object by their hash value.
     * 
     * @return The hash code for the object.
     * 
     * 
     * @since 1.8
     */
    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < m_data.length; i++) {
            hash = (hash * 31) + m_data[i];
        }
        return hash;
    }

    /**
     * Used to get the ASN.1 type for this particular object.
     */
    @Override
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Encodes the ASN.1 object identifier using the passed encoder and stores
     * the results in the passed buffer. An exception is thrown if an error
     * occurs with the encoding of the information.
     * 
     * @param buf
     *            The buffer to write the encoded information.
     * @param offset
     *            The offset to start writing information
     * @param encoder
     *            The encoder object.
     * 
     * @return The offset of the byte immediantly after the last encoded byte.
     * 
     * @exception AsnEncodingException
     *                Thrown if the encoder finds an error in the buffer.
     */
    @Override
    public int encodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnEncodingException {
        return encoder.buildObjectId(buf, offset, typeId(), m_data);
    }

    /**
     * Decodes the ASN.1 object identifer from the passed buffer. If an error
     * occurs during the decoding sequence then an AsnDecodingException is
     * thrown by the method. The value is decoded using the AsnEncoder passed to
     * the object.
     * 
     * @param buf
     *            The encode buffer
     * @param offset
     *            The offset byte to begin decoding
     * @param encoder
     *            The decoder object.
     * 
     * @return The index of the byte immediantly after the last decoded byte of
     *         information.
     * 
     * @exception AsnDecodingException
     *                Thrown by the encoder if an error occurs trying to decode
     *                the data buffer.
     */
    @Override
    public int decodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnDecodingException {
        Object[] rVals = encoder.parseObjectId(buf, offset);

        if (((Byte) rVals[1]).byteValue() != typeId())
            throw new AsnDecodingException("Invalid ASN.1 type");

        m_data = (int[]) rVals[2];

        return ((Integer) rVals[0]).intValue();
    }

    /**
     * Serves the same purpose as the method clone().
     * 
     * @return A new copy of self.
     * 
     */
    @Override
    public SnmpSyntax duplicate() {
        return new SnmpObjectId(this);
    }

    //
    // override clone() interface to support
    // Cloneable interface
    //

    /**
     * Implements the cloneable interface.
     * 
     * @return Returns a new SnmpObjectId copy of self.
     * 
     */
    @Override
    public Object clone() {
        return new SnmpObjectId(this);
    }
}
