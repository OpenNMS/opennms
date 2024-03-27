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
