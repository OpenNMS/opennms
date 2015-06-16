package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation;

import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.vaadin.jmxconfiggenerator.TestHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.DefaultNameProvider;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.NameProvider;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.SelectionManager;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import java.util.*;

/**
 * Created by mvrueden on 14/07/15.
 */
public class UniqueAttributeNameValidatorTest {

    private SelectionManager selectionManager;

    @Before
    public void before() {
        final Mbean mbean = TestHelper.createMbean("MBean1");
        mbean.getAttrib().add(TestHelper.createAttrib("attribute1", "attrib1"));
        mbean.getCompAttrib().add(
                TestHelper.createCompAttrib("compAttribute1",
                        TestHelper.createCompMember("compMember1", "compMem1"),
                        TestHelper.createCompMember("compMember2", "compMem2")
                ));


        selectionManager = new SelectionManager() {
            @Override
            public Collection<Attrib> getSelectedAttributes(Mbean otherMbean) {
                return mbean.getAttrib();
            }

            @Override
            public Collection<CompMember> getSelectedCompositeMembers(CompAttrib compAttrib) {
                return mbean.getCompAttrib().get(0).getCompMember();
            }

            @Override
            public Collection<CompAttrib> getSelectedCompositeAttributes(Mbean otherMbean) {
                return mbean.getCompAttrib();
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
        List<String> names = new DefaultNameProvider(SelectionManager.EMPTY).getNames();
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
        List<String> names = nameProvider.getNames();
        Assert.assertNotNull(names);
        Assert.assertEquals(3, names.size());
        Collections.sort(names);
        Assert.assertTrue(Arrays.equals(new String[]{"attrib1", "compMem1", "compMem2"}, names.toArray(new String[names.size()])));

        mbean.getAttrib().add(TestHelper.createAttrib("attribute2", "attrib1")); // alias clash
        names = new DefaultNameProvider(selectionManager).getNames();
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
        NameProvider nameProvider = new DefaultNameProvider(selectionManager, new NameProvider.FieldValueProvider() {
            @Override
            public String getFieldValue(Object input) {
                return fieldMap.get(input) != null ? fieldMap.get(input).getValue() : null;
            }
        });
        List<String> names = nameProvider.getNames();
        Assert.assertNotNull(names);
        Assert.assertTrue(Arrays.equals(new String[]{"attrib1", "attrib1", "compMem2"}, names.toArray(new String[names.size()])));

        // Verify validator
        UniqueAttributeNameValidator validator = new UniqueAttributeNameValidator(nameProvider);
        Assert.assertEquals(false, validator.isValid("attrib1"));
        Assert.assertEquals(true, validator.isValid("compMem2"));
    }


}
