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
