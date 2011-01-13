package org.opennms.core.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>JUnitDNSServer class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface JUnitDNSServer {

    /** the port to listen on **/
    int port() default 9153;

    /** a list of DNS zones to respond to **/
    DNSZone[] zones() default {};
}
