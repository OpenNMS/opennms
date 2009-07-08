/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.protocols.snmp.asn1;

/**
 * The AsnDecodingException is generated whenever an error occurs in ASN.1
 * decoding of data types. The errors are generally buffer underflow errors
 * (insufficent data). Other errors include malformed ASN.1 Types.
 * 
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version 1.1.1.1
 */
public class AsnDecodingException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -617170785194301839L;

    /**
     * The default exception constructor
     */
    public AsnDecodingException() {
        super();
    }

    /**
     * The exception constructor
     * 
     * @param why
     *            The reason the exception is being raised
     * 
     */
    public AsnDecodingException(String why) {
        super(why);
    }
}
