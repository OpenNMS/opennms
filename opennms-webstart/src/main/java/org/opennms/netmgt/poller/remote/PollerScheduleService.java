package org.opennms.netmgt.poller.remote;

import javax.swing.table.TableModel;

import org.quartz.Scheduler;


public interface PollerScheduleService {
	
	// use case 1: view schedule
	TableModel getScheduleTableModel();

	// use case 2: schedule services
	void scheduleServicePolls(Scheduler scheduler) throws Exception;
	
	

}
