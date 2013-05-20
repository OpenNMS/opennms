package org.opennms.features.topology.plugins.ncs.xpath;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.camel.component.bean.XPathAnnotationExpressionFactory;
import org.apache.camel.language.LanguageAnnotation;
import org.apache.camel.language.NamespacePrefix;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@LanguageAnnotation(language = "xpath", factory = XPathAnnotationExpressionFactory.class)
public @interface JuniperXPath {
    String value();
    
    NamespacePrefix[] namespaces() default{
        @NamespacePrefix(prefix = "soap", uri = "http://www.w3.org/2003/05/soap-envelope"),
        @NamespacePrefix(prefix = "xsd", uri = "http://www.w3.org/2001/XMLSchema"),
        @NamespacePrefix(prefix = "juniper", uri = "services.schema.networkapi.jmp.juniper.net")
    };
    
    Class<?> resultType() default Object.class;
}
