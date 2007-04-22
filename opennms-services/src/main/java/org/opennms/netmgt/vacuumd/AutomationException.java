package org.opennms.netmgt.vacuumd;

public class AutomationException extends RuntimeException {

	private static final long serialVersionUID = 689408072882653037L;

	public AutomationException(String arg0) {
		super(arg0);
	}

	public AutomationException(Throwable arg0) {
		super(arg0);
	}

	public AutomationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
