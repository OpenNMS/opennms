package org.opennms.netmgt.config.invd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

@XmlRootElement(name="service")
public class InvdService implements Serializable, Comparable<InvdService> {
	private static final long serialVersionUID = -811702769197570763L;

	private static final InvdServiceParameter[] OF_PARAMS = new InvdServiceParameter[0];
	
	@XmlAttribute(name="name",required=true)
	private String m_name;
	
	@XmlAttribute(name="status",required=true)
	private String m_status;
	
	@XmlAttribute(name="user-defined", required=true)
	private boolean m_userDefined;
	
	@XmlAttribute(name="interval",required=true)
	private Integer m_interval;
	
	@XmlElement(name="parameter",required=false)
	private List<InvdServiceParameter> m_serviceParameters = new ArrayList<InvdServiceParameter>();

	@XmlTransient
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}
	
	@XmlTransient
	public String getStatus() {
		return m_status;
	}

	public void setStatus(String status) {
		m_status = status;
	}

	@XmlTransient
	public boolean isUserDefined() {
		return m_userDefined;
	}

	public void setUserDefined(boolean userDefined) {
		m_userDefined = userDefined;
	}

	@XmlTransient
	public Integer getInterval() {
		return m_interval;
	}

	public void setInterval(Integer interval) {
		m_interval = interval;
	}

	@XmlTransient
	public List<InvdServiceParameter> getServiceParameters() {
		return m_serviceParameters;
	}

	public void setServiceParameters(List<InvdServiceParameter> serviceParameters) {
		m_serviceParameters = serviceParameters;
	}
	
	public void addServiceParameter(InvdServiceParameter serviceParameter) {
		m_serviceParameters.add(serviceParameter);
	}
	
	public int compareTo(InvdService obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getStatus(), obj.getStatus())
            .append(isUserDefined(), obj.isUserDefined())
            .append(getInterval(), obj.getInterval())
            .append(getServiceParameters().toArray(OF_PARAMS), obj.getServiceParameters().toArray(OF_PARAMS))
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvdService) {
        	InvdService other = (InvdService) obj;
            return new EqualsBuilder()
            	.append(getName(), other.getName())
            	.append(getStatus(), other.getStatus())
            	.append(isUserDefined(), other.isUserDefined())
            	.append(getInterval(), other.getInterval())
            	.append(getServiceParameters().toArray(OF_PARAMS), other.getServiceParameters().toArray(OF_PARAMS))
                .isEquals();
        }
        return false;
    }

}
