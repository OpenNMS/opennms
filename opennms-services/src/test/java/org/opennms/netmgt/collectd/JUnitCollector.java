package org.opennms.netmgt.collectd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface JUnitCollector {
    String schemaConfig() default "/org/opennms/netmgt/config/test-database-schema.xml";
    String datacollectionConfig();
    String datacollectionType();
    String[] anticipateRrds() default {};
    String[] anticipateFiles() default {};
}
