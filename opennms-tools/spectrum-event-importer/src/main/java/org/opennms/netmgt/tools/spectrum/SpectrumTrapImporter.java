/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.tools.spectrum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.Varbindsdecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mchange.v2.log.LogUtils;

/**
 * @author Jeff Gehlbach <jeffg@opennms.org>
 * @author OpenNMS <http://www.opennms.org/>
 *
 */
public class SpectrumTrapImporter {
	
    private static final Logger LOG = LoggerFactory.getLogger(SpectrumTrapImporter.class);

    private List<AlertMapping> m_alertMappings;
    private List<EventDisposition> m_eventDispositions;
    private Map<String,EventFormat> m_eventFormats;
    private String m_modelTypeAssetField;
    
    /**
     * Something like uei.opennms.org/import/Spectrum/
     */
    private String m_baseUei = null;
    private Resource m_customEventsDir = null;
    private PrintWriter m_outputWriter = null;
    private String m_reductionKeyBody = null;
    
    private SpectrumUtils m_utils;
    private Map<String,String> m_alarmCreators;
    
    private static final String COMMAND_HELP =
        "Reads a set of Spectrum event definitions, as described by a directory tree\n" +
        "with the following general layout:\n\n" +
        " ./AlertMap\n" +
        " ./EventDisp\n" + 
        " ./CsEvFormat/EventNNNNNNNN...\n" +
        " ./CsEvFormat/EventTables/enumFoo...\n\n" +
        "Produces XML describing a corresponding set of OpenNMS event definitions.\n\n" +
        "Syntax: java -jar spectrum-trap-importer.jar [--dir <input directory>] \\\n" +
        "                  --model-type-asset-field    OpenNMS asset field containing model type\n" +
        "                  --base-uei                  Directory containing Spectrum event definitions\n" +
        "                  --output-file               Filename for output (defaults to stdout)\n" +
        "                  --key                       Body of clear- / reduction-key (advanced)\n";
    
