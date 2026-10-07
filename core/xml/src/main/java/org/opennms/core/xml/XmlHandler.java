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
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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
    private final XMLInputFactory xmlInputFactory;

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
        // Use the same StAX parser as unmarshal(Reader), but disable DTD processing and external
        // entities. A crafted IPC message then cannot cause an XXE.
        this.xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
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
        XMLStreamReader reader = null;
        try {
            reader = xmlInputFactory.createXMLStreamReader(new StringReader(xml));
            return clazz.cast(unmarshaller.unmarshal(reader));
        } catch (XMLStreamException e) {
            throw new RuntimeException(new UnmarshalException(e));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    // The reader holds no resources that need a close.
                }
            }
        }
    }
}
