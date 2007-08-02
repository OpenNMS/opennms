package org.opennms.serviceregistration.strategies;

import java.util.Enumeration;
import java.util.Hashtable;

import org.opennms.serviceregistration.ServiceRegistrationStrategy;

import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.RegisterListener;
import com.apple.dnssd.TXTRecord;

public class AppleStrategy implements ServiceRegistrationStrategy, RegisterListener {
	private boolean registered = false;
	private DNSSDRegistration registration;
	private String serviceType;
	private String serviceName;
	private int port;
	private Hashtable<String,String> properties;

	public AppleStrategy() throws Exception {
	}

	public void initialize(String serviceType, String serviceName, int port) throws Exception {
		this.initialize(serviceType, serviceName, port, null);
	}
	
	public void initialize(String serviceType, String serviceName, int port, Hashtable<String,String> properties) throws Exception {
		if (registered == true) {
			throw new Exception("You have already registered a service with this object!");
		}
		
		this.serviceType   = "_" + serviceType.toLowerCase() + "._tcp";
		this.serviceName   = serviceName;
		this.port          = port;
		this.properties    = properties;
	}

	public void register() throws Exception {
		if (registered == false) {
			TXTRecord txt = new TXTRecord();
			if (properties != null) {
				for (Enumeration<String> e = properties.keys(); e.hasMoreElements();) {
					String key = e.nextElement();
					String value = properties.get(key);
					txt.set(key, value);
				}
			}
			DNSSD.register(0, 0, serviceName, serviceType, null, null, port, txt, this);
		} else {
			System.err.println("WARNING: register() called but the service has already been registered!");
		}
	}

	public void unregister() throws Exception {
		if (registered == true) {
			if (registration != null) {
				registration.stop();
				registration = null;
			} else {
				System.err.println("WARNING: unregister() has been called, but registration previously failed.");
			}
		} else {
			System.err.println("WARNING: unregister() called but no service has been registered.");
		}
		
	}

	public void serviceRegistered(DNSSDRegistration registration, int flags, String serviceName, String regType, String domain) {
		this.registration = registration;
		this.registered = true;
	}

	public void operationFailed(DNSSDService service, int errorCode) {
		this.registered = false;
		System.err.println("registration failed for service '" + service + "' with error code '" + errorCode + "'");
	}

}
