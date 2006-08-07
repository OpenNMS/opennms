package org.opennms.web.acegisecurity;

import org.opennms.web.acegisecurity.UpperCaseMd5PasswordEncoder;

import junit.framework.TestCase;

public class UpperCaseMd5PasswordEncoderTest extends TestCase {
	public void testAdminEncryption() {
		UpperCaseMd5PasswordEncoder encoder = new UpperCaseMd5PasswordEncoder();
		assertEquals("encoded admin password", "21232F297A57A5A743894A0E4A801FC3", encoder.encodePassword("admin", null));
	}
}
