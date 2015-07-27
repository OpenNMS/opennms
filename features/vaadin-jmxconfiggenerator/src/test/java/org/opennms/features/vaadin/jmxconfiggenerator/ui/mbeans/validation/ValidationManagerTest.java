package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.vaadin.jmxconfiggenerator.TestHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.NameProvider;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.SelectionManager;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ValidationManagerTest {

    private ValidationManager manager;

    @Before
    public void before() {
        manager = new ValidationManager(TestHelper.DUMMY_NAME_PROVIDER, SelectionManager.EMPTY);
    }

    @Test(expected=NullPointerException.class)
    public void testGetValidatorObjectFail() {
        manager.getValidator("Ulf");
    }

    @Test(expected=NullPointerException.class)
    public void testGetValidatorClassFail() {
        manager.getValidator(String.class);
    }

    @Test
    public void testGetValidatorObjectOk() {
        ValidationManager.Validator validator = manager.getValidator(new Mbean());
        Assert.assertNotNull(validator);
        Assert.assertEquals(ValidationManager.MbeanValidator.class, validator.getClass());
    }

    @Test
    public void testGetValidatorClassOk() {
        ValidationManager.Validator validator = manager.getValidator(Mbean.class);
        Assert.assertNotNull(validator);
        Assert.assertEquals(ValidationManager.MbeanValidator.class, validator.getClass());
    }
}
