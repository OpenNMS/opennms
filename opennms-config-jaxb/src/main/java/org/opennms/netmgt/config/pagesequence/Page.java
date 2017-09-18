/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.pagesequence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * This element specifies all the possible attributes in as fine grained
 * detail as possible. All that is really required (as you can see below) is
 * the "path" attribute. From that one attribute, the IP address passed in
 * through the ServiceMonitor and ServiceCollector interface, the URL will be
 * fully generated using the supplied defaults in this config. Configure
 * attributes these attributes to the level of detail you need to fully
 * control the behavior.
 * </p>
 * <p>
 * A little bit of indirection is possible here with the host attribute. If
 * the host attribute is anything other than the default, that value will be
 * used instead of the IP address passed in through the API (Interface).
 * </p>
 */

@XmlRootElement(name="page")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"m_method","m_httpVersion","m_userAgent","m_virtualHost","m_scheme","m_userInfo","m_host","m_requireIPv6","m_requireIPv4","m_disableSslVerification","m_port","m_path","m_query","m_fragment","m_failureMatch","m_failureMessage","m_successMatch","m_locationMatch","m_responseRange","m_dsName", "m_parameters","m_sessionVariables"})
public class Page implements Serializable {
    private static final long serialVersionUID = -8690979689322573975L;

    @XmlAttribute(name="method")
    private String m_method = "GET";

    @XmlAttribute(name="http-version")
    private String m_httpVersion = "1.1";

    @XmlAttribute(name="user-agent")
    private String m_userAgent;

    @XmlAttribute(name="virtual-host")
    private String m_virtualHost;

    @XmlAttribute(name="scheme")
    private String m_scheme = "http";

    @XmlAttribute(name="user-info")
    private String m_userInfo;

    @XmlAttribute(name="host")
    private String m_host = "${ipaddr}";

    @XmlAttribute(name="requireIPv6")
    private Boolean m_requireIPv6;

    @XmlAttribute(name="requireIPv4")
    private Boolean m_requireIPv4;

    /**
     * This element is used to enable or disable SSL host and certificate
     * verification. Default: true (verification is disabled)
     */
    @XmlAttribute(name="disable-ssl-verification")
    private String m_disableSslVerification = "true";

    @XmlAttribute(name="port")
    private Integer m_port = 80;

    @XmlAttribute(name="path")
    private String m_path;

    @XmlAttribute(name="query")
    private String m_query;

    @XmlAttribute(name="fragment")
    private String m_fragment;

    @XmlAttribute(name="failureMatch")
    private String m_failureMatch;

    @XmlAttribute(name="failureMessage")
    private String m_failureMessage;

    @XmlAttribute(name="successMatch")
    private String m_successMatch;

    @XmlAttribute(name="locationMatch")
    private String m_locationMatch;

    @XmlAttribute(name="response-range")
    private String m_responseRange = "100-399";

    @XmlAttribute(name="ds-name")
    private String m_dsName;

    /**
     * Currently only used for HTTP form parameters.
     */
    @XmlElement(name="parameter")
    private List<Parameter> m_parameters = new ArrayList<>();

    /**
     * Assign the value of a regex match group to a session variable with a
     * user-defined name. The match group is identified by number and must be
     * zero or greater.
     */
    @XmlElement(name="session-variable")
    private List<SessionVariable> m_sessionVariables = new ArrayList<>();


    public Page() {
        super();
    }

    public String getMethod() {
        return m_method == null? "GET" : m_method;
    }

    public void setMethod(final String method) {
        m_method = method == null? null : method.intern();
    }

    public String getHttpVersion() {
        return m_httpVersion == null? "1.1" : m_httpVersion;
    }

    public void setHttpVersion(final String httpVersion) {
        m_httpVersion = httpVersion == null? null : httpVersion.intern();
    }

    public String getUserAgent() {
        return m_userAgent;
    }

    public void setUserAgent(final String userAgent) {
        m_userAgent = userAgent == null? null : userAgent.intern();
    }

    public String getVirtualHost() {
        return m_virtualHost;
    }

    public void setVirtualHost(final String virtualHost) {
        m_virtualHost = virtualHost == null? null : virtualHost.intern();
    }

    public String getScheme() {
        return m_scheme == null? "http" : m_scheme;
    }

    public void setScheme(final String scheme) {
        m_scheme = scheme == null? null : scheme.intern();
    }

    public String getUserInfo() {
        return m_userInfo;
    }

    public void setUserInfo(final String userInfo) {
        m_userInfo = userInfo == null? null : userInfo.intern();
    }

    public String getHost() {
        return m_host == null? "${ipaddr}" : m_host;
    }

    public void setHost(final String host) {
        m_host = host == null? null : host.intern();
    }

    public Boolean getRequireIPv4() {
        return m_requireIPv4 == null? false : m_requireIPv4;
    }

    public boolean isRequireIPv4() {
        return m_requireIPv4 == null? false : m_requireIPv4;
    }

    public void setRequireIPv4(final Boolean requireIPv4) {
        m_requireIPv4 = requireIPv4;
    }

    public Boolean getRequireIPv6() {
        return m_requireIPv6 == null? false : m_requireIPv6;
    }

