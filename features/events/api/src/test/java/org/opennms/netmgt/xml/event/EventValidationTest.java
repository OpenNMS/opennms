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
        event.setDbid(-1L);
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
