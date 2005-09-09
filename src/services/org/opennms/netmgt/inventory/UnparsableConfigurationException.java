/*
 * Created on 11-giu-2004

 */
package org.opennms.netmgt.inventory;

/**
 * @author maurizio
 *
 */
public class UnparsableConfigurationException extends Exception {
 	public UnparsableConfigurationException(){
 	}
 	
	public UnparsableConfigurationException(String message) {
	super(message);
	}
}
