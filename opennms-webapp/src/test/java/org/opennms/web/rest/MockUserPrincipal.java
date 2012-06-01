package org.opennms.web.rest;

import java.security.Principal;

public class MockUserPrincipal implements Principal {
	
	private static MockUserPrincipal m_instance = null;
	private static String m_name = "admin";

	private MockUserPrincipal() {
	}

	@Override
	public String getName() {
		return m_name;
	}

	public static void setName(final String name) {
		m_name = name;
	}

	public static Principal getInstance() {
		if (m_instance == null) {
			m_instance  = new MockUserPrincipal();
		}
		
		return m_instance;
	}

}