    public static void main(String[] argv) {
        SpectrumTrapImporter importer = new SpectrumTrapImporter();
        importer.configureArgs(argv);
        
        try {
            importer.afterPropertiesSet();
        } catch (Throwable e) {
            importer.printHelp("Fatal exception caught at startup: " + e.getMessage());
            System.exit(1);
        }
        
        Events events = new Events();
        try {
            events = importer.makeEvents();
        } catch (IOException e) {
            importer.printHelp("Fatal exception caught at runtime: " + e.getMessage());
        }
        
        try {
            //events.marshal(importer.getOutputWriter());
            StringWriter sw = new StringWriter();
            events.marshal(sw);
            importer.prettyPrintXML(sw.toString());
        } catch (MarshalException e) {
            importer.printHelp("Fatal exception while marshaling output: " + e.getMessage());
        } catch (ValidationException e) {
            importer.printHelp("Fatal exception while validating output: " + e.getMessage());
        } catch (IOException e) {
            importer.printHelp("Fatal I/O exception: " + e.getMessage(), e);
        } catch (SAXException e) {
            importer.printHelp("Fatal SAX exception: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            importer.printHelp("Fatal parser configuration exception: " + e.getMessage());
        }
    }
    
    private void initialize() throws Exception {
        Resource alertMapResource = new FileSystemResource(m_customEventsDir.getFile().getPath() + File.separator + "AlertMap");
        Resource eventDispResource = new FileSystemResource(m_customEventsDir.getFile().getPath() + File.separator + "EventDisp");
        
        m_alertMappings = new AlertMapReader(alertMapResource).getAlertMappings();
        m_eventDispositions = new EventDispositionReader(eventDispResource).getEventDispositions();
        loadEventFormats();
        
        m_utils = new SpectrumUtils();
        m_utils.setModelTypeAssetField(m_modelTypeAssetField);
        m_alarmCreators = new HashMap<String,String>();
    }
    
    private void loadEventFormats() throws Exception {
        String csEvFormatDirName = m_customEventsDir.getFile().getPath() + File.separator + "CsEvFormat";
        Map<String,EventFormat> formats = new HashMap<String,EventFormat>();
        Map<String,String> unformattedEvents = new LinkedHashMap<String,String>();
        for (AlertMapping mapping : m_alertMappings) {
            if (formats.containsKey(mapping.getEventCode())) {
                LOG.debug("Already have read an event-format for event-code [{}], not loading again", mapping.getEventCode());
                continue;
            }
            String formatFileName = csEvFormatDirName + "/Event" + (mapping.getEventCode().substring(2));
            try {
                EventFormatReader reader = new EventFormatReader(new FileSystemResource(formatFileName));
                formats.put(mapping.getEventCode(), reader.getEventFormat());
            } catch (FileNotFoundException fnfe) {
                unformattedEvents.put(mapping.getEventCode(), mapping.getEventCode());
                continue;
            }
        }
        LOG.info("Loaded {} event-formats from files in [{}]", formats.size(), csEvFormatDirName);
        if (unformattedEvents.keySet().size() > 0) {
            StringBuilder uelBuilder = new StringBuilder("");
            for (String ec : unformattedEvents.keySet()) {
                if (uelBuilder.length() > 0) {
                    uelBuilder.append(" ");
                }
                uelBuilder.append(ec);
            }
            LOG.info("Unable to load an event-format for {} event-codes [{}].  Continuing without them.", unformattedEvents.keySet().size(), uelBuilder.toString());
        }
        m_eventFormats = formats;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (m_baseUei == null) {
            throw new IllegalStateException("The baseUei property must be set");
        }
        if (m_customEventsDir == null) {
            throw new IllegalStateException("The customEventsDir property must be set");
        }
        if (m_modelTypeAssetField == null) {
            throw new IllegalStateException("The modelTypeAssetField property must be set");
        }
        if (m_reductionKeyBody == null) {
            throw new IllegalStateException("The reductionKeyBody property must be set");
        }
        if (m_outputWriter == null) {
            throw new IllegalStateException("The outputStream property must be set");
        }
        
        initialize();
    }
    
    private void configureArgs(String[] argv) {
        Options opts = new Options();
        opts.addOption("d", "dir", true, "Directory where Spectrum custom events are located");
        opts.addOption("t", "model-type-asset-field", true, "Name of asset field containing equivalent of Spectrum model type.  Defaults to 'manufacturer'.");
        opts.addOption("u", "base-uei", true, "Base value for UEI of generated OpenNMS events.  Defaults to 'uei.opennms.org/import/Spectrum'.");
        opts.addOption("f", "output-file", true, "File to which OpenNMS events will be written.  Defaults to standard output.");
        opts.addOption("k", "key", true, "Middle part of reduction- and clear-key, after UEI and before discriminators.  Defaults to '%dpname%:%nodeid%:%interface%'.");
        
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(opts, argv);
            if (cmd.hasOption('d')) {
                m_customEventsDir = new FileSystemResource(cmd.getOptionValue('d'));
            }
            
            if (cmd.hasOption('t')) {
                m_modelTypeAssetField = cmd.getOptionValue('t');
            } else {
                m_modelTypeAssetField = "manufacturer";
            }
            
            if (cmd.hasOption('u')) {
                m_baseUei = cmd.getOptionValue('u');
            } else {
                m_baseUei = "uei.opennms.org/import/Spectrum";
            }
            
            if (cmd.hasOption('f')) {
                m_outputWriter = new PrintWriter(new FileSystemResource(cmd.getOptionValue('f')).getFile());
            } else {
                m_outputWriter = new PrintWriter(System.out);
            }
            
            if (cmd.hasOption('k')) {
                m_reductionKeyBody = cmd.getOptionValue('k');
            } else {
                m_reductionKeyBody = "%dpname%:%nodeid%:%interface%";
            }
        } catch (ParseException pe) {
            printHelp("Failed to parse command line options");
            System.exit(1);
        } catch (FileNotFoundException fnfe) {
            printHelp("Custom events input directory does not seem to exist");
        }
    }
    
    public Events makeEvents() throws IOException {
        Events events = new Events();
        for (AlertMapping mapping : m_alertMappings) {
            for (EventDisposition dispo : m_eventDispositions) {
                if (dispo.getEventCode().equals(mapping.getEventCode())) {
                    Event evt = makeEventConf(mapping, dispo);
                    if (evt == null) {
                        continue;
                    }
                    events.addEvent(evt);
                }
            }
        }
        LOG.debug("Made {} events", events.getEventCollection().size());
        return events;
    }
    
