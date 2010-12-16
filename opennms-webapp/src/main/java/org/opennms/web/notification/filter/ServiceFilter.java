package org.opennms.web.notification.filter;

import javax.servlet.ServletContext;

import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

/**
 * Encapsulates all service filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ServiceFilter extends EqualsFilter<Integer> {
    /** Constant <code>TYPE="service"</code> */
    public static final String TYPE = "service";

    /**
     * <p>Constructor for ServiceFilter.</p>
     *
     * @param serviceId a int.
     */
    public ServiceFilter(int serviceId) {
        super(TYPE, SQLType.INT, "SERVICEID", "serviceType.id", serviceId);
    }
    
    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextDescription(ServletContext servletContext) {
        String serviceName = Integer.toString(getServiceId());
        serviceName = NetworkElementFactory.getInstance(servletContext).getServiceNameFromId(getServiceId());

        return (TYPE + "=" + serviceName);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return ("<WebNotificationRepository.ServiceFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getServiceId</p>
     *
     * @return a int.
     */
    public int getServiceId() {
        return getValue();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}
