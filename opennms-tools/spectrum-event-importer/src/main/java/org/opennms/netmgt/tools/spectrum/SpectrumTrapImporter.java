/**
 * 
 */

package org.opennms.netmgt.tools.spectrum;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.Varbindsdecode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Jeff Gehlbach <jeffg@opennms.org>
 * @author OpenNMS <http://www.opennms.org/>
 *
 */
public class SpectrumTrapImporter {
    private List<AlertMapping> m_alertMappings;
    private List<EventDisposition> m_eventDispositions;
    private Map<String,EventFormat> m_eventFormats;
    private String m_modelTypeAssetField;
    private SpectrumUtils m_utils;
    
    /**
     * Something like uei.opennms.org/import/Spectrum/
     */
    private String m_baseUei = null;
    private Resource m_customEventsDir = null;
    private PrintWriter m_outputWriter = null;
    private String m_reductionKeyBody = null;
    
    public static void main(String[] argv) {
        SpectrumTrapImporter importer = new SpectrumTrapImporter();
        importer.configureArgs(argv);
        
        try {
            importer.afterPropertiesSet();
            importer.initialize();
        } catch (Exception e) {
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
            events.marshal(importer.getOutputWriter());
        } catch (MarshalException e) {
            importer.printHelp("Fatal exception while marshaling output: " + e.getMessage());
        } catch (ValidationException e) {
            importer.printHelp("Fatal exception while validating output: " + e.getMessage());
        }
    }
    
    private void initialize() throws Exception {
        Resource alertMapResource = new UrlResource(m_customEventsDir.getURI() + "/AlertMap");
        Resource eventDispResource = new UrlResource(m_customEventsDir.getURI() + "/EventDisp");
        
        m_alertMappings = new AlertMapReader(alertMapResource).getAlertMappings();
        m_eventDispositions = new EventDispositionReader(eventDispResource).getEventDispositions();
        loadEventFormats();
        
        m_utils = new SpectrumUtils();
        m_utils.setModelTypeAssetField(m_modelTypeAssetField);
    }
    
    private void loadEventFormats() throws Exception {
        String csEvFormatDirName = m_customEventsDir.getURI() + "/CsEvFormat";
        Map<String,EventFormat> formats = new HashMap<String,EventFormat>();
        for (AlertMapping mapping : m_alertMappings) {
            if (formats.containsKey(mapping.getEventCode())) {
                LogUtils.debugf(this, "Already have read an event-format for event-code [%s], not loading again", mapping.getEventCode());
                continue;
            }
            String formatFileName = csEvFormatDirName + "/Event" + (mapping.getEventCode().substring(2));
            try {
                EventFormatReader reader = new EventFormatReader(new UrlResource(formatFileName));
                formats.put(mapping.getEventCode(), reader.getEventFormat());
            } catch (FileNotFoundException fnfe) {
                LogUtils.infof(this, "Unable to load an event-format for event-code [%s] from [%s]; continuing without it", mapping.getEventCode(), formatFileName);
                continue;
            }
        }
        m_eventFormats = formats;
    }
    
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
                if (cmd.getOptionValue('d').contains(":/")) {
                    m_customEventsDir = new UrlResource(cmd.getOptionValue('d'));
                } else {
                    m_customEventsDir = new FileSystemResource(cmd.getOptionValue('d'));
                }
            }
            
            if (cmd.hasOption('t')) {
                m_modelTypeAssetField = "%asset[" + cmd.getOptionValue('t') + "]%";
            } else {
                m_modelTypeAssetField = "%asset[manufacturer]%";
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
        } catch (MalformedURLException mue) {
            printHelp("Custom events input URL is malformed");
        }
    }
    
    public Events makeEvents() throws IOException {
        Events events = new Events();
        for (AlertMapping mapping : m_alertMappings) {
            for (EventDisposition dispo : m_eventDispositions) {
                if (dispo.getEventCode().equals(mapping.getEventCode())) {
                    events.addEvent(makeEventConf(mapping, dispo));
                }
            }
        }
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
            if (m.matches()) {
                shortName = m.group(1);
            }
        }
        labelBuilder.append(shortName);
        return labelBuilder.toString();
    }
    
    public String makeDescr(AlertMapping mapping) {
        StringBuilder descrBuilder = new StringBuilder("<pre>\n");
        if (m_eventFormats.containsKey(mapping.getEventCode())) {
            descrBuilder.append(m_utils.translateAllSubstTokens(m_eventFormats.get(mapping.getEventCode())));
        }
        descrBuilder.append("</pre>\n");
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
        msg.setContent("<pre>" + makeEventLabel(mapping) + "</pre>");
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
        if (! dispo.isCreateAlarm()) {
            return null;
        }
        AlarmData alarmData = new AlarmData();
        
        // Set the alarm-type according to clues in the disposition
        if (dispo.isClearAlarm()) {
            alarmData.setAlarmType(2);
        } else if (! dispo.isUserClearable()) {
            alarmData.setAlarmType(1);
        } else {
            alarmData.setAlarmType(3);
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
            return m_utils.translateAllEventTables(fmt, m_customEventsDir + "/CsEvFormat/EventTables");
        } else {
            return new ArrayList<Varbindsdecode>();
        }
    }
    
    private static void prettyPrintXML(InputStream docStream, OutputStream out) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(docStream);
        
        OutputFormat fmt = new OutputFormat(doc);
        fmt.setLineWidth(72);
        fmt.setIndenting(true);
        fmt.setIndent(2);
        XMLSerializer ser = new XMLSerializer(out, fmt);
        ser.serialize(doc);
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
    
    public void setCustomEventsDir(Resource customEventsDir) throws IOException {
        if (! customEventsDir.getFile().isDirectory()) {
            throw new IllegalArgumentException("The customEventsDir property must refer to a directory");
        }
        m_customEventsDir = customEventsDir;
    }
    
    public Resource getCustomEventsDir() {
        return m_customEventsDir;
    }
    
    public List<AlertMapping> getAlertMappings() {
        return m_alertMappings;
    }
    
    public List<EventDisposition> getEventDispositions() {
        return m_eventDispositions;
    }
    
    public Map<String,EventFormat> getEventFormats() {
        return m_eventFormats;
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
        System.out.println("Error: " + msg + "\n\n");
    }
    
}
