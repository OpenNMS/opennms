//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//
//
// Tab Size = 8
//
// EventConstants.java,v 1.1.1.1 2001/11/11 17:34:38 ben Exp
//

package org.opennms.netmgt.trapd;

import java.math.BigInteger;
import org.opennms.core.utils.Base64;
import org.opennms.protocols.snmp.*;


public class EventConstants extends Object
{

	public final static String TYPE_STRING = "string";
	public final static String TYPE_INT = "int";
	public final static String TYPE_SNMP_OCTET_STRING = "OctetString";
	public final static String TYPE_SNMP_INT32 = "Int32";
	public final static String TYPE_SNMP_NULL = "Null";
	public final static String TYPE_SNMP_OBJECT_IDENTIFIER = "ObjectIdentifier";
	public final static String TYPE_SNMP_IPADDRESS = "IpAddress";
	public final static String TYPE_SNMP_TIMETICKS = "TimeTicks";
	public final static String TYPE_SNMP_COUNTER32 = "Counter32";
	public final static String TYPE_SNMP_GAUGE32 = "Gauge32";
	public final static String TYPE_SNMP_OPAQUE = "Opaque";
	public final static String TYPE_SNMP_SEQUENCE = "Sequence";
	public final static String TYPE_SNMP_COUNTER64 = "Counter64";


	public final static String XML_ENCODING_TEXT = "text";
	public final static String XML_ENCODING_BASE64 = "base64";


	/** Empty, private constructor so this object cannot be instantiated. */
	private EventConstants() {}
	
	/**
	 * Converts the value of the instance to a string
	 * representation in the correct encoding system.
	 *
	 */
	public static String toString( String encoding, Object value )
	{
		if( encoding == null || value == null )
		{
			throw new IllegalArgumentException( "Cannot take null parameters." );
		}
		
		String result = "";
		
		if(XML_ENCODING_TEXT.equals(encoding))
		{
			if(value instanceof String)
				result = (String)value;
			else if(value instanceof Number)
				result = value.toString();
			else if(value instanceof SnmpInt32)
				result = Integer.toString(((SnmpInt32)value).getValue());
			else if(value instanceof SnmpUInt32)
				result = Long.toString(((SnmpUInt32)value).getValue());
			else if(value instanceof SnmpCounter64)
				result = ((SnmpCounter64)value).getValue().toString();
			else if(value instanceof SnmpIPAddress)
				result = value.toString();
			else if(value instanceof SnmpOctetString)
				result = new String(((SnmpOctetString)value).getString());
			else if(value instanceof SnmpObjectId)
				result = value.toString();
		}
		else if(XML_ENCODING_BASE64.equals(encoding))
		{
			if(value instanceof String)
				result = new String(Base64.encodeBase64(((String)value).getBytes()));
			else if(value instanceof Number)
			{
				byte[] ibuf = null;
				if(value instanceof Short)
				{
					ibuf = new byte[2];
					ibuf[0] = (byte) ((((Number)value).shortValue() >> 8) & 0xff);
					ibuf[1] = (byte) (((Number)value).shortValue() & 0xff);
				}
				else if(value instanceof Integer)
				{
					ibuf = new byte[4];
					ibuf[0] = (byte) ((((Number)value).intValue() >> 24) & 0xff);
					ibuf[1] = (byte) ((((Number)value).intValue() >> 16) & 0xff);
					ibuf[2] = (byte) ((((Number)value).intValue() >> 8) & 0xff);
					ibuf[3] = (byte) (((Number)value).intValue() & 0xff);
				}
				else if(value instanceof Long)
				{
					ibuf = new byte[8];
					ibuf[0] = (byte) ((((Number)value).longValue() >> 56) & 0xffL);
					ibuf[1] = (byte) ((((Number)value).longValue() >> 48) & 0xffL);
					ibuf[2] = (byte) ((((Number)value).longValue() >> 40) & 0xffL);
					ibuf[3] = (byte) ((((Number)value).longValue() >> 32) & 0xffL);
					ibuf[4] = (byte) ((((Number)value).longValue() >> 24) & 0xffL);
					ibuf[5] = (byte) ((((Number)value).longValue() >> 16) & 0xffL);
					ibuf[6] = (byte) ((((Number)value).longValue() >> 8)  & 0xffL);
					ibuf[7] = (byte) (((Number)value).longValue() & 0xffL);
				}
				else if(value instanceof BigInteger)
				{
					ibuf = ((BigInteger)value).toByteArray();
				}
				result = new String(Base64.encodeBase64(ibuf));
			}
			else if(value instanceof SnmpInt32)
			{
				byte[] ibuf = new byte[4];
				ibuf[0] = (byte) ((((SnmpInt32)value).getValue() >> 24) & 0xff);
				ibuf[1] = (byte) ((((SnmpInt32)value).getValue() >> 16) & 0xff);
				ibuf[2] = (byte) ((((SnmpInt32)value).getValue() >> 8) & 0xff);
				ibuf[3] = (byte) (((SnmpInt32)value).getValue() & 0xff);
				
				result = new String(Base64.encodeBase64(ibuf));
			}
			else if(value instanceof SnmpUInt32)
			{
				byte[] ibuf = new byte[4];
				ibuf[0] = (byte) ((((SnmpUInt32)value).getValue() >> 24) & 0xffL);
				ibuf[1] = (byte) ((((SnmpUInt32)value).getValue() >> 16) & 0xffL);
				ibuf[2] = (byte) ((((SnmpUInt32)value).getValue() >> 8) & 0xffL);
				ibuf[3] = (byte) (((SnmpUInt32)value).getValue() & 0xffL);
				
				result = new String(Base64.encodeBase64(ibuf));
			}
			else if(value instanceof SnmpCounter64)
			{
				byte[] ibuf = ((SnmpCounter64)value).getValue().toByteArray();
				result = new String(Base64.encodeBase64(ibuf));
			}
			else if(value instanceof SnmpOctetString)
			{
				result = new String(Base64.encodeBase64(((SnmpOctetString)value).getString()));
			}
			else if(value instanceof SnmpObjectId)
			{
				result = new String(Base64.encodeBase64(value.toString().getBytes()));
			}
		} 
		
		return result;
	}
	
}
