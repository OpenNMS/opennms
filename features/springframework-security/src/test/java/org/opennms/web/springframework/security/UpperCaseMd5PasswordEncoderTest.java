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
package org.opennms.web.springframework.security;

import junit.framework.TestCase;

public class UpperCaseMd5PasswordEncoderTest extends TestCase {
	public void testAdminEncryption() {
		UpperCaseMd5PasswordEncoder encoder = new UpperCaseMd5PasswordEncoder();
		assertEquals("encoded admin password", "21232F297A57A5A743894A0E4A801FC3", encoder.encodePassword("admin", null));
	}

	public void testPasswordEncoderContract() {
		UpperCaseMd5PasswordEncoder encoder = new UpperCaseMd5PasswordEncoder();
		assertEquals("encode() matches the salt-less encodePassword()", "21232F297A57A5A743894A0E4A801FC3", encoder.encode("admin"));
		assertTrue("matches() accepts the stored uppercase hash", encoder.matches("admin", "21232F297A57A5A743894A0E4A801FC3"));
		assertTrue("matches() is case-insensitive on the stored hash", encoder.matches("admin", "21232f297a57a5a743894a0e4a801fc3"));
		assertFalse("matches() rejects a wrong password", encoder.matches("wrong", "21232F297A57A5A743894A0E4A801FC3"));
		assertFalse("matches() rejects a null stored hash", encoder.matches("admin", null));
	}
}
