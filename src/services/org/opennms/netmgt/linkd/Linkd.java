package org.opennms.netmgt.linkd;

import java.io.*;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.*;
import org.opennms.core.fiber.*;

public class Linkd implements PausableFiber {

	private static final String LOG4J_CATEGORY = "OpenNMS.Linkd";

	private static final Linkd m_singleton = new Linkd();

	private int m_status = 0;

	private int initial_sleep_time = 900000;

	private int sleep_time = 30000;

	private String categorylevel = "WARN";

	private Process p = null;

	private Linkd() {
		m_status = START_PENDING;
	}

	public static Linkd getInstance() {
		return m_singleton;
	}

	public synchronized void init() {

		ThreadCategory.setPrefix(LOG4J_CATEGORY);

		Category log = ThreadCategory.getInstance();
		if (log.getLevel() != null) {
			categorylevel = log.getLevel().toString();
		}
		if (log.isEnabledFor(Priority.WARN))
			log.warn("init: Category Level Set to " + categorylevel);

		try {
			LinkdConfigFactory.init();
		} catch (ClassNotFoundException ex) {
			if (log.isEnabledFor(Priority.ERROR))
				log.error("init: Failed to load linkd configuration file ", ex);
			return;
		}
		catch (MarshalException ex) {
			if (log.isEnabledFor(Priority.ERROR))
				log.error("init: Failed to load linkd configuration file ", ex);
			return;
		} catch (ValidationException ex) {
			if (log.isEnabledFor(Priority.ERROR))
				log.error("init: Failed to load linkd configuration file ", ex);
			return;
		} catch (IOException ex) {
			if (log.isEnabledFor(Priority.ERROR))
				log.error("init: Failed to load linkd configuration file ", ex);
			return;
		}

		try {
			initial_sleep_time = LinkdConfigFactory.getInstance()
					.getInitialSleepTime();
			sleep_time = LinkdConfigFactory.getInstance().getSleepTime();
		} catch (Throwable t) {
			if (log.isEnabledFor(Priority.WARN)) {
				log.warn("init: Failed to load linkd configuration file " + t);
			}
		}

		if (log.isEnabledFor(Priority.INFO)) {
			log.info("init: LINKD CONFIGURATION INITIALIZED");
		}

	}

	public synchronized void start() {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance();

		if (m_status == START_PENDING) {
			m_status = STARTING;

			if (log.isEnabledFor(Priority.INFO)) {
				log.info("start: Linkd starting");
			}

			try {

				if (log.isEnabledFor(Priority.INFO))
					log.info("start: discoverLink Start");

				p = Runtime.getRuntime().exec(
						"perl /opt/OpenNMS/bin/discoverLink.pl "
								+ initial_sleep_time + " " + sleep_time + " "
								+ categorylevel);
				if (log.isEnabledFor(Priority.INFO))
					log.info("start: discoverLink Started");
			} catch (IOException ex2) {
				log.info("discoverLink IOException " + ex2);
				m_status = STOPPED;
				return;
			}

		}

		m_status = RUNNING;

		if (log.isEnabledFor(Priority.INFO)) {
			log.info("start: Linkd Running");
		}

	}

	public synchronized void stop() {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance();
		m_status = STOP_PENDING;
		if (log.isEnabledFor(Priority.INFO)) {
			log.info("stop: Linkd Stopping");
		}
		//		process.setRun(false);
		//		process.interrupt();
		p.destroy();
		m_status = STOPPED;
		if (log.isEnabledFor(Priority.INFO)) {
			log.info("stop: Linkd Stopped");
		}

	}

	public synchronized void reload() throws IOException {
	}

	public String getName() {
		return "OpenNMS.Linkd";
	}

	public int getStatus() {
		return m_status;
	}

	public void resume() {
	}

	public void pause() {
	}

}