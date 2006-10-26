package org.opennms.netmgt.poller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate which distribution contexts that a ServiceMonitor is useful for.
 * The current distribution contexts are defined in the enum type DistributionContext.
 * 
 * If a ServiceMonitor is unannotated then it will be assumed that the ServiceMonitor is only valid when
 * run in the OpenNMS deamon context.
 * 
 * If the annotation exists but no contexts are specified then it is assumed that it works in all contexts.
 * 
 * Reasons that a monitor may not work in all contexts would be for example because it accesses location config
 * files or uses local daemon services such as dhcpd.
 * 
 * Note that this annotation does not inherit.
 * @author brozow
 *
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Distributable {
    DistributionContext[] value() default DistributionContext.ALL;
}
