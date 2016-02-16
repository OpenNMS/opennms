package org.opennms.netmgt.poller.remote.metadata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.poller.remote.metadata.MetadataField.Validator;

public class EmailValidatorTest {
    private Validator m_validator;

    @Before
    public void setUp() {
        m_validator = new EmailValidator();
    }

    @Test
    public void testValid() {
        assertTrue(m_validator.isValid("ranger@opennms.org"));
        assertTrue(m_validator.isValid("ranger@monkey.esophagus"));
        assertTrue(m_validator.isValid("ranger@giant.list.of.sub.domains.com"));
    }

    @Test
    public void testInvalid() {
        assertFalse(m_validator.isValid("ranger@opennms"));
        assertFalse(m_validator.isValid("ranger.monkey.esophagus"));
        assertFalse(m_validator.isValid("ranger@"));
        assertFalse(m_validator.isValid("@foo.com"));
        assertFalse(m_validator.isValid("@foo.com."));
        assertFalse(m_validator.isValid("@foo.com"));
        assertFalse(m_validator.isValid(".@foo.com"));
        assertFalse(m_validator.isValid(".e@foo.com"));
    }
}
