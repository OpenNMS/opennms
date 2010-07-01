package org.opennms.netmgt.provision.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Require class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Require {
    String[] value();
}
