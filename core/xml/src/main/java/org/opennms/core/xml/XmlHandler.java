/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.xml;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Fast XML marshaling and unmarshaling.
 *
 * This class attempts to perform as much of the initialization as possible
 * when constructed, in order to make the calls to {@link #marshal(Object)}
 * and {@link #unmarshal(String)} as quick as possible. 
 *
 * Instances of theses objects are not thread safe.
 *
 * @author jwhite
 */
public class XmlHandler<U> {
    private final Class<U> clazz;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;

    public XmlHandler(Class<U> clazz) {
        this.clazz = clazz;
        JAXBContext context;
        try {
            context = JaxbUtils.getContextFor(clazz);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        this.marshaller = JaxbUtils.getMarshallerFor(clazz, context);
        this.unmarshaller = JaxbUtils.getUnmarshallerFor(clazz, context, false);
        // Use the same event handler that we use in JaxbUtils
        try {
            unmarshaller.setEventHandler(new JaxbUtils.LoggingValidationEventHandler());
        } catch (JAXBException e) {
            throw new RuntimeException("An error was encountered while setting the event handler", e);
        }
    }

    public String marshal(U obj) {
        final StringWriter jaxbWriter = new StringWriter();
        try {
            marshaller.marshal(obj, jaxbWriter);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return jaxbWriter.toString();
    }

    public U unmarshal(String xml) {
        try (final StringReader sr = new StringReader(xml)) {
            return clazz.cast(unmarshaller.unmarshal(sr));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
