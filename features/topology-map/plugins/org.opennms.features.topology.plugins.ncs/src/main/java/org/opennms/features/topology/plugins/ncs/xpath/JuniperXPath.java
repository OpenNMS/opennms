/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
