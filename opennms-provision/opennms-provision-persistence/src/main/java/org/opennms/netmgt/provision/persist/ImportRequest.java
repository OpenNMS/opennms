/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import java.util.Objects;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

/**
 * Object to define the requisition import.
 * It is used for creating "request requisition import" events.
 *
 * @autor mvrueden
 */
public class ImportRequest {

    private static final Logger LOG = LoggerFactory.getLogger(ImportRequest.class);

    private final String source;
    private String url;
    private String foreignSource;
    private String rescanExisting;

    public ImportRequest(Event event, String systemRescanParameter) {
        this.source = event.getSource();
        this.url = EventUtils.getParm(event, EventConstants.PARM_IMPORT_RESOURCE);
        this.foreignSource = EventUtils.getParm(event, EventConstants.PARM_IMPORT_FOREIGN_SOURCE);
        this.rescanExisting = EventUtils.getParm(event, EventConstants.PARM_IMPORT_RESCAN_EXISTING, systemRescanParameter);
    }

    public ImportRequest(String source) {
        Objects.requireNonNull(source);
        this.source = source;
    }

    public ImportRequest(Object source) {
        Objects.requireNonNull(source);
        this.source = source.getClass().getSimpleName();
    }

    public String getUrl() {
        return url;
    }

    public ImportRequest withUrl(String url) {
        Objects.requireNonNull(url);
        this.url = url;
        return this;
    }

    public ImportRequest withForeignSource(String foreignSource) {
        Objects.requireNonNull(foreignSource);
        this.foreignSource = foreignSource;
        return this;
    }

    public String getForeignSource() {
        return foreignSource;
    }

    public ImportRequest withRescanExisting(String rescanExisting) {
        Objects.requireNonNull(rescanExisting);
        this.rescanExisting = rescanExisting;
        return this;
    }

    public String getRescanExisting() {
        return rescanExisting;
    }

    public Event toReloadEvent() {
        if (!isValid()) {
            throw new IllegalStateException("Cannot create event to reload requisition");
        }
        if (foreignSource != null && url != null) {
            LOG.warn("Url and foreignSource are both defined. Using foreignSource '{}' to import requisition. Will ignore url '{}'", foreignSource, url);
        }
        return addEventParameters(new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, source)).getEvent();
    }

    public EventBuilder addEventParameters(EventBuilder bldr) {
        if (foreignSource != null) {
            bldr.addParam(EventConstants.PARM_IMPORT_FOREIGN_SOURCE, foreignSource);
        }
        if (rescanExisting != null) {
            bldr.addParam(EventConstants.PARM_IMPORT_RESCAN_EXISTING, rescanExisting);
        }
        if (url != null) {
            bldr.addParam(EventConstants.PARM_IMPORT_RESOURCE, url);
        }
        return bldr;
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(foreignSource) || !Strings.isNullOrEmpty(url);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("resource", url)
                .add("foreignSource", foreignSource)
                .add("rescanExisting", rescanExisting)
                .toString();
    }
}
