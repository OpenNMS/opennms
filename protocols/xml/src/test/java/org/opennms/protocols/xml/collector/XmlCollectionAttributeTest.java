/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.protocols.xml.config.XmlObject;

/**
 * The Test Class for XmlCollectionAttribute.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollectionAttributeTest {

    /**
     * Test numeric values with units.
     */
    @Test
    public void testNumericValuesWithUnits() {
        XmlObject object = new XmlObject("test", "gauge");
        AttributeGroupType group = new AttributeGroupType("xml-data", AttributeGroupType.IF_TYPE_ALL);
        final XmlCollectionAttributeType type = new XmlCollectionAttributeType(object, group);
        
        // Standard Numeric Value
        XmlCollectionAttribute a = new XmlCollectionAttribute(null, type, "4.6");
        Assert.assertEquals("4.6", a.getNumericValue());
        Assert.assertEquals("4.6", a.getStringValue());

        // Percentage Value
        a = new XmlCollectionAttribute(null, type, "4.6%");
        Assert.assertEquals("4.6", a.getNumericValue());
        Assert.assertEquals("4.6%", a.getStringValue());

        // Value with Units - Model 1
        a = new XmlCollectionAttribute(null, type, "4.6Bps");
        Assert.assertEquals("4.6", a.getNumericValue());
        Assert.assertEquals("4.6Bps", a.getStringValue());

        // Value with Units - Model 2
        a = new XmlCollectionAttribute(null, type, "4.6 bps");
        Assert.assertEquals("4.6", a.getNumericValue());
        Assert.assertEquals("4.6 bps", a.getStringValue());

        // Negative value
        a = new XmlCollectionAttribute(null, type, "-42");
        Assert.assertEquals("-42.0", a.getNumericValue());
        Assert.assertEquals("-42", a.getStringValue());

        // Negative Value with Units
        a = new XmlCollectionAttribute(null, type, "-32 celcius");
        Assert.assertEquals("-32.0", a.getNumericValue());
        Assert.assertEquals("-32 celcius", a.getStringValue());

        // Value in scientific notation - Model 1
        a = new XmlCollectionAttribute(null, type, "4.2E2");
        Assert.assertEquals("420.0", a.getNumericValue());
        Assert.assertEquals("4.2E2", a.getStringValue());

        // Value in scientific notation - Model 2
        a = new XmlCollectionAttribute(null, type, "-4e-2");
        Assert.assertEquals("-0.04", a.getNumericValue());
        Assert.assertEquals("-4e-2", a.getStringValue());
    }

}
