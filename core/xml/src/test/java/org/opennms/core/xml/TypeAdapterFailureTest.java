/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.exceptions.ConversionException;
import org.junit.Test;

public class TypeAdapterFailureTest {

    public static class MyFailingAdapter extends XmlAdapter<String,Integer> {
        @Override
        public String marshal(final Integer value) throws Exception {
            return value != null ? Integer.toString(value) : null;
        }

        @Override
        public Integer unmarshal(final String value) throws Exception {
            // Always fail to unmarhsal
            throw new Exception("Oups.");
        }
    }

    @XmlRootElement(name = "my-point")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class MyPoint {
        @XmlJavaTypeAdapter(MyFailingAdapter.class)
        @XmlAttribute(name="x")
        Integer x;
        
        @XmlAttribute(name="y")
        Integer y;
    }

    /**
     * Here we validate that the XmlHandler throws an exception
     * when a type adapter fails to unmarshal.
     */
    @Test(expected=RuntimeException.class)
    public void unmarshalThrowsExceptionWhenAdapterFails() {
        MyPoint p = new MyPoint();
        p.x = p.y = 1;
        XmlHandler<MyPoint> handler = new XmlHandler<>(MyPoint.class);
        handler.unmarshal(handler.marshal(p));
    }

    /**
     * Here we validate that JaxbUtils throws an exception
     * when a type adapter fails to unmarshal.
     */
    @Test(expected=ConversionException.class)
    public void unmarshalThrowsExceptionWhenAdapterFailsInJaxbUtils() {
        MyPoint p = new MyPoint();
        p.x = p.y = 1;
        JaxbUtils.unmarshal(MyPoint.class, JaxbUtils.marshal(p));
    }
}
