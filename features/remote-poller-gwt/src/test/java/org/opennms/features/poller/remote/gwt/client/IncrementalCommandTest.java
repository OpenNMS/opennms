package org.opennms.features.poller.remote.gwt.client;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.google.gwt.user.client.IncrementalCommand;


public class IncrementalCommandTest {
    
    public class CommandCounter {
        
        private int m_commandCount = 0;

        public int getCount() {
            return m_commandCount;
        }
        
        public void addToCount(int numToAdd) {
            m_commandCount += numToAdd;
        }
    }

    ExecutorService m_executor = Executors.newSingleThreadExecutor();
    
    @Test
    public void testIncrementalCommand() {
        final CommandCounter commandCounter = new CommandCounter();

        IncrementalCommand command = new IncrementalCommand() {
            int m_counter = 0;
            public boolean execute() {
                
                if(m_counter == 2) {
                    commandCounter.addToCount(m_counter);
                    return false;
                }else {
                    m_counter++;
                    return true;
                }
            }
        };
        
        executeCommand(command);
        
        assertEquals(2, commandCounter.getCount());
    }
    
    @Test
    public void testMultipleIncrementalCommands() {
        final CommandCounter commandCounter = new CommandCounter();

        IncrementalCommand command = new IncrementalCommand() {
            int m_counter = 0;
            public boolean execute() {
                
                if(m_counter == 100000000) {
                    commandCounter.addToCount(m_counter);
                    return false;
                }else {
                    m_counter++;
                    return true;
                }
                
            }
        };
        
        
        executeCommand(command);
        
        assertEquals(100000000, commandCounter.getCount());
    }
    
    @Test
    public void testFindCommandInQueue() {
        
    }
    
    @Test
    public void testLocationInfoUpdateIncrementalCommand() {
        
    }

    private void executeCommand(IncrementalCommand command) {
       while(command.execute()) {
           
       }
    }
}
