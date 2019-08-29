/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;

import org.opennms.core.xml.JaxbUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface PollerParameter {
    Optional<SimplePollerParameter> asSimple();
    Optional<ComplexPollerParameter> asComplex();

    static PollerParameter simple(final String value) {
        return new SimplePollerParameter(value);
    }

    static PollerParameter complex(final Element element) {
        return new ComplexPollerParameter(element);
    }

    static PollerParameter empty() {
        return new SimplePollerParameter("");
    }

    static PollerParameter marshall(final Object value) {
        Objects.requireNonNull(value);

        final Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        final Marshaller marshaller = JaxbUtils.getMarshallerFor(value, null);

        try {
            marshaller.marshal(value, new DOMResult(document));
        } catch (final JAXBException e) {
            throw new RuntimeException(e);
        }

        return complex(document.getDocumentElement());
    }
}