    public Event makeEventConf(AlertMapping mapping, EventDisposition dispo) throws IOException {
        Event evt = new Event();
        evt.setMask(makeEventMask(mapping));
        evt.setUei(makeUei(mapping.getEventCode()));
        evt.setEventLabel(makeEventLabel(mapping));
        evt.setDescr(makeDescr(mapping));
        evt.setLogmsg(makeLogMsg(mapping, dispo));
        evt.setSeverity(makeSeverity(mapping, dispo));
        if (makeAlarmData(mapping, dispo) != null) {
            evt.setAlarmData(makeAlarmData(mapping, dispo));
        }
        evt.setVarbindsdecode(makeVarbindsDecodes(mapping));
        
        if (shouldDiscardEvent(dispo)) {
            LOG.warn("Not creating an OpenNMS event definition corresponding to the following Spectrum event-disposition, because doing so would cause a conflict with an existing alarm-creating event for the same event-code and discriminators: {}. Hand-tweaking the output may be needed to compensate for this omission.", dispo);
            return null;
        }
        return evt;
    }
    
    public Mask makeEventMask(AlertMapping mapping) {
        Mask mask = new Mask();
        
        // Trap-OID
        Maskelement me = new Maskelement();
        me.setMename("id");
        me.setMevalue(new String[] { mapping.getTrapOid() });
        mask.addMaskelement(me);
        
        // Generic-type
        me = new Maskelement();
        me.setMename("generic");
        me.setMevalue(new String[] { mapping.getTrapGenericType() });
        mask.addMaskelement(me);
        
        // Specific-type
        me = new Maskelement();
        me.setMename("specific");
        me.setMevalue(new String[] { mapping.getTrapSpecificType() });
        mask.addMaskelement(me);
        
        return mask;
    }
    
    public String makeUei(String eventCode) {
        StringBuilder ueiBuilder = new StringBuilder(m_baseUei);
        ueiBuilder.append("/").append(eventCode);
        return ueiBuilder.toString();
    }
    
    public String makeEventLabel(AlertMapping mapping) {
        StringBuilder labelBuilder = new StringBuilder("Spectrum imported event: ");
        String shortName = mapping.getEventCode();
        if (m_eventFormats.containsKey(mapping.getEventCode())) {
            Matcher m = Pattern.compile("(?s).*An? \"(.*?)\" event has occurred.*").matcher(m_eventFormats.get(mapping.getEventCode()).getContents());
            if (m.find()) {
                shortName = m.group(1);
            }
        }
        labelBuilder.append(shortName);
        return labelBuilder.toString();
    }
    
    public String makeDescr(AlertMapping mapping) {
        StringBuilder descrBuilder = new StringBuilder("<p>");
        if (m_eventFormats.containsKey(mapping.getEventCode())) {
            String theDescr = m_utils.translateAllSubstTokens(m_eventFormats.get(mapping.getEventCode()));
            theDescr = theDescr.replaceAll("\n", "<br/>\n");
            descrBuilder.append(theDescr);
        }
        descrBuilder.append("</p>\n");
        return descrBuilder.toString();
    }
    
    public Logmsg makeLogMsg(AlertMapping mapping, EventDisposition dispo) {
        Logmsg msg = new Logmsg();
        if (! dispo.isPersistent()) {
            msg.setDest("donotpersist");
        } else if (! dispo.isLogEvent()) {
            msg.setDest("discardtraps");
        } else {
            msg.setDest("logndisplay");
        }
        msg.setContent("<p>" + makeEventLabel(mapping) + "</p>");
        return msg;
    }
    
    public String makeSeverity(AlertMapping mapping, EventDisposition dispo) {
        if (dispo.isClearAlarm()) {
            // A clear-alarm always needs to have Normal severity for our automations to work
            return "Normal";
        }
        return m_utils.translateSeverity(dispo.getAlarmSeverity());
    }
    
    public AlarmData makeAlarmData(AlertMapping mapping, EventDisposition dispo) {
        if (!dispo.isCreateAlarm() && !dispo.isClearAlarm()) {
            return null;
        }
        AlarmData alarmData = new AlarmData();
        
        // Set the alarm-type according to clues in the disposition
        if (dispo.isClearAlarm()) {
            alarmData.setAlarmType(2);
        } else {
            alarmData.setAlarmType(1);
        }
        
        // Set the reduction key to include standard node stuff plus any discriminators
        StringBuilder rkBuilder = new StringBuilder("%uei%:");
        rkBuilder.append(m_reductionKeyBody);
        for (int discriminator : dispo.getDiscriminators()) {
            rkBuilder.append(":%parm[#").append(discriminator).append("]%");
        }
        // If it's marked as a unique alarm, add the event ID to the reduction-key
        if (dispo.isUniqueAlarm()) {
            rkBuilder.append("%eventid%"); 
        }
        alarmData.setReductionKey(rkBuilder.toString());
        
        // If it's a clear-alarm, set the clear-key appropriately
        if (dispo.isClearAlarm()) {
            StringBuilder ckBuilder = new StringBuilder(makeUei(dispo.getClearAlarmCause()));
            ckBuilder.append(":").append(m_reductionKeyBody);
            for (int discriminator : dispo.getDiscriminators()) {
                ckBuilder.append(":%parm[#").append(discriminator).append("]%");
            }
            alarmData.setClearKey(ckBuilder.toString());
        }
        
        return alarmData;
    }
    
