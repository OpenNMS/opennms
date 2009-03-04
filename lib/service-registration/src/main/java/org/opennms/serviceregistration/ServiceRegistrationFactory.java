package org.opennms.serviceregistration;

public class ServiceRegistrationFactory {
	private static ServiceRegistrationStrategy s;
	private static final String CLASS_PROPERTY = "org.opennms.serviceregistration.strategy";
    private static final String[] classes = {
            "org.opennms.serviceregistration.strategies.AppleStrategy",
            "org.opennms.serviceregistration.strategies.NullStrategy"
    };
    /* JMDNSStrategy is disabled for now, you can ask for it if you really want it */
	
	private ServiceRegistrationFactory() {
	}

	public static synchronized ServiceRegistrationStrategy getStrategy() throws Exception {
		if (s == null) {
		    if (System.getProperty(CLASS_PROPERTY) != null) {
		        try {
	                s = (ServiceRegistrationStrategy)(Class.forName(System.getProperty(CLASS_PROPERTY)).newInstance());
		        } catch (NoClassDefFoundError e) {
		            System.err.println("unable to load class specified in " + CLASS_PROPERTY + ": " + e.getMessage());
		        }
		    }
            if (s == null) {
                for (String className : classes) {
		            try {
		                s = (ServiceRegistrationStrategy)(Class.forName(className).newInstance());
		            } catch (NoClassDefFoundError e) {
		                // fall through silently for now
		            } catch (UnsatisfiedLinkError e) {
                        // fall through silently for now
		            }
		            if (s != null) {
		                break;
		            }
		        }
		    }
		}
		
		if (s == null) {
		    System.err.println("an error occurred finding any service registration strategy");
		}
		return s;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Singletons cannot be cloned.");
	}

}
