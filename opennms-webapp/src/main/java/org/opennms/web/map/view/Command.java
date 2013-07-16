/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
            @Override
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
