/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.web.command.LocationMonitorIdCommand;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultDistributedPollerServiceTest extends TestCase {
    private List<Object> m_mocks;
    private LocationMonitorDao m_locationMonitorDao;
    private DefaultDistributedPollerService m_distributedPollerService;
    
    @Override
    protected void setUp() {
        m_mocks = new LinkedList<Object>();
        
        m_locationMonitorDao = createMock(LocationMonitorDao.class);
        m_mocks.add(m_locationMonitorDao);
        
        m_distributedPollerService = new DefaultDistributedPollerService();
        m_distributedPollerService.setLocationMonitorDao(m_locationMonitorDao);
    }
    
    public void testPauseLocationMonitorSuccess() {
        OnmsLocationMonitor locationMonitor = new OnmsLocationMonitor();
        locationMonitor.setId(1);
        locationMonitor.setStatus(MonitorStatus.STARTED);
        expect(m_locationMonitorDao.load(locationMonitor.getId())).andReturn(locationMonitor);
        m_locationMonitorDao.update(locationMonitor);
        
        LocationMonitorIdCommand command = new LocationMonitorIdCommand();
        command.setMonitorId(1);
        
        BindException errors = new BindException(command, "command");
        
        replayMocks();
        m_distributedPollerService.pauseLocationMonitor(command, errors);
        verifyMocks();
        
        assertEquals("error count", 0, errors.getErrorCount());
        assertEquals("new monitor status", MonitorStatus.PAUSED, locationMonitor.getStatus());
    }
    
    public void testPauseLocationMonitorAlreadyPaused() {
        OnmsLocationMonitor locationMonitor = new OnmsLocationMonitor();
        locationMonitor.setId(1);
        locationMonitor.setStatus(MonitorStatus.PAUSED);
        expect(m_locationMonitorDao.load(locationMonitor.getId())).andReturn(locationMonitor);
        
        LocationMonitorIdCommand command = new LocationMonitorIdCommand();
        command.setMonitorId(1);
        
        BindException errors = new BindException(command, "command");
        
        replayMocks();
        m_distributedPollerService.pauseLocationMonitor(command, errors);
        verifyMocks();
        
        assertEquals("error count", 1, errors.getErrorCount());
        List<ObjectError> errorList = getErrorList(errors);
        assertEquals("error list count", 1, errorList.size());
        assertEquals("error 0 code", "distributed.locationMonitor.alreadyPaused", errorList.get(0).getCode());
    }
    

    public void testPauseLocationMonitorBindingErrors() {
        LocationMonitorIdCommand command = new LocationMonitorIdCommand();
        
        BindException errors = new BindException(command, "command");
        errors.addError(new ObjectError("foo", null, null, "foo"));
        assertEquals("error count before pause", 1, errors.getErrorCount());
        
        replayMocks();
        m_distributedPollerService.pauseLocationMonitor(command, errors);
        verifyMocks();
        
        assertEquals("error count after pause", 1, errors.getErrorCount());
    }
    
    
    public void testPauseLocationMonitorNullCommand() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("command argument cannot be null"));

        LocationMonitorIdCommand command = new LocationMonitorIdCommand();
        BindException errors = new BindException(command, "command");

        replayMocks();
        try {
            m_distributedPollerService.pauseLocationMonitor(null, errors);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        verifyMocks();
    }
    
    public void testPauseLocationMonitorNullBindException() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("errors argument cannot be null"));

        LocationMonitorIdCommand command = new LocationMonitorIdCommand();

        replayMocks();
        try {
            m_distributedPollerService.pauseLocationMonitor(command, null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        verifyMocks();
    }
    
    public void testResumeLocationMonitorSuccess() {
        OnmsLocationMonitor locationMonitor = new OnmsLocationMonitor();
        locationMonitor.setId(1);
        locationMonitor.setStatus(MonitorStatus.PAUSED);
        expect(m_locationMonitorDao.load(locationMonitor.getId())).andReturn(locationMonitor);
        m_locationMonitorDao.update(locationMonitor);
        
        LocationMonitorIdCommand command = new LocationMonitorIdCommand();
        command.setMonitorId(1);
        
        BindException errors = new BindException(command, "command");
        
        replayMocks();
        m_distributedPollerService.resumeLocationMonitor(command, errors);
        verifyMocks();
        
        assertEquals("error count", 0, errors.getErrorCount());
        assertEquals("new monitor status", MonitorStatus.STARTED, locationMonitor.getStatus());
    }
    
    public void testResumeLocationMonitorNotPaused() {
        OnmsLocationMonitor locationMonitor = new OnmsLocationMonitor();
        locationMonitor.setId(1);
        locationMonitor.setStatus(MonitorStatus.STARTED);
        expect(m_locationMonitorDao.load(locationMonitor.getId())).andReturn(locationMonitor);
        
        LocationMonitorIdCommand command = new LocationMonitorIdCommand();
        command.setMonitorId(1);
        
        BindException errors = new BindException(command, "command");
        
        replayMocks();
        m_distributedPollerService.resumeLocationMonitor(command, errors);
        verifyMocks();
        
        assertEquals("error count", 1, errors.getErrorCount());
        List<ObjectError> errorList = getErrorList(errors);
        assertEquals("error list count", 1, errorList.size());
        assertEquals("error 0 code", "distributed.locationMonitor.notPaused", errorList.get(0).getCode());
    }
    

    public void testResumeLocationMonitorBindingErrors() {
        LocationMonitorIdCommand command = new LocationMonitorIdCommand();
        
        BindException errors = new BindException(command, "command");
        errors.addError(new ObjectError("foo", null, null, "foo"));
        assertEquals("error count before pause", 1, errors.getErrorCount());
        
        replayMocks();
        m_distributedPollerService.resumeLocationMonitor(command, errors);
        verifyMocks();
        
        assertEquals("error count after pause", 1, errors.getErrorCount());
    }
    
    
    public void testResumeLocationMonitorNullCommand() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("command argument cannot be null"));

        LocationMonitorIdCommand command = new LocationMonitorIdCommand();
        BindException errors = new BindException(command, "command");

        replayMocks();
        try {
            m_distributedPollerService.resumeLocationMonitor(null, errors);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        verifyMocks();
    }
    
    public void testResumeLocationMonitorNullBindException() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("errors argument cannot be null"));

        LocationMonitorIdCommand command = new LocationMonitorIdCommand();

        replayMocks();
        try {
            m_distributedPollerService.resumeLocationMonitor(command, null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        verifyMocks();
    }
    
    public void testDeleteLocationMonitorSuccess() {
        OnmsLocationMonitor locationMonitor = new OnmsLocationMonitor();
        locationMonitor.setId(1);
        expect(m_locationMonitorDao.load(locationMonitor.getId())).andReturn(locationMonitor);
        m_locationMonitorDao.delete(locationMonitor);
        
        LocationMonitorIdCommand command = new LocationMonitorIdCommand();
        command.setMonitorId(1);
        
        BindException errors = new BindException(command, "command");
        
        replayMocks();
        m_distributedPollerService.deleteLocationMonitor(command, errors);
        verifyMocks();
        
        assertEquals("error count", 0, errors.getErrorCount());
    }
    

    public void testDeleteLocationMonitorBindingErrors() {
        LocationMonitorIdCommand command = new LocationMonitorIdCommand();
        
        BindException errors = new BindException(command, "command");
        errors.addError(new ObjectError("foo", null, null, "foo"));
        assertEquals("error count before pause", 1, errors.getErrorCount());
        
        replayMocks();
        m_distributedPollerService.deleteLocationMonitor(command, errors);
        verifyMocks();
        
        assertEquals("error count after pause", 1, errors.getErrorCount());
    }
    
    
    public void testDeleteLocationMonitorNullCommand() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("command argument cannot be null"));

        LocationMonitorIdCommand command = new LocationMonitorIdCommand();
        BindException errors = new BindException(command, "command");

        replayMocks();
        try {
            m_distributedPollerService.deleteLocationMonitor(null, errors);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        verifyMocks();
    }
    
    public void testDeleteLocationMonitorNullBindException() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("errors argument cannot be null"));

        LocationMonitorIdCommand command = new LocationMonitorIdCommand();

        replayMocks();
        try {
            m_distributedPollerService.deleteLocationMonitor(command, null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        verifyMocks();
    }
    
    private void replayMocks() {
        for (Object o : m_mocks) {
            replay(o);
        }
    }
    
    private void verifyMocks() {
        for (Object o : m_mocks) {
            verify(o);
        }
        for (Object o : m_mocks) {
            reset(o);
        }
    }
    
    private static List<ObjectError> getErrorList(Errors errors) {
        return (List<ObjectError>) errors.getAllErrors();
    }
}
