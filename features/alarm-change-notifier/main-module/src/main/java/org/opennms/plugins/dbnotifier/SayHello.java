package org.opennms.plugins.dbnotifier;

/**
 * simple class to print startup message to karaf consol
 * @author admin
 *
 */
public class SayHello {
	public SayHello(){
		super();
		System.out.println("Hello - Alarm Change Notifier started");
	}
	
	public void destroyMethod(){
		System.out.println("Goodbye - Alarm Change Notifier stopped");
	}
}
