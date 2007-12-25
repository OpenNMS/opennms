package org.opennms.netmgt.eventd.datablock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.opennms.netmgt.dao.CastorObjectRetrievalFailureException;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

public class DefaultEventConfDao implements EventConfDao, InitializingBean {

    private File m_configFile;

    private Set<String> m_secureTags = new HashSet<String>();

    private List<EventConf> m_eventConfs = new LinkedList<EventConf>();

    public class EventConf {
    }

    public void setConfigFile(File configFile) {
        m_configFile = configFile;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_configFile, "The configFile must be set");

        loadConfigFile(m_configFile);
    }



    private void loadConfigFile(File configFile) {
        FileReader rdr = null;
        try {
            rdr = new FileReader(configFile);
            Events events = (Events)Unmarshaller.unmarshal(Events.class, rdr);
            processConfigFile(configFile, events);
        } catch (FileNotFoundException e) {
            throw new DataAccessResourceFailureException("Unable to read eventConf config file: "+configFile, e);
        } catch (MarshalException e) {
            throw new CastorDataAccessFailureException("Trouble parsing eventConf config File: "+configFile, e);
        } catch (ValidationException e) {
            throw new CastorObjectRetrievalFailureException("Invalid eventConf config File: "+configFile, e);
        } finally {
            IOUtils.closeQuietly(rdr);
        }

    }

    private void processConfigFile(File configFile, Events events) {

        m_secureTags.addAll(events.getGlobal().getSecurity().getDoNotOverrideCollection());

        processEvents(events.getEvent());

        processEventFiles(configFile, events.getEventFile());

    }

    private void processEventFiles(File configFile, String[] configFileNames) {
        for (String configFileName : configFileNames) {
            File config = new File(configFileName);
            if (!config.isAbsolute()) {
                config = new File(configFile.getParentFile(), configFileName);
            }
            loadConfigFile(config);
        }

    }

    private void processEvents(Event[] events) {
        for (Event event : events) {
            m_eventConfs.add(createEventConf(event));
        }

    }

    private EventConf createEventConf(Event event) {
        return new EventConf();
    }

    public Event getMatchingEventConf(org.opennms.netmgt.xml.event.Event trapEvent) {
        Event e = new Event();
        e.setUei("uei.opennms.org/generic/traps/SNMP_Cold_Start");
        return e;
    }

}
