/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config.poller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Service to be polled for addresses in this
 *  package.
 */

@XmlRootElement(name="service")
@XmlAccessorType(XmlAccessType.NONE)
public class Service implements Serializable {
    private static final long serialVersionUID = -8728008607550515715L;

    /**
     * Service name
     */
    @XmlAttribute(name="name")
    private String m_name;

    /**
     * Interval at which the service is to be polled
     */
    @XmlAttribute(name="interval")
    private Long m_interval;

    /**
     * Specifies if the service is user defined. Used specifically for UI
     * purposes.
     */
    @XmlAttribute(name="user-defined")
    private String m_userDefined = "false";

    /**
     * Status of the service. The service is polled only if this is set to
     * 'on'.
     */
    @XmlAttribute(name="status")
    private String m_status = "on";

    @XmlElement(name="pattern")
    private String m_pattern = null;

    /**
     * Parameters to be used for polling this service. E.g.: for polling HTTP,
     * the URL to hit is configurable via a parameter. Parameters are specific
     * to the service monitor.
     */
    @XmlElement(name="parameter")
    private List<Parameter> m_parameters = new ArrayList<>();


    public Service() {
        super();
        setUserDefined("false");
        setStatus("on");
    }

    public Service(final String name, final long interval, final String userDefined, final String status, final String... parameters) {
        this();
        setName(name);
        setInterval(interval);
        setUserDefined(userDefined);
        setStatus(status);
        
        if (parameters != null && parameters.length > 0) {
            final List<String> params = Arrays.asList(parameters);
            final Iterator<String> paramIterator = params.iterator();
            while (paramIterator.hasNext()) {
                final String key = paramIterator.next();
                if (!paramIterator.hasNext()) {
                    throw new IllegalArgumentException("Odd number of key/value pairs passed to new Service()!");
                }
                final String value = paramIterator.next();
                addParameter(key, value);
            }
        }
    }

    /**
     * Service name
     */
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Interval at which the service is to be polled
     */
    public Long getInterval() {
        return m_interval == null? 0 : m_interval;
    }

    public void setInterval(final Long interval) {
        m_interval = interval;
    }

    @XmlTransient
    public void setInterval(final Integer interval) {
        m_interval = interval == null? null : interval.longValue();
    }

    /**
     * Specifies if the service is user defined. Used specifically for UI
     * purposes.
     */
    public String getUserDefined() {
        return m_userDefined == null? "false" : m_userDefined;
    }

    public void setUserDefined(final String userDefined) {
        m_userDefined = userDefined;
    }

    /**
     * Status of the service. The service is polled only if this is set to
     * 'on'.
     */
    public String getStatus() {
        return m_status == null? "on" : m_status;
    }

    public void setStatus(final String status) {
        m_status = status;
    }

    public String getPattern() {
        return m_pattern;
    }

    public void setPattern(final String pattern) {
        m_pattern = pattern;
    }

    public List<Parameter> getParameters() {
        if (m_parameters == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_parameters);
        }
    }

    public Map<String,Object> getParameterMap() {
        final Map<String,Object> m = new ConcurrentSkipListMap<>();
        for (final Parameter p : this.getParameters()) {
            Object val = p.getValue();
            if (val == null) {
                val = (p.getAnyObject() == null ? "" : p.getAnyObject());
            }
            m.put(p.getKey(), val);
        }
        return m;
    }

    public void setParameters(final List<Parameter> parameters) {
        m_parameters = new ArrayList<Parameter>(parameters);
    }

    public void addParameter(final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.add(parameter);
    }
    
    public void addParameter(final String key, final String value) {
        m_parameters.add(new Parameter(key, value));
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    public Parameter getParameter(final String key) {
        for (final Parameter parameter : m_parameters) {
            if (key.equals(parameter.getKey())) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;
        
        if (obj instanceof Service) {
            final Service temp = (Service)obj;
            if (m_name != null) {
                if (temp.m_name == null) {
                    return false;
                } else if (!(m_name.equals(temp.m_name))) {
                    return false;
                }
            } else if (temp.m_name != null) {
                return false;
            }
            if (m_interval != null) {
                if (temp.m_interval == null) {
                    return false;
                } else if (!(m_interval.equals(temp.m_interval))) {
                    return false;
                }
            } else if (m_interval != null) {
                return false;
            }
            if (m_userDefined != null) {
                if (temp.m_userDefined == null) {
                    return false;
                } else if (!(m_userDefined.equals(temp.m_userDefined))) {
                    return false;
                }
            } else if (temp.m_userDefined != null) {
                return false;
            }
            if (m_status != null) {
                if (temp.m_status == null) {
                    return false;
                } else if (!(m_status.equals(temp.m_status))) {
                    return false;
                }
            } else if (temp.m_status != null) {
                return false;
            }
            if (m_parameters != null) {
                if (temp.m_parameters == null) {
                    return false;
                } else if (!(m_parameters.equals(temp.m_parameters))) {
                    return false;
                }
            } else if (temp.m_parameters != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        int result = 17;
        
        if (m_name != null) {
           result = 37 * result + m_name.hashCode();
        }
        if (m_interval != null) {
            result = 37 * result + m_interval.hashCode();
        }
        if (m_userDefined != null) {
           result = 37 * result + m_userDefined.hashCode();
        }
        if (m_status != null) {
           result = 37 * result + m_status.hashCode();
        }
        if (m_parameters != null) {
           result = 37 * result + m_parameters.hashCode();
        }
        
        return result;
    }

    @Override
    public String toString() {
        return "Service[name=" + m_name +
                ",interval=" + m_interval +
                ",userDefined=" + m_userDefined +
                ",status=" + m_status +
                ",parameters=" + m_parameters +
                "]";
    }
}
