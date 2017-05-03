/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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
