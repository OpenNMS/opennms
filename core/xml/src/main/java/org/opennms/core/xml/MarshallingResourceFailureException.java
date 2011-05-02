package org.opennms.core.xml;

import org.springframework.dao.DataAccessResourceFailureException;

public class MarshallingResourceFailureException extends DataAccessResourceFailureException {
	private static final long serialVersionUID = -3634878517879877803L;

	public MarshallingResourceFailureException(final String msg) {
		super(msg);
	}

	public MarshallingResourceFailureException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}