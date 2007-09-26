package org.opennms.serviceregistration;

import org.opennms.serviceregistration.strategies.AppleStrategy;
import org.opennms.serviceregistration.strategies.NullStrategy;
/* import org.opennms.serviceregistration.strategies.JMDNSStrategy; */

public class ServiceRegistrationFactory {
	private static ServiceRegistrationStrategy s;
	
	private ServiceRegistrationFactory() {
	}

	public static synchronized ServiceRegistrationStrategy getStrategy() throws Exception {
		if (s == null) {
			try {
				s = new AppleStrategy();
			} catch (NoClassDefFoundError e) {
				// we try the next thing then
			}
		}
		/*
		if (s == null) {
			try {
				s = new JMDNSStrategy();
			} catch (NoClassDefFoundError e) {
				// we try the next thing then
			}
		}
		*/
		if (s == null) {
			s = new NullStrategy();
		}
		return s;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Singletons cannot be cloned.");
	}

}
