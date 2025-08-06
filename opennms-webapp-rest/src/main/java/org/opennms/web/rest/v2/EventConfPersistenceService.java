package org.opennms.web.rest.v2;

import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.events.EventConfSourceMetadataDto;
import org.opennms.netmgt.model.events.ParsedEventEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class EventConfPersistenceService {

    @Autowired
    private EventConfSourceDao eventConfSourceDao;

    @Autowired
    private EventConfEventDao eventConfEventDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistEventConfFile(final List<ParsedEventEntry> events, final EventConfSourceMetadataDto eventConfSourceMetadataDto
    ) {
        EventConfSource source = createOrUpdateSource(eventConfSourceMetadataDto);
        eventConfEventDao.deleteBySourceId(source.getId());
        saveEvents(source, events, eventConfSourceMetadataDto.getUsername(), eventConfSourceMetadataDto.getNow());
    }

    private EventConfSource createOrUpdateSource(final EventConfSourceMetadataDto eventConfSourceMetadataDto) {
        EventConfSource source = eventConfSourceDao.findByName(eventConfSourceMetadataDto.getFilename());
        if (source == null) {
            source = new EventConfSource();
            source.setCreatedTime(eventConfSourceMetadataDto.getNow());
        }

        source.setName(eventConfSourceMetadataDto.getFilename());
        source.setFileOrder(eventConfSourceMetadataDto.getFileOrder());
        source.setEventCount(eventConfSourceMetadataDto.getEventCount());
        source.setEnabled(true);
        source.setUploadedBy(eventConfSourceMetadataDto.getUsername());
        source.setLastModified(eventConfSourceMetadataDto.getNow());
        source.setVendor(eventConfSourceMetadataDto.getVendor());
        source.setDescription(eventConfSourceMetadataDto.getDescription());

        eventConfSourceDao.saveOrUpdate(source);
        return eventConfSourceDao.get(source.getId());
    }

    private void saveEvents(EventConfSource source, List<ParsedEventEntry> events, String username, Date now) {
        List<EventConfEvent> eventEntities = events.stream().map(parsed -> {
            EventConfEvent event = new EventConfEvent();
            event.setSource(source);
            event.setUei(parsed.getUei());
            event.setEventLabel(parsed.getEventLabel());
            event.setDescription(parsed.getDescription());
            event.setEnabled(parsed.getEnabled());
            event.setXmlContent(parsed.getXmlContent());
            event.setCreatedTime(now);
            event.setLastModified(now);
            event.setModifiedBy(username);
            return event;
        }).toList();

        eventEntities.forEach(eventConfEventDao::save);
    }
}
