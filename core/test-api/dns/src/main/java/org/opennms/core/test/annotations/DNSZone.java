package org.opennms.core.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface DNSZone {

    String name();
    String v4address() default "127.0.0.1";
    String v6address() default "::1";
    DNSEntry[] entries() default {};

}
