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

import com.vaadin.data.Validator;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 *
 * @author Markus von Rüden
 */
public class NameValidatorTest {

	@Test
	public void testValidate() {
		final String[] OK = new String[]{
			"com",  "com_webserver", "someEntry", 
			"HELLO-WORLD", "Hello_woRlD", 
			"some-additional-entry",  "some_Entry", 
			"com.java.op-erating-system" /*strange seperation, but we must test this!*/, 
			"some.entry.separated.by_.dots.a__.lot.of_.dots",
			"a", "ab", "ab.cd", "a.bc", "ab.c"};
		final String[] FAIL = new String[]{
			"", ".", ".org", "opennms.", ".serviceopennms.org", "servicename!",
			"service name", "service,name", "service, name", "straße", "schädel", "hühner", "hölle"};
		NameValidator validator = new NameValidator();
		validate(validator, OK, true);
		validate(validator, FAIL, false);
	}

	public static void validate(Validator validator, String[] names, boolean shouldSucceed) {
		for (String validateMe : names) {
			try {
				validator.validate(validateMe);
				if (!shouldSucceed) fail("Validation succeeded unexpectedly: " + validateMe);
			} catch (Throwable e) {
				if (shouldSucceed) fail("Validation failed: " + validateMe);
			}
		}
	}
}
