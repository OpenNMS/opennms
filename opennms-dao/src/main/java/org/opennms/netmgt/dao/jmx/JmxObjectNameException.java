package org.opennms.netmgt.dao.jmx;

import org.springframework.dao.UncategorizedDataAccessException;

public class JmxObjectNameException extends UncategorizedDataAccessException {

	private static final long serialVersionUID = -7210917837913419033L;

	public JmxObjectNameException(String message, Throwable nestedException) {
		super(message, nestedException);
	}

}
