package edu.ncsu.pdgrenon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;


public class Collector {

	private List<LogMessage> m_messages = new ArrayList<LogMessage>();

	public void addLog(String logMessage) {
		m_messages.add(new LogMessage(logMessage));
	}
	public Date getStartTime() {
		LogMessage firstValidLine = getFirstValidLogMessage();
		return firstValidLine.getDate();	
			
	}
	public LogMessage getFirstValidLogMessage (){
		for(LogMessage logMessage : m_messages ) {
			if(logMessage.isBeginMessage()){
				return logMessage;
			}
		}
		return null;
	}
	public LogMessage getLastValidLogMessage () {
		for(ListIterator<LogMessage> it = m_messages.listIterator(m_messages.size()); it.hasPrevious() ; ) { 
			LogMessage logMessage = it.previous();
			if(logMessage.isEndMessage()){
				return logMessage;
			}
		}
		return null;
	}
	public Date getEndTime() {
		LogMessage logMessage = getLastValidLogMessage();
		return logMessage.getDate();
	}
	public long getDuration() {
		return this.getEndTime().getTime()-this.getStartTime().getTime();
	}
	public int getServiceCount() {
		Set<String> services = new HashSet<String>();
		for(LogMessage logMessage : m_messages) {
			services.add(logMessage.getServiceID());
		}
		return services.size();
	}
	public String sortAndPrintServiceCount() {
		int errorCount = 0;
		int beginCount = 0;
		int endCount = 0;
		int persistCount = 0;
		int failure = 0;
		for(LogMessage logMessage : m_messages) {
			if(logMessage.isBeginMessage() && !logMessage.isPersistMessage()) {
				beginCount +=1;
			}else if (logMessage.isCollectorEndMessage() && !logMessage.isPersistMessage()) {
				endCount +=1;
			}else if(logMessage.isPersistMessage()) {
				persistCount +=1;
			}else if(logMessage.isErrorMessage()) {
				errorCount+=1;
			}else{
				failure +=1;
			}
		}
		System.out.println("Beginning collecting messages during collection: " + beginCount + " Ending collecting messages during collection:  " + endCount +
				" Persisting messages during collection: " + persistCount + " Error messages during collection: " + errorCount+ " failures: " + failure);
		return "Beginning collecting messages during collection: " + beginCount + " Ending collecting messages during collection:  " + endCount +
				" Persisting messages during collection: " + persistCount + " Error messages during collection: " + errorCount + " failures: " + failure;
	}
	public int getThreadCount() {
		Set<String> threads = new HashSet<String>();
		for(LogMessage logMessage : m_messages) {
			threads.add(logMessage.getThread());
		}
		return threads.size();
	}
	public int collectionsPerService(String serviceID) {
		int count = 0;
		for(LogMessage logMessage : m_messages) {
			if(logMessage.isCollectorBeginMessage() && serviceID.equals(logMessage.getServiceID())) {
				count += 1;
			}
		}
		return count;
	}
	public long averageCollectionTimePerService(String serviceID) {
		long [] begin = new long [this.collectionsPerService(serviceID)];
		long [] end = new long [this.collectionsPerService(serviceID)];
		int i = 0;
		long added = 0;
		long average = 0;
		long [] elapsed = new long[this.collectionsPerService(serviceID)];
		for(LogMessage logMessage : m_messages) {
			if(serviceID.equals(logMessage.getServiceID()) && logMessage.isCollectorBeginMessage()) {
				begin[i] = logMessage.getDate().getTime();
			}
			if(serviceID.equals(logMessage.getServiceID()) && logMessage.isCollectorEndMessage()) {
				end[i] = logMessage.getDate().getTime();
			}		
		}
		for(i = 0 ; i < begin.length && i < end.length ; i++) {
			elapsed[ i] =  end[ i] - begin[i];
			added += elapsed [i];
		}
		if(elapsed.length == 0) {
			average = 0;
		}else if(end[0]==0){
			average = 0;
		}else {
			average = added / elapsed.length;
		}
		return average;
	}
	public long testTotalCollectionTimePerService(String serviceID) {
		long [] begin = new long [this.collectionsPerService(serviceID)];
		long [] end = new long [this.collectionsPerService(serviceID)];
		int i = 0;
		long added = 0;
		long [] elapsed = new long[this.collectionsPerService(serviceID)];
		for(LogMessage logMessage : m_messages) {
			if(serviceID.equals(logMessage.getServiceID()) && logMessage.isCollectorBeginMessage()) {
				begin[i] = logMessage.getDate().getTime();
			}
			if(serviceID.equals(logMessage.getServiceID()) && logMessage.isCollectorEndMessage()) {
				end[i] = logMessage.getDate().getTime();
			}		
		}
		for(i = 0 ; i < begin.length && i < end.length ; i++) {
			elapsed[ i] =  end[ i] - begin[i];
			added += elapsed [i];
		}
		if(added<0){
			added = -1;
		}
		return added;
	}
	public void readLogMessagesFromFile(String fileName) throws IOException {
		File logFile = new File(fileName);
		BufferedReader r = new BufferedReader(new FileReader(logFile));	
		String logMessage = r.readLine();
		while(logMessage != null){
			this.addLog(logMessage);
			logMessage = r.readLine();
		}
		r.close();
	}
	public int [] printGlobalStats() {
		return null;
	}
}
