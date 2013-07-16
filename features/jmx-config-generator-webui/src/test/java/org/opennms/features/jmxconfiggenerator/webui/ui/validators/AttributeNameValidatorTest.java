/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator.webui.ui.validators;

import org.opennms.features.jmxconfiggenerator.webui.ui.validators.AttributeNameValidator;
import com.vaadin.data.Validator;
import org.junit.Test;

/**
 *
 * @author Markus von Rüden
 */
public class AttributeNameValidatorTest {

	@Test
	public void testValidate() {
		final String[] OK = new String[]{
			"com", "comwebserver", "someEntry",
			"HELLOWORLD", "HellowoRlD", "a", "ab"
			};
		final String[] FAIL = new String[]{
			"", ".", ".org", "opennms.", ".serviceopennms.org", "servicename!",
			"someadditional-entry", "some_Entry",
			"com.java.op-erating-system","some.entry.separated.by_.dots.a__.lot.of_.dots",
			"ab.cd", "a.bc", "ab.c",
			"service name", "service,name", "service, name", "straße", "schädel", "hühner", "hölle"};
		Validator validator = new AttributeNameValidator();
		MBeansNameValidatorTest.validate(validator, OK, true);
		MBeansNameValidatorTest.validate(validator, FAIL, false);
	}
}
