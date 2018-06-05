/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.vaadin.jmxconfiggenerator.TestHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.DefaultNameProvider;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.NameProvider;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.SelectionManager;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

/**
 * Created by mvrueden on 14/07/15.
 */
public class UniqueAttributeNameValidatorTest {

    private SelectionManager selectionManager;

    @Before
    public void before() {
        final Mbean mbean = TestHelper.createMbean("MBean1");
        mbean.addAttrib(TestHelper.createAttrib("attribute1", "attrib1"));
        mbean.addCompAttrib(
                TestHelper.createCompAttrib("compAttribute1",
                        TestHelper.createCompMember("compMember1", "compMem1"),
                        TestHelper.createCompMember("compMember2", "compMem2")
                ));


        selectionManager = new SelectionManager() {
            @Override
            public Collection<Attrib> getSelectedAttributes(Mbean otherMbean) {
                return mbean.getAttribList();
            }

            @Override
            public Collection<CompMember> getSelectedCompositeMembers(CompAttrib compAttrib) {
                return mbean.getCompAttribList().get(0).getCompMemberList();
            }

            @Override
            public Collection<CompAttrib> getSelectedCompositeAttributes(Mbean otherMbean) {
                return mbean.getCompAttribList();
            }

            @Override
            public Collection<Mbean> getSelectedMbeans() {
                return Arrays.asList(new Mbean[]{ mbean });
            }
        };
    }

    @Test
    public void testEmpty() {
        DefaultNameProvider defaultNameProvider = new DefaultNameProvider(SelectionManager.EMPTY);
        Collection<String> names = new DefaultNameProvider(SelectionManager.EMPTY).getNamesMap().values();
        Assert.assertNotNull(names);
        Assert.assertEquals(Boolean.TRUE.booleanValue(), names.isEmpty());

        UniqueAttributeNameValidator validator = new UniqueAttributeNameValidator(defaultNameProvider);
        Assert.assertEquals(true, validator.isValid("not-existing"));
        Assert.assertEquals(true, validator.isValid("compMem1"));
        Assert.assertEquals(true, validator.isValid("attrib1"));
    }

    @Test
    public void testNotEmpty() {
        Mbean mbean = selectionManager.getSelectedMbeans().iterator().next();

        DefaultNameProvider nameProvider = new DefaultNameProvider(selectionManager);
        List<String> names = new ArrayList<>(nameProvider.getNamesMap().values());
        Assert.assertNotNull(names);
        Assert.assertEquals(3, names.size());
        Collections.sort(names);
        Assert.assertTrue(Arrays.equals(new String[]{"attrib1", "compMem1", "compMem2"}, names.toArray(new String[names.size()])));

        mbean.addAttrib(TestHelper.createAttrib("attribute2", "attrib1")); // alias clash
        names = new ArrayList<>(nameProvider.getNamesMap().values());
        Assert.assertEquals(4, names.size());

        UniqueAttributeNameValidator validator = new UniqueAttributeNameValidator(nameProvider);
        Assert.assertEquals(Boolean.TRUE.booleanValue(), validator.isValid("yes"));
        Assert.assertEquals(Boolean.TRUE.booleanValue(), validator.isValid("compMem1"));
        Assert.assertEquals(Boolean.FALSE.booleanValue(), validator.isValid("attrib1"));
    }

    /**
     * We test that the values in the fieldMap are considered while validating the uniqueness of aliases.
     */
    @Test
    public void testOverriddenByField() {
        // simulate a user input in a text field bound to the MBeans only CompAttrib's CompMember (see method before/setUp)
        Field<String> dummyField = new TextField();
        dummyField.setValue("attrib1");
        CompMember compMember = selectionManager.getSelectedCompositeMembers(null).iterator().next();
        final Map<Object, Field<String>> fieldMap = new HashMap<>();
        fieldMap.put(compMember, dummyField);

        // Verify nameProvider
        NameProvider nameProvider = new DefaultNameProvider(selectionManager);
        List<String> names = new ArrayList<>(nameProvider.getNamesMap().values());
        Assert.assertNotNull(names);
        Collections.sort(names);
        Assert.assertTrue(Arrays.equals(new String[]{"attrib1", "compMem1", "compMem2"}, names.toArray(new String[names.size()])));

        // Verify validator
        UniqueAttributeNameValidator validator = new UniqueAttributeNameValidator(nameProvider, new UniqueAttributeNameValidator.FieldProvider() {
            @Override
            public Map<Object, Field<String>> getObjectFieldMap() {
                return fieldMap;
            }
        });
        names = validator.getNames();
        Assert.assertNotNull(names);
        Collections.sort(names);
        Assert.assertTrue(Arrays.equals(new String[]{"attrib1", "attrib1", "compMem2"}, names.toArray(new String[names.size()])));
        Assert.assertEquals(false, validator.isValid("attrib1"));
        Assert.assertEquals(true, validator.isValid("compMem2"));
    }


}
