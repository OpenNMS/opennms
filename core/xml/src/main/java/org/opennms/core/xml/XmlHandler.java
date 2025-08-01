/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
