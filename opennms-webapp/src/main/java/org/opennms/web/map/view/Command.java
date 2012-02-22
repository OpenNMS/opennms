package org.opennms.web.map.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Command {
    private final BufferedReader out;
    private final Process p;
    private boolean scheduledtoremove;
    private int scheduletoremoverequest = 0;
    
    private final List<String> lines = new ArrayList<String>();

    public Command(String command) throws IOException, IllegalStateException
    {
        if(command.startsWith("traceroute") || command.startsWith("ping") || command.startsWith("ipmitool")){
                 p = Runtime.getRuntime().exec(command);
                out = new BufferedReader(new InputStreamReader(p.getInputStream()));
        }else{
                throw new IllegalStateException("Command "+ command+" not supported.");
        }
        
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    String s = null;
                    while((s = out.readLine()) != null)
                    {
                        addLineBuffer(s);
                    }
                    
                }
                catch(IOException io){
                    throw new IllegalStateException("Error while writing the IO buffer");
                }
            }
        }, this.getClass().getSimpleName()).start();
        
    }
    
    private synchronized void addLineBuffer(String line) {
        lines.add(line);
    }
    
    public synchronized String getNextLine() {
        scheduledtoremove=false;
        scheduletoremoverequest=0;
        if (lines.size() > 0)
            return lines.remove(0);
        return null;
    }
    
    public boolean runned() {
        try {
            p.exitValue();
            return true;
        } catch (IllegalThreadStateException exc) {
            return false;
        }
    }
    
    public boolean scheduledToRemove() {
        return scheduledtoremove;
    }
    
    public void scheduleToRemove() {
        scheduletoremoverequest++;
        if (scheduletoremoverequest > 2  )
        scheduledtoremove=true;
    }
}
