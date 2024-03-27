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
package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation;

import com.vaadin.v7.data.Validator;
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
		NameValidatorTest.validate(validator, OK, true);
		NameValidatorTest.validate(validator, FAIL, false);
	}
}
