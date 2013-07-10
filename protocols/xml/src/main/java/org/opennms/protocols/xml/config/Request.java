/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.xml.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class Request.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="request")
@XmlAccessorType(XmlAccessType.FIELD)
public class Request {

    /** The method. */
    @XmlAttribute
    private String method = "GET";

    /** The parameters. */
    @XmlElement(name="parameter")
    private List<Parameter> parameters = new ArrayList<Parameter>();

    /** The headers. */
    @XmlElement(name="header")
    private List<Header> headers = new ArrayList<Header>();

    /** The content. */
    @XmlElement
    private Content content;

    /**
     * Gets the method.
     *
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Gets the value of a specific parameter.
     *
     * @param name the name
     * @return the parameter
     */
    public String getParameter(String name) {
        for (Parameter p : parameters) {
            if (p.getName().equals(name)) {
                return p.getValue();
            }
        }
        return null;
    }

    /**
     * Gets the parameter as integer.
     *
     * @param name the name
     * @return the parameter value as integer
     */
    public int getParameterAsInt(String name) {
        for (Parameter p : parameters) {
            if (p.getName().equals(name)) {
                try {
                    return Integer.parseInt(p.getValue());
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Gets the headers.
     *
     * @return the headers
     */
    public List<Header> getHeaders() {
        return headers;
    }

    /**
     * Gets the value of a specific header.
     *
     * @param name the name
     * @return the header value
     */
    public String getHeader(String name) {
        for (Header h : headers) {
            if (h.getName().equals(name)) {
                return h.getValue();
            }
        }
        return null;
    }

    /**
     * Gets the content.
     *
     * @return the content
     */
    public Content getContent() {
        return content;
    }

    /**
     * Sets the method.
     *
     * @param method the new method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Sets the parameters.
     *
     * @param parameters the new parameters
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Adds the parameter.
     *
     * @param name the name
     * @param value the value
     */
    public void addParameter(String name, String value) {
        getParameters().add(new Parameter(name, value));
    }

    /**
     * Sets the headers.
     *
     * @param headers the new headers
     */
    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    /**
     * Adds the header.
     *
     * @param name the name
     * @param value the value
     */
    public void addHeader(String name, String value) {
        getHeaders().add(new Header(name, value));
    }

    /**
     * Sets the content.
     *
     * @param content the new content
     */
    public void setContent(Content content) {
        this.content = content;
    }
}