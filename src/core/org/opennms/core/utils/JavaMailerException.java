/*
 * Created on Sep 16, 2004
 *
 * Copyright (C) 2004, The OpenNMS Group, Inc.
 * 
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.core.utils;

/**Exception used to create proper return code
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace</A>
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JavaMailerException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public JavaMailerException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param Exception message
	 */
	public JavaMailerException(String message) {
		super(message);
	}

	/**
	 * @param Exception message
	 * @param cause
	 */
	public JavaMailerException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public JavaMailerException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