    public List<Varbindsdecode> makeVarbindsDecodes(AlertMapping mapping) throws IOException {
        if (m_eventFormats.containsKey(mapping.getEventCode())) {
            EventFormat fmt = m_eventFormats.get(mapping.getEventCode());
            return m_utils.translateAllEventTables(fmt, m_customEventsDir.getFile().getPath() + File.separator + "CsEvFormat" + File.separator + "EventTables");
        } else {
            return new ArrayList<Varbindsdecode>();
        }
    }
    
    public boolean shouldDiscardEvent(EventDisposition dispo) {
        boolean discard = false;
        StringBuilder eventKeyBldr = new StringBuilder(dispo.getEventCode());
        for (int d : dispo.getDiscriminators()) {
            eventKeyBldr.append(",").append(d);
        }
        String eventKey = eventKeyBldr.toString();
        
        // If not yet recorded, note this one
        if (dispo.isCreateAlarm()) {
            m_alarmCreators.put(eventKey, eventKey);
        }
        
        // If this is a clear-alarm, but a create-alarm already exists with
        // the same event-code and discriminators, then we should discard
        // this one
        if (dispo.isClearAlarm() && m_alarmCreators.containsKey(eventKey)) {
            discard = true;
        }
        return discard;
    }
    
    private void prettyPrintXML(String inDoc) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource inSrc = new InputSource(new StringReader(inDoc));
        Document outDoc = builder.parse(inSrc);
        
        OutputFormat fmt = new OutputFormat(outDoc);
        fmt.setLineWidth(72);
        fmt.setIndenting(true);
        fmt.setIndent(2);
        XMLSerializer ser = new XMLSerializer(m_outputWriter, fmt);
        ser.serialize(outDoc);
    }
    
    public void setBaseUei(String baseUei) {
        if (baseUei == null) {
            throw new IllegalArgumentException("The base-UEI must be non-null");
        }
        m_baseUei = baseUei;
    }
    
    public String getBaseUei() {
        return m_baseUei;
    }
    
    public void setCustomEventsDir(FileSystemResource customEventsDir) throws IOException {
        if (! customEventsDir.getFile().isDirectory()) {
            throw new IllegalArgumentException("The customEventsDir property must refer to a directory");
        }
        m_customEventsDir = customEventsDir;
    }
    
    public Resource getCustomEventsDir() {
        return m_customEventsDir;
    }
    
    public List<AlertMapping> getAlertMappings() {
        return Collections.unmodifiableList(m_alertMappings);
    }
    
    public List<EventDisposition> getEventDispositions() {
        return Collections.unmodifiableList(m_eventDispositions);
    }
    
    public Map<String,EventFormat> getEventFormats() {
        return Collections.unmodifiableMap(m_eventFormats);
    }
    
    public void setOutputWriter(PrintWriter out) {
        m_outputWriter = out;
    }
    
    public PrintWriter getOutputWriter() {
        return m_outputWriter;
    }
    
    public void setModelTypeAssetField(String fieldName) {
        m_modelTypeAssetField = fieldName;
    }
    
    public String getModelTypeAssetField() {
        return m_modelTypeAssetField;
    }
    
    public void setReductionKeyBody(String body) {
        m_reductionKeyBody = body;
    }
    
    public String getReductionKeyBody() {
        return m_reductionKeyBody;
    }
    
    private void printHelp(String msg) {
        System.err.println("Error: " + msg + "\n\n");
        System.err.println(COMMAND_HELP);
    }
    private void printHelp(String msg, Throwable t) {
        System.err.println("Error: " + msg + "\n\n");
        t.printStackTrace();
        System.err.println(COMMAND_HELP);
    }

}
