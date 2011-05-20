package org.opennms.core.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ValidateUsing {

	/**
	 * The name of the XSD file associated with this JAXB-compatible object.
	 * This will be used by JAXBUtils to validate the XML when passing through
	 * the system.
	 * 
	 * @return The name of the XSD file, without paths.  This file is expected
	 * to be in the classpath, in /xsds/.
	 */
	String value();

}
