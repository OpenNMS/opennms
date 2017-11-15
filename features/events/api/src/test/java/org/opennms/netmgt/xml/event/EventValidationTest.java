/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventValidationTest {
    private static final Logger LOG = LoggerFactory.getLogger(EventValidationTest.class);
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testEmptyEvent() throws Exception {
        final Validator validator = factory.getValidator();
        final Event event = new Event();
        final Set<ConstraintViolation<Event>> errors = validator.validate(event);
        assertNull(event.getSource());
        assertEquals(2, errors.size());
    }

    @Test
    public void testEmptySource() throws Exception {
        final Validator validator = factory.getValidator();
        final Event event = new Event();
        event.setTime(new Date());
        final Set<ConstraintViolation<Event>> errors = validator.validate(event);
        assertNull(event.getSource());
        assertEquals(1, errors.size());
    }

    @Test
    public void testBadDbid() throws Exception {
        final Validator validator = factory.getValidator();
        final Event event = new Event();
        event.setSource("tests");
        event.setTime(new Date());
        event.setDbid(-1);
        final Set<ConstraintViolation<Event>> errors = validator.validate(event);
        assertEquals(1, errors.size());
    }

    @Test
    public void testBadState() throws Exception {
        final Validator validator = factory.getValidator();
        final Event event = new Event();
        event.setSource("tests");
        event.setTime(new Date());
        final Operaction action = new Operaction();
        action.setState("monkey");
        event.addOperaction(action);
        final Set<ConstraintViolation<Event>> errors = validator.validate(event);
        LOG.debug("errors: {}", errors);
        assertEquals(2, errors.size());
    }

}
