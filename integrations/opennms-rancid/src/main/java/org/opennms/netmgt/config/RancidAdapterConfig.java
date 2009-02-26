package org.opennms.netmgt.config;

import java.util.List;

import org.opennms.netmgt.config.rancid.adapter.PolicyManage;
import org.opennms.netmgt.config.rancid.adapter.Schedule;


public interface RancidAdapterConfig {
    
    public int getThreads();
    
    public int getRetry();
    
    public long getInterval();
        
    public String getType(String sysoid);
    
    public boolean useCategories();

    public boolean hasPolicies();
    
    public boolean hasPolicyManage(String ipaddress);

    public PolicyManage getPolicyManage(String ipaddress);

    public long getDelay(String ipaddress);

    public boolean hasSchedule(String ipaddress);

    public List<Schedule> getSchedules(String ipaddress);    

}