    public boolean isRequireIPv6() {
        return m_requireIPv6 == null? false : m_requireIPv6;
    }

    public void setRequireIPv6(final Boolean requireIPv6) {
        m_requireIPv6 = requireIPv6;
    }

    /**
     * This element is used to enable or disable SSL host and certificate
     * verification. Default: true (verification is disabled)
     */
    public String getDisableSslVerification() {
        return m_disableSslVerification == null? "true" : m_disableSslVerification;
    }

    public void setDisableSslVerification(final String disableSslVerification) {
        m_disableSslVerification = disableSslVerification;
    }

    public Integer getPort() {
        return m_port == null? 80 : m_port;
    }

    public void setPort(final Integer port) {
        m_port = port;
    }

    public String getPath() {
        return m_path;
    }

    public void setPath(final String path) {
        m_path = path == null? null : path.intern();
    }

    public String getQuery() {
        return m_query;
    }

    public void setQuery(final String query) {
        m_query = query == null? null : query.intern();
    }

    public String getFragment() {
        return m_fragment;
    }

    public void setFragment(final String fragment) {
        m_fragment = fragment == null? null : fragment.intern();
    }

    public String getFailureMatch() {
        return m_failureMatch;
    }

    public void setFailureMatch(final String failureMatch) {
        m_failureMatch = failureMatch == null? null : failureMatch.intern();
    }

    public String getFailureMessage() {
        return m_failureMessage;
    }

    public void setFailureMessage(final String failureMessage) {
        m_failureMessage = failureMessage == null? null : failureMessage.intern();
    }

    public String getSuccessMatch() {
        return m_successMatch;
    }

    public void setSuccessMatch(final String successMatch) {
        m_successMatch = successMatch == null? null : successMatch.intern();
    }

    public String getLocationMatch() {
        return m_locationMatch;
    }

    public void setLocationMatch(final String locationMatch) {
        m_locationMatch = locationMatch == null? null : locationMatch.intern();
    }

    public String getResponseRange() {
        return m_responseRange == null? "100-399" : m_responseRange;
    }

    public void setResponseRange(final String responseRange) {
        m_responseRange = responseRange == null? null : responseRange.intern();
    }

    public String getDsName() {
        return m_dsName;
    }

    public void setDsName(final String dsName) {
        m_dsName = dsName == null? null : dsName.intern();
    }

