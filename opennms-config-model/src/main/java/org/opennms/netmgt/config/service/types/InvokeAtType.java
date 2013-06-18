/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.config.service.types;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Class InvokeAtType.
 * 
 * @version $Revision$ $Date$
 */
public class InvokeAtType implements java.io.Serializable {
    private static final long serialVersionUID = -4284023865042615453L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * The start type
     */
    public static final int START_TYPE = 0;

    /**
     * The instance of the start type
     */
    public static final InvokeAtType START = new InvokeAtType(START_TYPE,
                                                              "start");

    /**
     * The stop type
     */
    public static final int STOP_TYPE = 1;

    /**
     * The instance of the stop type
     */
    public static final InvokeAtType STOP = new InvokeAtType(STOP_TYPE,
                                                             "stop");

    /**
     * The status type
     */
    public static final int STATUS_TYPE = 2;

    /**
     * The instance of the status type
     */
    public static final InvokeAtType STATUS = new InvokeAtType(STATUS_TYPE,
                                                               "status");

    /**
     * Field _memberTable.
     */
    private static Hashtable<Object, Object> _memberTable = init();

    /**
     * Field type.
     */
    private final int type;

    /**
     * Field stringValue.
     */
    private String stringValue = null;

    // ----------------/
    // - Constructors -/
    // ----------------/

    private InvokeAtType(final int type, final String value) {
        super();
        this.type = type;
        this.stringValue = value;
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Method enumerate.Returns an enumeration of all possible instances of
     * InvokeAtType
     * 
     * @return an Enumeration over all possible instances of InvokeAtType
     */
    public static Enumeration<Object> enumerate() {
        return _memberTable.elements();
    }

    /**
     * Method getType.Returns the type of this InvokeAtType
     * 
     * @return the type of this InvokeAtType
     */
    public int getType() {
        return this.type;
    }

    /**
     * Method init.
     * 
     * @return the initialized Hashtable for the member table
     */
    private static Hashtable<Object, Object> init() {
        Hashtable<Object, Object> members = new Hashtable<Object, Object>();
        members.put("start", START);
        members.put("stop", STOP);
        members.put("status", STATUS);
        return members;
    }

    /**
     * Method readResolve. will be called during deserialization to replace
     * the deserialized object with the correct constant instance.
     * 
     * @return this deserialized object
     */
    private Object readResolve() {
        return valueOf(this.stringValue);
    }

    /**
     * Method toString.Returns the String representation of this InvokeAtType
     * 
     * @return the String representation of this InvokeAtType
     */
    public String toString() {
        return this.stringValue;
    }

    /**
     * Method valueOf.Returns a new InvokeAtType based on the given String
     * value.
     * 
     * @param string
     * @return the InvokeAtType value of parameter 'string'
     */
    public static InvokeAtType valueOf(final String string) {
        Object obj = null;
        if (string != null) {
            obj = _memberTable.get(string);
        }
        if (obj == null) {
            String err = "" + string + " is not a valid InvokeAtType";
            throw new IllegalArgumentException(err);
        }
        return (InvokeAtType) obj;
    }

}
