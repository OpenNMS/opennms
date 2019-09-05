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
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.opennms.core.xml.JaxbUtils;
import org.w3c.dom.Element;

import com.google.common.base.MoreObjects;

public class ComplexPollerParameter implements PollerParameter, Serializable {
    private final Element element;

    public ComplexPollerParameter(final Element element) {
        this.element = Objects.requireNonNull(element);
    }

    public Element getElement() {
        return this.element;
    }

    public <T> T getInstance(final Class<T> clazz) throws JAXBException {
        final Unmarshaller um = JaxbUtils.getUnmarshallerFor(clazz, null, false);
        return clazz.cast(um.unmarshal(element));
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
