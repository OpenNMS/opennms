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

package org.opennms.netmgt.rtc.utils;

import java.lang.reflect.UndeclaredThrowableException;

import java.io.Reader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.IOException;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;

import org.opennms.core.utils.ThreadCategory;

// castor generated class
import org.opennms.netmgt.xml.rtc.EuiLevel;


/**
 * The class that marshalls the object to be sent to a PipedReader
 *
 * @author 	<A HREF="mailto:weave@opennms.org">Brian Weaver</A>
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 */
public class PipedMarshaller
{
	private EuiLevel	m_objToMarshall;

	private class MarshalThread
		implements Runnable
	{
		private PipedWriter		m_out;
		private PipedReader		m_in;
		private EuiLevel		m_obj;

		MarshalThread(EuiLevel inp)
			throws IOException
		{
			m_obj = inp;
			m_out = new PipedWriter();
			m_in  = new PipedReader(m_out);
		}
		
		public void run()
		{
			try
			{
				Marshaller.marshal(m_obj, m_out);
				m_out.flush();
				m_out.close();
			}
			catch(MarshalException e)
			{
				ThreadCategory.getInstance(this.getClass()).error("Failed to convert category to xml", e);
				throw new UndeclaredThrowableException(e);
			}
			catch(ValidationException e)
			{
				ThreadCategory.getInstance(this.getClass()).error("Failed to convert category to xml", e);
				throw new UndeclaredThrowableException(e);
			}
			catch(IOException e)
			{
				ThreadCategory.getInstance(this.getClass()).error("Failed to convert category to xml", e);
				throw new UndeclaredThrowableException(e);
			}
		}

		Reader getReader()
		{
			return m_in;
		}
	}

	public PipedMarshaller(EuiLevel toMarshall)
	{
		m_objToMarshall = toMarshall;
	}

	public Reader getReader()
		throws IOException
	{
		Reader inr = null;
		try
		{
			MarshalThread m = new MarshalThread(m_objToMarshall);
			Thread t = new Thread(m, "PipedMarshaller");
			t.start();

			return m.getReader();
		}
		catch(IOException e)
		{
			throw e;
		}
	}
}

