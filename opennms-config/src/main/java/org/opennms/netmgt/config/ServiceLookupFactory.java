package org.opennms.netmgt.config;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.lookup.ServiceRegistryLookup;
import org.opennms.core.soa.support.DefaultServiceRegistry;

public class ServiceLookupFactory {

    private static final ServiceLookup<Class<?>, String> serviceLookup = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
            .blocking()
            .build();

    private ServiceLookupFactory() { super(); }

    public static <T> void withService(Class<T> clazz, java.util.function.Consumer<T> callback) {
        T service = serviceLookup.lookup(clazz, null);
        if (service != null) {
            callback.accept(service);
        }
    }
}
