package org.opennms.dashboard.server;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.dashboard.client.Alarm;
import org.opennms.dashboard.client.SurveillanceData;
import org.opennms.dashboard.client.SurveillanceGroup;
import org.opennms.dashboard.client.SurveillanceService;
import org.opennms.dashboard.client.SurveillanceSet;

public class DefaultSurveillanceService implements SurveillanceService {
    
    private int m_count = 0;
    private Timer m_timer = new Timer();
    
    private Random m_random = new Random();
    
    private SurveillanceData m_data;
    

    public SurveillanceData getSurveillanceData() {
        
        System.err.println("Request made!");
        
        if (m_data == null) {
            System.err.println("Creating new data");
            final SurveillanceData data = new SurveillanceData();
            m_data = data;
            
            SurveillanceGroup[] columnGroups = new SurveillanceGroup[] {
                    new SurveillanceGroup("prod", "Production"), 
                    new SurveillanceGroup("test", "Test"), 
                    new SurveillanceGroup("dev", "Developement")
            };
            
            SurveillanceGroup[] rowGroups = new SurveillanceGroup[] {
                    new SurveillanceGroup("ibm", "IBM"),
                    new SurveillanceGroup("hp", "HP"),
                    new SurveillanceGroup("duke", "Duke Hospital"),
                    new SurveillanceGroup("unc", "UNC Hospitals")
            };
            
            data.setColumnGroups(columnGroups);
            data.setRowGroups(rowGroups);
            
            m_timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    
                    System.err.println("Updating data");
                    data.setCell(m_count / data.getColumnCount(), m_count % data.getColumnCount(), ""+m_count);
                    
                    m_count++;
                    
                    if (m_count < data.getColumnCount()*data.getRowCount()) {
                        data.setComplete(false);
                    } else {
                        this.cancel();
                        data.setComplete(true);
                        m_count = 0;
                    }

                }
                
            }, 3000, 2000);
        } else if (m_data.isComplete()) {
            SurveillanceData data = m_data;
            m_data = null;
            return data;
        }
        
        return m_data;

        
    }


    public Alarm[] getAlarmsForSet(SurveillanceSet set) {
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        
        int alarmCount = m_random.nextInt(30);
        
        Alarm[] alarms = new Alarm[alarmCount];
        for(int i = 0; i < alarmCount; i++) {
            alarms[i] = newAlarm();
        }
        
        return alarms;
        
    }

    private Alarm newAlarm() {
        return new Alarm(getSeverify(m_random.nextInt(5)), "node"+m_random.nextInt(20), "An alarm", 2);
    }
    
    private String getSeverify(int count) {
        switch(count % 5) {
        case 0: return "Normal";
        case 1: return "Critical";
        case 2: return "Major";
        case 3: return "Minor";
        case 4: return "Resolved";
        default: return "Normal";
        }
    }

}
