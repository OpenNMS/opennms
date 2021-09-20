/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.snmp.asn1;

/**
 * The AsnEncodingException is generated whenever an error occurs in ASN.1
 * encoding of data types. The errors are generally buffer overflow errors.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
 */
public class AsnEncodingException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 8151144874153521980L;

    /**
     * The default exception constructor
     */
    public AsnEncodingException() {
        super();
    }

    /**
     * The exception constructor
     * 
     * @param why
     *            The reason the exception is being raised
     * 
     */
    public AsnEncodingException(String why) {
        super(why);
    }
}
