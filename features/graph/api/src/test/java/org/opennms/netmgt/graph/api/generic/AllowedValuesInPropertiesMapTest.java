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

package org.opennms.netmgt.graph.api.generic;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opennms.core.test.OnmsAssert;

import com.google.common.collect.ImmutableList;

public class AllowedValuesInPropertiesMapTest {
    
    enum ExampleEnum {A, B}
    
    @Test
    public void shouldAllowAllPrimitives() {
        Map<String, Object> properties = new MapBuilder<String, Object>()
                .withProperty("a", true)
                .withProperty("b", 1f)
                .withProperty("c", 2)
                .withProperty("d", 4.2)
                .withProperty("e", "blah")
                .withProperty("f", 3f)
                .build();
        // should all pass
        AllowedValuesInPropertiesMap.validate(properties);    
    }
    
    @Test
    public void shouldAllowEnums() {
        Map<String, Object> properties = new MapBuilder<String, Object>()
                .withProperty("a", ExampleEnum.A)
                .withProperty("b", "blah")
                .build();
        // should all pass
        AllowedValuesInPropertiesMap.validate(properties);    
    }
    
    @Test
    public void shouldAllowCollectionsOfCollections() {
    	List<String> nestedCollection = ImmutableList.of("A", "B");
    	List<List<String>> collection = ImmutableList.of(nestedCollection);
        Map<String, Object> properties = new MapBuilder<String, Object>()
                .withProperty("a", collection)
                .withProperty("b", "blah")
                .build();
        // should all pass
        AllowedValuesInPropertiesMap.validate(properties);    
    }
    
    @Test
    public void shouldDisallowMutableCollections() {
        List<String> nestedMutableCollection = Arrays.asList("A", "B");
    	List<List<String>> collection = Arrays.asList(nestedMutableCollection);
        Map<String, Object> properties = new MapBuilder<String, Object>()
                .withProperty("a", collection)
                .withProperty("b", "blah")
                .build();
        // should fail since Arrays$ArrayList is not immutable
        OnmsAssert.assertThrowsException(IllegalArgumentException.class,  () -> AllowedValuesInPropertiesMap.validate(properties));
    }
    
    @Test
    public void shouldDisallowUnknownClasses() {
    	Object objectOfForbiddenType = new Serializable() { }; 
        Map<String, Object> properties = new MapBuilder<String, Object>()
                .withProperty("a", objectOfForbiddenType)
                .withProperty("b", "blah")
                .build();
        // should fail since properties contains one element of not allowed type (Object)
        OnmsAssert.assertThrowsException(IllegalArgumentException.class,  () -> AllowedValuesInPropertiesMap.validate(properties));    
    }
}
