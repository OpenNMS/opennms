// 
//    JoeSNMP - SNMPv1 & v2 Compliant Libraries for Java
//    Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//   
// For more information contact: 
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
//
// Tab Size = 8
//
// AsnEncodingException.java,v 1.1.1.1 2001/11/11 17:27:23 ben Exp
//
package org.opennms.protocols.snmp.asn1;

import java.lang.Exception;

/**
 * The AsnEncodingException is generated whenever an error
 * occurs in ASN.1 encoding of data types. The errors are
 * generally buffer overflow errors.
 *
 * @author	<a href="http://www.opennms.org>OpenNMS</a>
 * @author	<a href="mailto:weave@opennms.org">Brian Weaver</a>
 * @version	1.1.1.1
 *
 */
public class AsnEncodingException extends Exception
{
	/**
	 * The default exception constructor
	 */
	public AsnEncodingException( )
	{
		super();
	}

	/**
	 * The exception constructor
	 *
	 * @param why The reason the exception is being raised
	 *
	 */
	public AsnEncodingException(String why)
	{
		super(why);
	}
}
