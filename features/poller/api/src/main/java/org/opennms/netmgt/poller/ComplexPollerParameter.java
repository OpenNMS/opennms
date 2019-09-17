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

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opennms.core.xml.JaxbUtils;
import org.w3c.dom.Element;

import com.google.common.base.MoreObjects;

public class ComplexPollerParameter implements PollerParameter, Serializable {
    private final Element element;

    public ComplexPollerParameter(final Element element) {
        this.element = Objects.requireNonNull(element);
        this.element.normalize();
    }

    public Element getElement() {
        return this.element;
    }

    public <T> T getInstance(final Class<T> clazz) {
        // We can not unmarshal the element directly into the required class because the elements do not have the right
        // namespaces attached. Therefore the element is transformed into XML first and then it's read back into the expected
        // bean.

        final StringWriter writer = new StringWriter();
        try {
            final Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer();
            transformer.transform(
                    new DOMSource(this.element),
                    new StreamResult(writer));

        } catch (final TransformerException e) {
            throw new RuntimeException(e);
        }

        return JaxbUtils.unmarshal(clazz, writer.getBuffer().toString());
    }

    @Override
    public Optional<SimplePollerParameter> asSimple() {
        return Optional.empty();
    }

    @Override
    public Optional<ComplexPollerParameter> asComplex() {
        return Optional.of(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComplexPollerParameter)) return false;

        final ComplexPollerParameter that = (ComplexPollerParameter) o;
        return this.element == that.element || (this.element != null && that.element != null && this.element.isEqualNode(that.element));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.element);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("element", this.element)
                .toString();
    }
}