    public List<Parameter> getParameters() {
        if (m_parameters == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_parameters);
        }
    }

    public void setParameters(final List<Parameter> parameters) {
        m_parameters = new ArrayList<Parameter>(parameters);
    }

    public void addParameter(final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    public List<SessionVariable> getSessionVariables() {
        if (m_sessionVariables == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_sessionVariables);
        }
    }

    public void setSessionVariables(final List<SessionVariable> sessionVariables) {
        m_sessionVariables = new ArrayList<SessionVariable>(sessionVariables);
    }

    public void addSessionVariable(final SessionVariable sessionVariable) throws IndexOutOfBoundsException {
        m_sessionVariables.add(sessionVariable);
    }

    public boolean removeSessionVariable(final SessionVariable sessionVariable) {
        return m_sessionVariables.remove(sessionVariable);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_disableSslVerification == null) ? 0 : m_disableSslVerification.hashCode());
        result = prime * result + ((m_dsName == null) ? 0 : m_dsName.hashCode());
        result = prime * result + ((m_failureMatch == null) ? 0 : m_failureMatch.hashCode());
        result = prime * result + ((m_failureMessage == null) ? 0 : m_failureMessage.hashCode());
        result = prime * result + ((m_fragment == null) ? 0 : m_fragment.hashCode());
        result = prime * result + ((m_host == null) ? 0 : m_host.hashCode());
        result = prime * result + ((m_httpVersion == null) ? 0 : m_httpVersion.hashCode());
        result = prime * result + ((m_locationMatch == null) ? 0 : m_locationMatch.hashCode());
        result = prime * result + ((m_method == null) ? 0 : m_method.hashCode());
        result = prime * result + ((m_parameters == null) ? 0 : m_parameters.hashCode());
        result = prime * result + ((m_path == null) ? 0 : m_path.hashCode());
        result = prime * result + ((m_port == null) ? 0 : m_port.hashCode());
        result = prime * result + ((m_query == null) ? 0 : m_query.hashCode());
        result = prime * result + ((m_requireIPv4 == null) ? 0 : m_requireIPv4.hashCode());
        result = prime * result + ((m_requireIPv6 == null) ? 0 : m_requireIPv6.hashCode());
        result = prime * result + ((m_responseRange == null) ? 0 : m_responseRange.hashCode());
        result = prime * result + ((m_scheme == null) ? 0 : m_scheme.hashCode());
        result = prime * result + ((m_sessionVariables == null) ? 0 : m_sessionVariables.hashCode());
        result = prime * result + ((m_successMatch == null) ? 0 : m_successMatch.hashCode());
        result = prime * result + ((m_userAgent == null) ? 0 : m_userAgent.hashCode());
        result = prime * result + ((m_userInfo == null) ? 0 : m_userInfo.hashCode());
        result = prime * result + ((m_virtualHost == null) ? 0 : m_virtualHost.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Page)) {
            return false;
        }
        final Page other = (Page) obj;
        if (m_disableSslVerification == null) {
            if (other.m_disableSslVerification != null) {
                return false;
            }
        } else if (!m_disableSslVerification.equals(other.m_disableSslVerification)) {
            return false;
        }
        if (m_dsName == null) {
            if (other.m_dsName != null) {
                return false;
            }
        } else if (!m_dsName.equals(other.m_dsName)) {
            return false;
        }
        if (m_failureMatch == null) {
            if (other.m_failureMatch != null) {
                return false;
            }
        } else if (!m_failureMatch.equals(other.m_failureMatch)) {
            return false;
        }
        if (m_failureMessage == null) {
            if (other.m_failureMessage != null) {
                return false;
            }
        } else if (!m_failureMessage.equals(other.m_failureMessage)) {
            return false;
        }
        if (m_fragment == null) {
            if (other.m_fragment != null) {
                return false;
            }
        } else if (!m_fragment.equals(other.m_fragment)) {
            return false;
        }
        if (m_host == null) {
            if (other.m_host != null) {
                return false;
            }
        } else if (!m_host.equals(other.m_host)) {
            return false;
        }
        if (m_httpVersion == null) {
            if (other.m_httpVersion != null) {
                return false;
            }
        } else if (!m_httpVersion.equals(other.m_httpVersion)) {
            return false;
        }
        if (m_locationMatch == null) {
            if (other.m_locationMatch != null) {
                return false;
            }
        } else if (!m_locationMatch.equals(other.m_locationMatch)) {
            return false;
        }
        if (m_method == null) {
            if (other.m_method != null) {
                return false;
            }
        } else if (!m_method.equals(other.m_method)) {
            return false;
        }
        if (m_parameters == null) {
            if (other.m_parameters != null) {
                return false;
            }
        } else if (!m_parameters.equals(other.m_parameters)) {
            return false;
        }
        if (m_path == null) {
            if (other.m_path != null) {
                return false;
            }
        } else if (!m_path.equals(other.m_path)) {
            return false;
        }
        if (m_port == null) {
            if (other.m_port != null) {
                return false;
            }
        } else if (!m_port.equals(other.m_port)) {
            return false;
        }
        if (m_query == null) {
            if (other.m_query != null) {
                return false;
            }
        } else if (!m_query.equals(other.m_query)) {
            return false;
        }
        if (m_requireIPv4 == null) {
            if (other.m_requireIPv4 != null) {
                return false;
            }
        } else if (!m_requireIPv4.equals(other.m_requireIPv4)) {
            return false;
        }
        if (m_requireIPv6 == null) {
            if (other.m_requireIPv6 != null) {
                return false;
            }
        } else if (!m_requireIPv6.equals(other.m_requireIPv6)) {
            return false;
        }
        if (m_responseRange == null) {
            if (other.m_responseRange != null) {
                return false;
            }
        } else if (!m_responseRange.equals(other.m_responseRange)) {
            return false;
        }
        if (m_scheme == null) {
            if (other.m_scheme != null) {
                return false;
            }
        } else if (!m_scheme.equals(other.m_scheme)) {
            return false;
        }
        if (m_sessionVariables == null) {
            if (other.m_sessionVariables != null) {
                return false;
            }
        } else if (!m_sessionVariables.equals(other.m_sessionVariables)) {
            return false;
        }
        if (m_successMatch == null) {
            if (other.m_successMatch != null) {
                return false;
            }
        } else if (!m_successMatch.equals(other.m_successMatch)) {
            return false;
        }
        if (m_userAgent == null) {
            if (other.m_userAgent != null) {
                return false;
            }
        } else if (!m_userAgent.equals(other.m_userAgent)) {
            return false;
        }
        if (m_userInfo == null) {
            if (other.m_userInfo != null) {
                return false;
            }
        } else if (!m_userInfo.equals(other.m_userInfo)) {
            return false;
        }
        if (m_virtualHost == null) {
            if (other.m_virtualHost != null) {
                return false;
            }
        } else if (!m_virtualHost.equals(other.m_virtualHost)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Page [method=" + m_method + ", httpVersion=" + m_httpVersion + ", userAgent=" + m_userAgent + ", virtualHost=" + m_virtualHost + ", scheme=" + m_scheme + ", userInfo="
                + m_userInfo + ", host=" + m_host + ", requireIPv6=" + m_requireIPv6 + ", requireIPv4=" + m_requireIPv4 + ", disableSslVerification=" + m_disableSslVerification + ", port="
                + m_port + ", path=" + m_path + ", query=" + m_query + ", fragment=" + m_fragment + ", failureMatch=" + m_failureMatch + ", failureMessage=" + m_failureMessage
                + ", successMatch=" + m_successMatch + ", locationMatch=" + m_locationMatch + ", responseRange=" + m_responseRange + ", dsName=" + m_dsName + ", parameters=" + m_parameters
                + ", sessionVariables=" + m_sessionVariables + "]";
    }


}
