package org.opennms.netmgt.importer;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.importer.operations.AbstractSaveOrUpdateOperation;
import org.opennms.netmgt.importer.operations.ImportOperation;
import org.opennms.netmgt.importer.operations.ImportOperationsManager;
import org.opennms.netmgt.importer.operations.ImportStatistics;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

public class ImporterService extends BaseImporter implements InitializingBean, DisposableBean, EventListener {
	
	public static final String NAME = "ModelImporter";

	public static final String RELOAD_IMPORT_UEI = "uei.opennms.org/internal/importer/reloadImport";
	
	public static final String IMPORT_STARTED_UEI = "uei.opennms.org/internal/importer/importStarted";
	
	public static final String IMPORT_SUCCESSFUL_UEI = "uei.opennms.org/internal/importer/importSuccessful";
	public static final String PARM_IMPORT_STATS = "importStats";

	public static final String IMPORT_FAILED_UEI = "uei.opennms.org/internal/importer/importFailed";
	public static final String PARM_FAILURE_MESSAGE = "failureMessage";
    
    private Resource m_importResource;
	private EventIpcManager m_eventManager;
	private ImporterStats m_stats;

    public void doImport() {
    	ThreadCategory.setPrefix(NAME);

        try {
        	sendImportStarted();
            m_stats = new ImporterStats();
			importModelFromResource(m_importResource, m_stats);
            log().info("Finished Importing: "+m_stats);
            sendImportSuccessful(m_stats);
        } catch (IOException e) {
            String msg = "IOException importing "+m_importResource;
			log().error(msg, e);
            sendImportFailed(msg+": "+e.getMessage());
        } catch (ModelImportException e) {
            String msg = "Error parsing import data from "+m_importResource;
			log().error(msg, e);
            sendImportFailed(msg+": "+e.getMessage());
        }
    }
    
    public String getStats() { return (m_stats == null ? "No Stats Availabile" : m_stats.toString()); }

    private void sendImportSuccessful(ImporterStats stats) {
    	Event e = createEvent(IMPORT_SUCCESSFUL_UEI, PARM_IMPORT_STATS, stats.toString());
		m_eventManager.sendNow(e);
	}
    
    private Event createEvent(String uei) {
    	Event e = new Event();
    	e.setSource(NAME);
    	e.setCreationTime(EventConstants.formatToString(new Date()));
    	e.setTime(EventConstants.formatToString(new Date()));
    	e.setUei(uei);
    	
    	return e;
    }
    
    private Event createEvent(String uei, String parmName, String value) {
		Event e = createEvent(uei);
		Parms parms = new Parms();
		e.setParms(parms);
		
		Parm parm = new Parm();
		parm.setParmName(parmName);
		parms.addParm(parm);
		
		Value val = new Value();
		val.setContent(value);
		parm.setValue(val);
		
		return e;
    	
    }

	private void sendImportFailed(String msg) {
		Event e = createEvent(IMPORT_FAILED_UEI, PARM_FAILURE_MESSAGE, msg);
		m_eventManager.sendNow(e);
	}

	private void sendImportStarted() {
		Event e = createEvent(IMPORT_STARTED_UEI);
		m_eventManager.sendNow(e);
	}

    public void setImportResource(Resource resource) {
        m_importResource = resource;
    }

	public EventIpcManager getEventManager() {
	    return m_eventManager;
	}

	public void setEventManager(EventIpcManager eventManager) {
		m_eventManager = eventManager;
	}

	protected ImportOperationsManager createImportOperationsManager(Map assetNumbersToNodes, ImportStatistics stats) {
		ImportOperationsManager opsMgr = super.createImportOperationsManager(assetNumbersToNodes, stats);
		opsMgr.setEventMgr(m_eventManager);
		return opsMgr;
	}

	public void afterPropertiesSet() throws Exception {
		m_eventManager.addEventListener(this, RELOAD_IMPORT_UEI);
	}

	public void destroy() throws Exception {
		m_eventManager.removeEventListener(this, RELOAD_IMPORT_UEI);
		
	}

	public String getName() {
		return NAME;
	}

	public void onEvent(Event e) {

		if (!RELOAD_IMPORT_UEI.equals(e.getUei())) return;
		
		doImport();
	}
	
	public class ImporterStats implements ImportStatistics {

		private Duration m_importDuration = new Duration("Importing");
		private Duration m_auditDuration = new Duration("Auditing");
		private Duration m_loadingDuration = new Duration("Loading");
		private Duration m_processingDuration = new Duration("Processing");
		private Duration m_preprocessingDuration = new Duration("Scanning");
		private Duration m_relateDuration = new Duration("Relating");
		private WorkEffort m_preprocessingEffort = new WorkEffort("Scan Effort");
		private WorkEffort m_processingEffort = new WorkEffort("Write Effort");
		private WorkEffort m_eventEffort = new WorkEffort("Event Sending Effort");
		private int m_deleteCount;
		private int m_insertCount;
		private int m_updateCount;
		private int m_eventCount;

		public void beginProcessingOps() {
			m_processingDuration.start();
		}

		public void finishProcessingOps() {
			m_processingDuration.end();
		}

