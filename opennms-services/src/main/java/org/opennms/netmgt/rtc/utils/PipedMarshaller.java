/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rtc.utils;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.lang.reflect.UndeclaredThrowableException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.xml.rtc.EuiLevel;

/**
 * The class that marshalls the object to be sent to a PipedReader
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
 */
public class PipedMarshaller {
    private EuiLevel m_objToMarshall;

    private class MarshalThread implements Runnable {
        private PipedWriter m_out;

        private PipedReader m_in;

        private EuiLevel m_obj;

        MarshalThread(EuiLevel inp) throws IOException {
            m_obj = inp;
            m_out = new PipedWriter();
            m_in = new PipedReader(m_out);
        }

        @Override
        public void run() {
            try {
                Marshaller.marshal(m_obj, m_out);
                m_out.flush();
                m_out.close();
            } catch (MarshalException e) {
                ThreadCategory.getInstance(this.getClass()).error("Failed to convert category to xml", e);
                throw new UndeclaredThrowableException(e);
            } catch (ValidationException e) {
                ThreadCategory.getInstance(this.getClass()).error("Failed to convert category to xml", e);
                throw new UndeclaredThrowableException(e);
            } catch (IOException e) {
                ThreadCategory.getInstance(this.getClass()).warn("Failed to convert category to xml", e);
                // don't rethrow, it just bubbles up into output.log and confuses people, the error still shows in rtc.log
                // throw new UndeclaredThrowableException(e);
            }
        }

        Reader getReader() {
            return m_in;
        }
    }

    /**
     * <p>Constructor for PipedMarshaller.</p>
     *
     * @param toMarshall a {@link org.opennms.netmgt.xml.rtc.EuiLevel} object.
     */
    public PipedMarshaller(EuiLevel toMarshall) {
        m_objToMarshall = toMarshall;
    }

    /**
     * <p>getReader</p>
     *
     * @return a {@link java.io.Reader} object.
     * @throws java.io.IOException if any.
     */
    public Reader getReader() throws IOException {
        try {
            MarshalThread m = new MarshalThread(m_objToMarshall);
            Thread t = new Thread(m, "PipedMarshaller");
            t.start();

            return m.getReader();
        } catch (IOException e) {
            throw e;
        }
    }
}
