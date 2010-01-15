package org.opennms.core.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface JUnitHttpServer {

    /** the directory from which to serve test files **/
    String resource() default "target/test-classes";
    
    /** the port to listen on **/
    int port() default 9162;

    /** whether or not to use HTTPS (defaults to HTTP) **/
    boolean https() default false;
}
