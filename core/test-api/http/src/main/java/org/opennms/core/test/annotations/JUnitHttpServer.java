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

    /** the list of virtual hosts to respond to, defaults to "localhost" **/
    String[] vhosts() default { "localhost", "127.0.0.1" };

    /** whether or not to use HTTPS (defaults to HTTP) **/
    boolean https() default false;

    /** whether or not to use basic auth **/
    boolean basicAuth() default false;

    /** the basic auth property file (defaults to target/test-classes/realm.properties **/
    String basicAuthFile() default "target/test-classes/realm.properties";

    /** the location of the keystore if using HTTPS (defaults to target/test-classes/JUnitHttpServer.keystore) **/
    String keystore() default "target/test-classes/JUnitHttpServer.keystore";
    
    /** the keystore password **/
    String keystorePassword() default "opennms";
    
    /** the key password **/
    String keyPassword() default "opennms";

    /** zero or more webapps to include, with contexts **/
    Webapp[] webapps() default { };
    
}
