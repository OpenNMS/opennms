package org.opennms.netmgt.dao.support;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opennms.netmgt.model.AttributeStatistic;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.opennms.netmgt.model.OnmsAttribute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class BottomNAttributeStatisticVisitor implements AttributeStatisticVisitorWithResults, InitializingBean {

    private Integer m_count;
    private SortedSet<AttributeStatistic> m_results = new TreeSet<AttributeStatistic>(new AttributeStatisticComparator());
    protected Comparator<Double> m_comparator = new DoubleComparator();

    public BottomNAttributeStatisticVisitor() {
        super();
    }

    /**
     * @see org.opennms.netmgt.model.AttributeStatisticVisitor#visit(org.opennms.netmgt.model.OnmsAttribute, double)
     */
    public void visit(OnmsAttribute attribute, double statistic) {
        Assert.notNull(attribute, "attribute argument must not be null");
        
        m_results.add(new AttributeStatistic(attribute, statistic));
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        Assert.state(m_count != null, "property count must be set to a non-null value");
    }

    public Integer getCount() {
        return m_count;
    }

    public void setCount(Integer count) {
        m_count = count;
    }

    /**
     * @return top attribute statistics (up to getCount() number)
     */
    public SortedSet<AttributeStatistic> getResults() {
        SortedSet<AttributeStatistic> top = new TreeSet<AttributeStatistic>(new AttributeStatisticComparator());
        
        for (AttributeStatistic stat : m_results) {
            top.add(stat);
            
            if (top.size() >= m_count) {
                break;
            }
        }
        
        return top;
    }

    public Comparator<Double> getComparator() {
        return m_comparator;
    }

    public void setComparator(Comparator<Double> comparator) {
        m_comparator = comparator;
    }

    public class AttributeStatisticComparator implements Comparator<AttributeStatistic> {
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(AttributeStatistic o1, AttributeStatistic o2) {
            int diff;
            
            diff = getComparator().compare(o1.getStatistic(), o2.getStatistic()); 
            if (diff != 0) {
                return diff;
            }
            
            diff = o1.getAttribute().getResource().getId().compareToIgnoreCase(o2.getAttribute().getResource().getId());
            if (diff != 0) {
                return diff;
            }
            
            return new Integer(o1.getAttribute().hashCode()).compareTo(o2.getAttribute().hashCode());
        }
    }
    
    public class DoubleComparator implements Comparator<Double> {
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Double o1, Double o2) {
            return o1.compareTo(o2);
        }
    }

}