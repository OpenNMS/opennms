package org.opennms.plugins.elasticsearch.rest;

/**
 * simple class to print startup message to karaf consol
 * @author cgallen
 *
 */
public class SayHello {
	public SayHello(){
		super();
		System.out.println("Hello - Elastic Search ReST Event Forwarder started");
	}
	
	public void destroyMethod(){
		System.out.println("Goodbye - Elastic Search ReST Event Forwarder stopped");
	}
}
