/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.events;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.api.Logger;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.VerticalLayout;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * The Class Event Panel.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@SuppressWarnings("serial")
public abstract class EventPanel extends Panel {

    /** The isNew flag. True, if the group is new. */
    private boolean isNew = false;

    /** The Events Configuration DAO. */
    private EventConfDao eventConfDao;

    /** The Events Proxy. */
    private EventProxy eventProxy;

    /** The Events File. */
    private File eventFile;

    /** The selected Event ID. */
    private Object selectedEventId;

    /** The event table. */
    final EventTable eventTable;

    /** The base event object. */
    final Events baseEventsObject = new Events();

    private static final String EVENT_CONFIG_UPLOAD_URL = "/api/v2/eventconf/upload";

    private static  final String EVENT_CONFIG_UPLOADED_SOURCE_NAMES_URL = "/api/v2/eventconf/sources/names";

    /**
     * Instantiates a new event panel.
     *
     * @param eventConfDao the OpenNMS Events Configuration DAO
     * @param eventProxy the OpenNMS Events Proxy
     * @param eventFile the events file
     * @param events the OpenNMS events object
     * @param logger the logger object
     */
    public EventPanel(final EventConfDao eventConfDao, final EventProxy eventProxy, final File eventFile, final Events events, final Logger logger) {

        if (eventProxy == null) {
            throw new RuntimeException("eventProxy cannot be null.");
        }

        if (eventConfDao == null) {
            throw new RuntimeException("eventConfDao cannot be null.");
        }

        this.eventConfDao = eventConfDao;
        this.eventProxy = eventProxy;
        this.eventFile = eventFile;

        setCaption("Events");
        addStyleName("light");

        baseEventsObject.setGlobal(events.getGlobal());
        baseEventsObject.setEventFiles(events.getEventFiles());

        final HorizontalLayout topToolbar = new HorizontalLayout();
        topToolbar.addComponent(new Button("Save Events File", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                processEvents(logger);
            }
        }));
        topToolbar.addComponent(new Button("Cancel", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                logger.info("Event processing has been canceled");
                cancel();
            }
        }));

        eventTable = new EventTable(events.getEvents());

        final EventForm eventForm = new EventForm();
        eventForm.setVisible(false);

        eventTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (eventForm.isVisible() && !eventForm.isReadOnly()) {
                    eventTable.select(selectedEventId);
                    Notification.show("An event seems to be being edited.\nPlease save or cancel your current changes.", Notification.Type.WARNING_MESSAGE);
                } else {
                    Object eventId = eventTable.getValue();
                    if (eventId != null) {
                        selectedEventId = eventId;
                        eventForm.setEvent(eventTable.getEvent(eventId));
                    }
                    eventForm.setReadOnly(true);
                    eventForm.setVisible(eventId != null);
                }
            }
        });

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(topToolbar);
        mainLayout.addComponent(eventTable);
        mainLayout.addComponent(eventForm);
        mainLayout.setComponentAlignment(topToolbar, Alignment.MIDDLE_RIGHT);
        setContent(mainLayout);
    }

    /**
     * Sets the value of the ifNew flag.
     *
     * @param isNew true, if the group is new.
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Cancel.
     */
    public abstract void cancel();

    /**
     * Success.
     */
    public abstract void success();

    /**
     * Failure.
     *
     * @param reason the reason
     */
    public abstract void failure(String reason);

    /**
     * Process events.
     *
     * @param logger the logger
     */
    public void processEvents(final Logger logger) {
        String fileName = eventFile.getName().replaceFirst("\\.xml$", "");
        List<String> names = this.getUploadedSourceNames(logger);
        if (names.contains(fileName)) {
            ConfirmDialog.show(getUI(),
                               "Are you sure?",
                               "Do you really want to override all existing events for this event source?\nAll current information will be lost.",
                               "Yes",
                               "No",
                               new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        validateFile(eventFile, logger);
                    }
                }
            });
        } else {
            validateFile(eventFile, logger);
        }
    }

    /**
     * Validate file.
     *
     * @param file the file
     * @param logger the logger
     */
    private void validateFile(final File file, final Logger logger) {
        int eventCount = 0;
        for (org.opennms.netmgt.xml.eventconf.Event e : eventTable.getOnmsEvents()) {
            if (eventConfDao.findByUei(e.getUei()) != null)
                eventCount++;
        }
        if (eventCount == 0) {
            saveFile(file, logger);
        } else {
            ConfirmDialog.show(getUI(),
                               "Are you sure?",
                               eventCount + " of the new events are already on the configuration files.\nIf you click 'Yes', the existing definitions are going to be ignored.",
                               "Yes",
                               "No",
                               new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        saveFile(file, logger);
                    }
                }
            });
        }
    }

    /**
     * Save file.
     *
     * @param file the file
     * @param logger the logger
     */
    private void saveFile(final File file, final Logger logger) {
        try {
            // Updating the base events object with the new events set.
            baseEventsObject.setEvents(eventTable.getOnmsEvents());
            // Normalize the Event Content (required to avoid marshaling problems)
            // TODO Are other normalizations required ?
            for (org.opennms.netmgt.xml.eventconf.Event event : baseEventsObject.getEvents()) {
                logger.debug("Normalizing event " + event.getUei());
                final AlarmData ad = event.getAlarmData();
                if (ad != null && (ad.getReductionKey() == null || ad.getReductionKey().trim().isEmpty() || ad.getAlarmType() == null || ad.getAlarmType() == 0)) {
                    event.setAlarmData(null);
                }
                final Mask m = event.getMask();
                if (m != null && m.getMaskelements().isEmpty()) {
                    event.setMask(null);
                }
            }
            boolean response = uploadFileToApi(baseEventsObject, file, logger);
            if (response) {
                EventBuilder eb = new EventBuilder(EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI, "WebUI");
                eventProxy.send(eb.getEvent());
                logger.info("The event's configuration reload operation is being performed.");
                success();
            } else {
                final String message = "Failed to upload event source file.";
                logger.error(message);
                failure(message);
            }
        } catch (Exception e) {
            logger.error(e.getClass() + ": " + (e.getMessage() == null ? "[No Details]" : e.getMessage()));
            if (e.getMessage() == null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                logger.error(sw.toString());
            }
            failure(e.getMessage());
        }
    }

    /**
     * Save events.
     *
     * @param events the events
     * @param eventFile the events file
     * @param logger the logger
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void saveEvents(final Events events, final File eventFile, final Logger logger) throws IOException {
        logger.info("Saving XML data into " + eventFile);
        FileWriter writer = new FileWriter(eventFile);
        JaxbUtils.marshal(events, writer);
        writer.close();
    }

    private boolean uploadFileToApi(final Events events, final File eventFile, final Logger logger) {
        logger.info("Saving event source file to database.");
        // Marshal the Events object into a byte array
        byte[] eventData;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JaxbUtils.marshal(events, new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            eventData = outputStream.toByteArray();
        } catch (IOException e) {
            logger.error("Failed to serialize Events object: " + e.getMessage());
            throw new RuntimeException("Error serializing Events object", e);
        }
        String apiUrl = getApiUrl(EVENT_CONFIG_UPLOAD_URL);
        HttpPost uploadRequest = new HttpPost(apiUrl);
        uploadRequest.setHeader("Cookie", getCookie(logger));
        HttpEntity multipart = MultipartEntityBuilder.create()
                .addBinaryBody("upload", eventData, ContentType.APPLICATION_XML, eventFile.getName())
                .build();
        uploadRequest.setEntity(multipart);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(uploadRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (statusCode == HttpStatus.SC_OK) {
                logger.info("File successfully uploaded via API: " + statusCode);
                logger.debug("Response: " + responseBody);
                return true;
            } else {
                logger.error("Upload failed: " + statusCode);
                logger.debug("Response: " + responseBody);
                return false;
            }
        } catch (IOException e) {
            logger.error("I/O error during file upload: " + e.getMessage());
            throw new RuntimeException("I/O error during file upload", e);
        } catch (Exception e) {
            logger.error("Unexpected error during file upload: " + e.getMessage());
            throw new RuntimeException("Unexpected error during file upload", e);
        }
    }

    private String getApiUrl(final String api) {
        VaadinServletRequest vaadinServletRequest = (VaadinServletRequest) VaadinService.getCurrentRequest();
        HttpServletRequest httpRequest = vaadinServletRequest.getHttpServletRequest();
        String baseUrl = httpRequest.getRequestURL().toString()
                .replace(httpRequest.getRequestURI(), "")
                + httpRequest.getContextPath();
        return baseUrl + api;
    }

    private String getCookie(final Logger logger) {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null) {
            logger.error("No current request found");
            throw new RuntimeException("No current request found");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("JSESSIONID".equalsIgnoreCase(c.getName())) {
                    return "JSESSIONID=" + c.getValue();
                }
            }
        }
        logger.error("JSESSIONID cookie not found");
        throw new RuntimeException("No JSESSIONID cookie found");
    }

    private List<String> getUploadedSourceNames(final Logger logger) {
        String apiUrl = getApiUrl(EVENT_CONFIG_UPLOADED_SOURCE_NAMES_URL);
        HttpGet getRequest = new HttpGet(apiUrl);
        getRequest.setHeader("Cookie", getCookie(logger));
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(getRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (statusCode == HttpStatus.SC_OK) {
                logger.info("Successfully fetched uploaded source names");
                ObjectMapper mapper = new ObjectMapper();
                return Arrays.asList(mapper.readValue(responseBody, String[].class));
            } else {
                logger.error("Error getting uploaded source names. HTTP status: " + statusCode);
                throw new RuntimeException("Failed to fetch uploaded source names. HTTP " + statusCode);
            }
        } catch (IOException e) {
            logger.error("I/O error fetching uploaded source names: " + e.getMessage());
            throw new RuntimeException("I/O error fetching uploaded source names", e);
        } catch (Exception e) {
            logger.error("Unexpected error fetching uploaded source names: " + e.getMessage());
            throw new RuntimeException("Unexpected error fetching uploaded source names", e);
        }
    }
}