		public void beginPreprocessingOps() {
			m_preprocessingDuration.start();
		}

		public void finishPreprocessingOps() {
			m_preprocessingDuration.end();
		}

		public void beginPreprocessing(ImportOperation oper) {
			if (oper instanceof AbstractSaveOrUpdateOperation) {
				m_preprocessingEffort.begin();
			}
		}

		public void finishPreprocessing(ImportOperation oper) {
			if (oper instanceof AbstractSaveOrUpdateOperation) {
				m_preprocessingEffort.end();
			}
		}

		public void beginPersisting(ImportOperation oper) {
			m_processingEffort.begin();
			
		}

		public void finishPersisting(ImportOperation oper) {
			m_processingEffort.end();
		}

		public void beginSendingEvents(ImportOperation oper, List events) {
			if (events != null) m_eventCount += events.size();
			m_eventEffort.begin();
		}

		public void finishSendingEvents(ImportOperation oper, List events) {
			m_eventEffort.end();
		}

		public void beginLoadingResource(Resource resource) {
			m_loadingDuration.setName("Loading Resource: "+resource);
			m_loadingDuration.start();
		}

		public void finishLoadingResource(Resource resource) {
			m_loadingDuration.end();
		}

		public void beginImporting() {
			m_importDuration.start();
		}

		public void finishImporting() {
			m_importDuration.end();
		}

		public void beginAuditNodes() {
			m_auditDuration.start();
		}

		public void finishAuditNodes() {
			m_auditDuration.end();
		}
		
		public void setDeleteCount(int deleteCount) {
			m_deleteCount = deleteCount;
		}

		public void setInsertCount(int insertCount) {
			m_insertCount = insertCount;
		}

		public void setUpdateCount(int updateCount) {
			m_updateCount = updateCount;
		}

		public void beginRelateNodes() {
			m_relateDuration.start();
		}

		public void finishRelateNodes() {
			m_relateDuration.end();
		}
		
		public String toString() {
			StringBuffer stats = new StringBuffer();
			stats.append("Deletes: ").append(m_deleteCount).append(' ');
			stats.append("Updates: ").append(m_updateCount).append(' ');
			stats.append("Inserts: ").append(m_insertCount).append('\n');
			stats.append(m_importDuration).append(' ');
			stats.append(m_loadingDuration).append(' ');
			stats.append(m_auditDuration).append('\n');
			stats.append(m_preprocessingDuration).append(' ');
			stats.append(m_processingDuration).append(' ');
			stats.append(m_relateDuration).append(' ');
			stats.append(m_preprocessingEffort).append(' ');
			stats.append(m_processingEffort).append(' ');
			stats.append(m_eventEffort).append(' ');
			if (m_eventCount > 0) {
				stats.append("Avg ").append((double)m_eventEffort.getTotalTime()/(double)m_eventCount).append(" ms per event");
			}
			
			return stats.toString();
		}

	}
		
	public class Duration {

		private String m_name = null;
		private long m_start = -1L;
		private long m_end = -1L;
		
		public Duration() {
			this(null);
		}

		public Duration(String name) {
			m_name = name;
		}
		
		public void setName(String name) {
			m_name = name;
		}

		public void start() {
			m_start = System.currentTimeMillis();
		}

		public void end() {
			m_end = System.currentTimeMillis();
		}
		
		public long getLength() {
			if (m_start == -1L) return 0L;
			long end = (m_end == -1L ? System.currentTimeMillis() : m_end);
			return end - m_start;
		}

		public String toString() {
			return (m_name == null ? "" : m_name+": ")+(m_start == -1L ? "has not begun": elapsedTime());
		}

		private String elapsedTime() {

			long duration = getLength();

			long hours = duration / 3600000L;
			duration = duration % 3600000L;
			long mins = duration / 60000L;
			duration = duration % 60000L;
			long secs = duration / 1000L;
			long millis = duration % 1000L;

			StringBuffer elapsed = new StringBuffer();
			if (hours > 0)
				elapsed.append(hours).append("h ");
			if (mins > 0)
				elapsed.append(mins).append("m ");
			if (secs > 0)
				elapsed.append(secs).append("s ");
			if (millis > 0)
				elapsed.append(millis).append("ms");

			return elapsed.toString();

		}

	}

	public class WorkEffort {
		
		private String m_name;
		private long m_totalTime;
		private long m_sectionCount;
		private ThreadLocal m_pendingSection = new ThreadLocal();
		
		public WorkEffort(String name) {
			m_name = name;
		}

		public void begin() {
			Duration pending = new Duration();
			pending.start();
			m_pendingSection.set(pending);
		}

		public void end() {
			Duration pending = (Duration)m_pendingSection.get();
			m_sectionCount++;
			m_totalTime += pending.getLength();
		}
		
		public long getTotalTime() {
			return m_totalTime;
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("Total ").append(m_name).append(": ");
			buf.append((double)m_totalTime/(double)1000L).append(" thread-seconds ");
			if (m_sectionCount > 0) {
				buf.append("Avg ").append(m_name).append(": ");
				buf.append((double)m_totalTime/(double)m_sectionCount).append(" ms per node");
			}
			return buf.toString();
		}

	}



}
