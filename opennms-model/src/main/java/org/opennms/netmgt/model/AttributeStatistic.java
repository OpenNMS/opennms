/**
 * 
 */
package org.opennms.netmgt.model;


public class AttributeStatistic implements Comparable<AttributeStatistic> {
    private OnmsAttribute m_attribute;
    private Double m_statistic;
    
    public AttributeStatistic(OnmsAttribute attribute, Double statistic) {
        m_attribute = attribute;
        m_statistic = statistic;
    }
    
    public OnmsAttribute getAttribute() {
        return m_attribute;
    }
    
    public Double getStatistic() {
        return m_statistic;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(AttributeStatistic o) {
        int diff;
        
        diff = getStatistic().compareTo(o.getStatistic()); 
        if (diff != 0) {
            return diff;
        }
        
        diff = getAttribute().getResource().getId().compareToIgnoreCase(o.getAttribute().getResource().getId());
        if (diff != 0) {
            return diff;
        }
        
        return new Integer(getAttribute().hashCode()).compareTo(o.getAttribute().hashCode());
    }
}