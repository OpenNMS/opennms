package org.opennms.web.services;


@SuppressWarnings("serial")
public class ServiceException extends RuntimeException {

	public ServiceException(String msg, Exception e) {
		super(msg, e);
	}

}
