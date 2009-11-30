package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;


public class QuartzSchedulerService {
    
    private Scheduler m_scheduler;
  
    
    public List<TriggerDescription> getAll() {
        
        String [] triggerGroups;
        
        List<TriggerDescription> triggerList = new ArrayList<TriggerDescription>();
        
        try {
            triggerGroups = m_scheduler.getTriggerGroupNames();
            for (int j = 0; j < triggerGroups.length; j++) {
                triggerList.addAll(this.get(triggerGroups[j]));
            }
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

        return triggerList;
        
        
    }
    
    public List<TriggerDescription> get(String triggerGroup) {
        
        String [] triggers;
        
        List<TriggerDescription> triggerList = new ArrayList<TriggerDescription>();
        
        try {
            triggers = m_scheduler.getTriggerNames(triggerGroup);
            for (int j = 0; j < triggers.length; j++) {
                TriggerDescription trigger = new TriggerDescription();
                trigger.setGroup(triggerGroup);
                trigger.setName(triggers[j]);
                triggerList.add(trigger);
             }
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return triggerList;

        
    }

}
