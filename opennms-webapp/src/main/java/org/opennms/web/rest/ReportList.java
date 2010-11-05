package org.opennms.web.rest;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.reportd.Report;

@XmlRootElement(name="reports")
public class ReportList extends LinkedList<Report>{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int m_totalCount;
    
    public ReportList() {
        super();
    }
    
    public ReportList(Collection<? extends Report> c) {
        super(c);
    }
    
    @XmlElement(name ="report")
    public List<Report> getReports(){
        return this;
    }
    
    public void setReports(List<Report> reports) {
        clear();
        addAll(reports);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
        return this.size();
    }
    
    @XmlAttribute(name="totalCount")
    public int getTotalCount() {
        return m_totalCount;
    }
    
    public void setTotalCount(int count) {
        m_totalCount = count;
    }
}