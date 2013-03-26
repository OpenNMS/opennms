package org.opennms.netmgt.config.accesspointmonitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.netmgt.accesspointmonitor.poller.AccessPointPoller;

/**
 * <p>
 * Parameter class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public class Package implements Serializable, Comparable<Package> {
    private static final long serialVersionUID = -988483514208208854L;

    private static final String[] OF_SPECIFICS = new String[0];
    private static final IpRange[] OF_INCLUDERANGES = new IpRange[0];
    private static final IpRange[] OF_EXCLUDERANGES = new IpRange[0];

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlElement(name = "filter", required = true)
    private String m_filter;

    @XmlElement(name = "specific")
    private List<String> m_specifics = new ArrayList<String>();

    @XmlElement(name = "include-range")
    private List<IpRange> m_includeRanges = new ArrayList<IpRange>();

    @XmlElement(name = "exclude-range")
    private List<IpRange> m_excludeRanges = new ArrayList<IpRange>();

    @XmlElement(name = "service", required = true)
    private Service m_service;

    private boolean m_isDynamic = false;

    public Package() {

    }

    public Package(Package copy) {
        if (copy.m_name != null) {
            m_name = new String(copy.m_name);
        }
        if (copy.m_filter != null) {
            m_filter = new String(copy.m_filter);
        }
        for (String s : copy.m_specifics) {
            m_specifics.add(new String(s));
        }
        for (IpRange r : copy.m_includeRanges) {
            m_includeRanges.add(new IpRange(r));
        }
        for (IpRange r : copy.m_excludeRanges) {
            m_excludeRanges.add(new IpRange(r));
        }
        if (copy.m_service != null) {
            m_service = new Service(copy.m_service);
        }
        m_isDynamic = copy.m_isDynamic;
    }

    @XmlTransient
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public boolean nameHasWildcard() {
        return m_name.endsWith("%");
    }

    @XmlTransient
    public String getFilter() {
        return m_filter;
    }

    public String getEffectiveFilter() {
        return m_filter.replaceAll("%packageName%", getName());
    }

    public void setFilter(String filter) {
        m_filter = filter;
    }

    @XmlTransient
    public List<String> getSpecifics() {
        return m_specifics;
    }

    public void setSpecifics(List<String> specifics) {
        m_specifics = specifics;
    }

    public void addSpecific(String specific) {
        m_specifics.add(specific);
    }

    @XmlTransient
    public List<IpRange> getIncludeRanges() {
        return m_includeRanges;
    }

    public void setIncludeRanges(List<IpRange> includeRanges) {
        m_includeRanges = includeRanges;
    }

    public void addIncludeRange(IpRange includeRange) {
        m_includeRanges.add(includeRange);
    }

    @XmlTransient
    public List<IpRange> getExcludeRanges() {
        return m_excludeRanges;
    }

    public void setExcludeRanges(List<IpRange> excludeRanges) {
        m_excludeRanges = excludeRanges;
    }

    public void addExcludeRange(IpRange excludeRange) {
        m_excludeRanges.add(excludeRange);
    }

    @XmlTransient
    public Service getService() {
        return m_service;
    }

    public void setService(Service service) {
        m_service = service;
    }

    @XmlTransient
    public Service getEffectiveService() {
        // Create a deep copy of the service - the templates might change
        Service service;
        try {
            service = (Service) getService().clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
        ServiceTemplate template = service.getTemplate();

        // Inherit the attributes form the template, if these are not set in
        // the template, then use default values
        if (StringUtils.isBlank(service.getPassiveServiceName())) {
            if (template != null && StringUtils.isNotBlank(template.getPassiveServiceName())) {
                service.setPassiveServiceName(template.getPassiveServiceName());
            } else {
                // Default to the service name
                service.setPassiveServiceName(service.getName());
            }
        }

        if (service.getThreads() == null) {
            if (template != null && template.getThreads() != null) {
                service.setThreads(template.getThreads());
            } else {
                // Default to 3 threads
                service.setThreads(3);
            }
        }

        if (service.getInterval() == null) {
            if (template != null && template.getInterval() != null) {
                service.setInterval(template.getInterval());
            } else {
                // Default to 5 minutes
                service.setInterval(300000L);
            }
        }

        if (StringUtils.isBlank(service.getStatus())) {
            if (template != null && StringUtils.isNotBlank(template.getStatus())) {
                service.setStatus(template.getStatus());
            } else {
                // Default to on
                service.setStatus("on");
            }
        }

        if (template == null)
            return service;

        // Add parameters from the template to the service if they don't
        // already exist
        Map<String, String> serviceParameters = service.getParameterMap();
        Map<String, String> templateParameters = template.getParameterMap();

        for (String key : templateParameters.keySet()) {
            if (!serviceParameters.containsKey(key)) {
                service.addParameter(new Parameter(key, templateParameters.get(key)));
            }
        }

        return service;
    }

    public void setIsDynamic(boolean isDynamic) {
        m_isDynamic = isDynamic;
    }

    @XmlTransient
    public boolean getIsDynamic() {
        return m_isDynamic;
    }

    private static Class<? extends AccessPointPoller> findPollingStrategyClass(final Monitor monitor) throws ClassNotFoundException {
        final Class<? extends AccessPointPoller> ps = Class.forName(monitor.getClassName()).asSubclass(AccessPointPoller.class);
        if (!AccessPointPoller.class.isAssignableFrom(ps)) {
            throw new MarshallingResourceFailureException("The monitor for service: " + monitor.getService() + " class-name: " + monitor.getClassName() + " must implement PollingStrategy");
        }
        return ps;
    }

    public AccessPointPoller getPoller(List<Monitor> monitors) {
        for (Monitor monitor : monitors) {
            if (monitor.getService().compareToIgnoreCase(m_service.getName()) != 0)
                continue;

            try {
                final Class<? extends AccessPointPoller> psClass = findPollingStrategyClass(monitor);
                return (AccessPointPoller) psClass.newInstance();
            } catch (final ClassNotFoundException e) {
                LogUtils.warnf(this, e, "Unable to location monitor for service: %s class-name: %s", monitor.getService(), monitor.getClassName());
            } catch (IllegalAccessException e) {
                LogUtils.warnf(this, e, e.getMessage());
            } catch (InstantiationException e) {
                LogUtils.warnf(this, e, e.getMessage());
            }
        }

        return null;
    }

    public int compareTo(Package obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getFilter(), obj.getFilter())
            .append(getSpecifics().toArray(OF_SPECIFICS), obj.getSpecifics().toArray(OF_SPECIFICS))
            .append(getIncludeRanges().toArray(OF_INCLUDERANGES), obj.getIncludeRanges().toArray(OF_INCLUDERANGES))
            .append(getExcludeRanges().toArray(OF_EXCLUDERANGES), obj.getExcludeRanges().toArray(OF_EXCLUDERANGES))
            .toComparison();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_excludeRanges == null) ? 0 : m_excludeRanges.hashCode());
        result = prime * result + ((m_filter == null) ? 0 : m_filter.hashCode());
        result = prime * result + ((m_includeRanges == null) ? 0 : m_includeRanges.hashCode());
        result = prime * result + (m_isDynamic ? 1231 : 1237);
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_service == null) ? 0 : m_service.hashCode());
        result = prime * result + ((m_specifics == null) ? 0 : m_specifics.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Package) {
            Package other = (Package) obj;
            return new EqualsBuilder()
                .append(getName(), other.getName())
                .append(getFilter(), other.getFilter())
                .append(getSpecifics().toArray(OF_SPECIFICS), other.getSpecifics().toArray(OF_SPECIFICS))
                .append(getIncludeRanges().toArray(OF_INCLUDERANGES), other.getIncludeRanges().toArray(OF_INCLUDERANGES))
                .append(getExcludeRanges().toArray(OF_EXCLUDERANGES), other.getExcludeRanges().toArray(OF_EXCLUDERANGES))
                .isEquals();
        }
        return false;
    }
}
