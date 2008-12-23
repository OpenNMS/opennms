package org.opennms.netmgt.mib2events;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpNotificationType;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.snmp.SnmpTrapType;
import net.percederberg.mibble.snmp.SnmpType;
import net.percederberg.mibble.type.IntegerType;
import net.percederberg.mibble.value.ObjectIdentifierValue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Decode;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.Varbindsdecode;
import org.w3c.dom.Document;

public class Mib2Events {
    // FIXME Left at mib2opennms to match what mib2opennms does
    //private static final String DEFAULT_UEIBASE = "uei.opennms.org/mib2events/";
    private static final String DEFAULT_UEIBASE = "uei.opennms.org/mib2opennms/";

    /** Command-line help. */
    private static final String COMMAND_HELP =
        "Reads a MIB definition, creating from its traps (if any) a set of\n" +
        "event definitions suitable for loading into OpenNMS. This program\n" +
        "comes with ABSOLUTELY NO WARRANTY; for details, see the LICENSE.txt\n" +
        "file.\n" +
        "\n" +
        "Syntax: Mib2Events --mib <file(s) or URL(s)> --ueibase <base-uei>\n" +
        "\n" +
        "	--mib		The pathname or URL of a MIB to load\n" +
        "	--ueibase	The base UEI for resulting event definitions\n" +
        "			(default: uei.opennms.org/mib2events/)\n" +
        "\n" +
        "EXAMPLES\n" +
        "\n" +
        "Create events from the OSPF-TRAP-MIB, putting the events' UEI into an\n" +
        "IETF namespace:\n" +
        "\n" +
        "Mib2Events --mib OSPF-TRAP-MIB.my --ueibase uei.opennms.org/vendors/ietf/\n";

    private String m_mibLocation;
    private String m_ueiBase = DEFAULT_UEIBASE;
    private MibLoader m_loader;
    private Mib m_mib;

    public static void main(String[] args) throws FileNotFoundException {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);

        Mib2Events convertor = new Mib2Events();

        convertor.parseCli(args);
        try {
            convertor.convert();
        } catch (FileNotFoundException e) {
            printError(convertor.getMibLocation(), e);
            System.exit(1);
        } catch (IOException e) {
            printError(convertor.getMibLocation(), e);
        } catch (MibLoaderException e) {
            e.getLog().printTo(System.err);
            System.exit(1);
        }

        if (convertor.getMib().getLog().warningCount() > 0) {
            convertor.getMib().getLog().printTo(System.err);
        }

        PrintStream out = System.out;
        out.println("<!-- Start of auto generated data from MIB: " + convertor.getMib().getName() + " -->");
        convertor.printEvents(out);
        out.println("<!-- End of auto generated data from MIB: " + convertor.getMib().getName() + " -->");
    }

    public String getMibLocation() {
        return m_mibLocation;
    }

    public void convert() throws IOException, MibLoaderException {
        m_loader = new MibLoader();
        m_mib = null;

        URL url;
        try {
            url = new URL(m_mibLocation);
        } catch (MalformedURLException e) {
            url = null;
        }

        if (url == null) {
            File file = new File(m_mibLocation);
            m_loader.addDir(file.getParentFile());
            m_mib = m_loader.load(file);
        } else {
            m_mib = m_loader.load(url);
        }
    }

    private void printEvents(PrintStream out) {
        if (m_loader == null) {
            throw new IllegalStateException("convert() must be called first");
        }

        for (Mib mib : m_loader.getAllMibs()) {
            if (!mib.isLoaded()) {
                continue;
            }

            Events events = convertMibToEvents(mib, m_ueiBase);

            if (events.getEventCount() < 1) {
                System.err.println("No trap definitions found in this MIB (" + mib.getName() + "), exiting");
                System.exit(0);
            }

            try {
                // FIXME We should just spit out a valid events inclusion file, but for now we match mib2opennms
                //StringWriter writer = new StringWriter();
                //
                //events.marshal(writer);
                //
                //String noNameSpace = writer.toString().replaceAll(" xmlns=\"[^\"]*\"", "");
                //prettyPrintXML(new ByteArrayInputStream(noNameSpace.getBytes()), out);

                for (Event event : events.getEventCollection()) {
                    StringWriter writer = new StringWriter();

                    event.marshal(writer);

                    String noNameSpace = writer.toString().replaceAll(" xmlns=\"[^\"]*\"", "");

                    ByteArrayOutputStream formattedXml = new ByteArrayOutputStream();
                    prettyPrintXML(new ByteArrayInputStream(noNameSpace.getBytes()), formattedXml);
                    String noXmlProcessingInstruction = formattedXml.toString().replaceAll("(?m)<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>\n", "");
                    String singleQuotesLogMsgDest = noXmlProcessingInstruction.replaceAll("dest=\"logndisplay\"", "dest='logndisplay'");
                    out.print(singleQuotesLogMsgDest);
                }
            } catch (MarshalException e) {
                System.err.println("Fatal: caught MarshalException:" + e);
            } catch (ValidationException e) {
                System.err.println("Fatal: caught ValidationException:" + e);
            } catch (Exception e) {
                System.err.println("Fatal: Unhandled exception: " + e);
            }
        }
    }

    public void parseCli(String[] argv) {
        Options opts = new Options();
        opts.addOption("m", "mib", true, "Pathname or URL of MIB file to scan for traps");
        opts.addOption("b", "ueibase", true, "Base UEI for resulting events");

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(opts, argv);
            if (cmd.hasOption('m')) {
                m_mibLocation = cmd.getOptionValue('m');
            } else {
                printHelp("You must specify a MIB file pathname or URL");
                System.exit(1);
            }
            if (cmd.hasOption("b")) {
                m_ueiBase = cmd.getOptionValue('b');
            }
        } catch (ParseException e) {
            printHelp("Failed to parse command line options");
            System.exit(1);
        }
    }

    public static void printHelp(String msg) {
        System.out.println("Error: " + msg + "\n\n");
        System.out.println(COMMAND_HELP);
    }

    public static void printError(String msg) {
        System.err.print("Error: ");
        System.err.println(msg);
    }

    public static void printError(String file, FileNotFoundException e) {
        StringBuffer buf = new StringBuffer();
        buf.append("Could not open file:\n\t");
        buf.append(file);
        printError(buf.toString());
    }

    public static void printError(String url, IOException e) {
        StringBuffer buf = new StringBuffer();
        buf.append("Could not open URL:\n\t");
        buf.append(url);
        printError(buf.toString());
    }

    public static String getTrapEnterprise(MibValueSymbol trapValueSymbol) {
        if (trapValueSymbol.getType() instanceof SnmpNotificationType) {
            String trapOid = "." + trapValueSymbol.getValue().toString();
            // FIXME This isn't right, I think, but it matches mib2opennms functionality... it should chop up just the last number, not the last *two*
            Matcher m = Pattern.compile("(.*)\\.\\d+\\.\\d+$").matcher(trapOid);
            if (m.matches()) {
                return m.group(1);
            } else {
                throw new IllegalStateException("Could not pull last two numbers out of trap OID '" + trapOid + "' to get SNMPv1 'enterprise' value");
            }
        } else if (trapValueSymbol.getType() instanceof SnmpTrapType) {
            SnmpTrapType v1trap = (SnmpTrapType) trapValueSymbol.getType();
            return "." + v1trap.getEnterprise().toString();
        } else {
            throw new IllegalStateException("Trying to get a trap enterprise number from a non-trap, non-notification object");
        }
    }

    public static String getTrapSpecificType(MibValueSymbol trapValueSymbol) {
        if (trapValueSymbol.getType() instanceof SnmpNotificationType) {
            String trapOid = trapValueSymbol.getValue().toString();
            Matcher m = Pattern.compile(".*\\.(\\d+)$").matcher(trapOid);
            if (m.matches()) {
                return m.group(1);
            } else {
                throw new IllegalStateException("Could not pull last number out of trap OID '" + trapOid + "' to get SNMPv1 'specific' value");
            }
        } else if (trapValueSymbol.getType() instanceof SnmpTrapType) {
            return trapValueSymbol.getValue().toString();
        } else {
            throw new IllegalStateException("Trying to get a trap enterprise number from a non-trap, non-notification object");
        }
    }

    public static String getTrapEventLabel(MibValueSymbol trapValueSymbol) {
        StringBuffer buf = new StringBuffer();
        buf.append(trapValueSymbol.getMib());
        buf.append(" defined trap event: ");
        buf.append(trapValueSymbol.getName());
        return buf.toString();
    }

    public static String getTrapEventUEI(MibValueSymbol trapValueSymbol, String ueibase) {
        StringBuffer buf = new StringBuffer(ueibase);
        if (! ueibase.endsWith("/")) {
            buf.append("/");
        }
        buf.append(trapValueSymbol.getName());
        return buf.toString();
    }

    public static List<MibValue> getTrapVars(MibValueSymbol trapValueSymbol) {
        if (trapValueSymbol.getType() instanceof SnmpNotificationType) {
            SnmpNotificationType v2notif = (SnmpNotificationType) trapValueSymbol.getType();
            return getV2NotificationObjects(v2notif);
        } else if (trapValueSymbol.getType() instanceof SnmpTrapType) {
            SnmpTrapType v1trap = (SnmpTrapType) trapValueSymbol.getType();
            return getV1TrapVariables(v1trap);
        } else {
            throw new IllegalStateException("trap type is not an SNMP v1 Trap or v2 Notification");      
        }
    }

    @SuppressWarnings("unchecked")
    private static List<MibValue> getV1TrapVariables(SnmpTrapType v1trap) {
        return v1trap.getVariables();
    }

    @SuppressWarnings("unchecked")
    private static List<MibValue> getV2NotificationObjects(SnmpNotificationType v2notif) {
        return v2notif.getObjects();
    }

    public static Logmsg getTrapEventLogmsg(MibValueSymbol trapValueSymbol) {
        Logmsg msg = new Logmsg();
        msg.setDest("logndisplay");

        // FIXME There some detail here (like removing the last \n) that can go away when we don't need to match mib2opennms exactly
        final StringBuffer dbuf = new StringBuffer();
        dbuf.append("<p>");
        dbuf.append("\n");
        dbuf.append("\t").append(trapValueSymbol.getName()).append(" trap received\n");
        int vbNum = 1;
        for (MibValue vb : getTrapVars(trapValueSymbol)) {
            dbuf.append("\t").append(vb.getName()).append("=%parm[#").append(vbNum).append("]%\n");
            vbNum++;
        }

        if (dbuf.charAt(dbuf.length() - 1) == '\n') {
            dbuf.deleteCharAt(dbuf.length() - 1); // delete the \n at the end
        }
        dbuf.append("</p>\n\t");

        msg.setContent(dbuf.toString());

        return msg;
    }

    public static String getTrapEventDescr(MibValueSymbol trapValueSymbol) {
        String description = ((SnmpType) trapValueSymbol.getType()).getDescription();

        // FIXME There a lot of detail here (like removing the last \n) that can go away when we don't need to match mib2opennms exactly

        final String descrStartingNewlines = description.replaceAll("^", "\n<p>");

        final String descrEndingNewlines = descrStartingNewlines.replaceAll("$", "</p>\n");

        //final String descrTabbed = descrEndingNewlines.replaceAll("(?m)^", "\t");
        //final StringBuffer dbuf = new StringBuffer(descrTabbed);

        final StringBuffer dbuf = new StringBuffer(descrEndingNewlines);
        if (dbuf.charAt(dbuf.length() - 1) == '\n') {
            dbuf.deleteCharAt(dbuf.length() - 1); // delete the \n at the end
        }

        //if (dbuf.lastIndexOf("\n") != -1) {
        //    dbuf.insert(dbuf.lastIndexOf("\n") + 1, '\t');
        //}

        //final StringBuffer dbuf = new StringBuffer(descrEndingNewlines);
        //dbuf.append("\n");

        dbuf.append("<table>");
        dbuf.append("\n");
        int vbNum = 1;
        for (MibValue vb : getTrapVars(trapValueSymbol)) {
            dbuf.append("\t<tr><td><b>\n\n\t").append(vb.getName());
            dbuf.append("</b></td><td>\n\t%parm[#").append(vbNum).append("]%;</td><td><p>");

            SnmpObjectType snmpObjectType = ((SnmpObjectType) ((ObjectIdentifierValue) vb).getSymbol().getType());
            if (snmpObjectType.getSyntax().getClass().equals(IntegerType.class)) {
                IntegerType integerType = (IntegerType) snmpObjectType.getSyntax();

                if (integerType.getAllSymbols().length > 0) {
                    SortedMap<Integer, String> map = new TreeMap<Integer, String>();
                    for (MibValueSymbol sym : integerType.getAllSymbols()) {
                        map.put(new Integer(sym.getValue().toString()), sym.getName());
                    }

                    dbuf.append("\n");
                    for (Entry<Integer, String> entry : map.entrySet()) {
                        dbuf.append("\t\t").append(entry.getValue()).append("(").append(entry.getKey()).append(")\n");
                    }
                    dbuf.append("\t");
                }
            }

            dbuf.append("</p></td></tr>\n");
            vbNum++;
        }

        if (dbuf.charAt(dbuf.length() - 1) == '\n') {
            dbuf.deleteCharAt(dbuf.length() - 1); // delete the \n at the end
        }
        dbuf.append("</table>\n\t");

        return dbuf.toString();
    }

    public static AlarmData getTrapEventAlarmData() {
        AlarmData ad = new AlarmData();
        ad.setReductionKey("%uei%:%dpname%:%nodeid%:%interface%");
        ad.setAlarmType(1);
        ad.setAutoClean(false);
        return ad;
    }

    public static Event getTrapEvent(MibValueSymbol trapValueSymbol, String ueibase) {
        Event evt = new Event();
        Mask mask = new Mask();
        Maskelement me;

        // Set the event's UEI, event-label, logmsg, severity, and descr
        evt.setUei(getTrapEventUEI(trapValueSymbol, ueibase));
        evt.setEventLabel(getTrapEventLabel(trapValueSymbol));
        evt.setLogmsg(getTrapEventLogmsg(trapValueSymbol));
        evt.setSeverity("Indeterminate");
        evt.setDescr(getTrapEventDescr(trapValueSymbol));

        // FIXME Disabled for now so we match mib2opennms functionality
        //evt.setAlarmData(getTrapEventAlarmData());

        List<Varbindsdecode> decode = getTrapVarbindsDecode(trapValueSymbol);
        if (!decode.isEmpty()) {
            evt.setVarbindsdecode(decode);
        }

        // Construct the event mask object
        // The "ID" mask element (trap enterprise)
        me = new Maskelement();
        me.setMename("id");
        me.addMevalue(getTrapEnterprise(trapValueSymbol));
        mask.addMaskelement(me);

        // The "generic" mask element (hard-wired to enterprise-specific(6))
        me = new Maskelement();
        me.setMename("generic");
        me.addMevalue("6");
        mask.addMaskelement(me);

        // The "specific" mask element (trap specific-type)
        me = new Maskelement();
        me.setMename("specific");
        me.addMevalue(getTrapSpecificType(trapValueSymbol));
        mask.addMaskelement(me);

        evt.setMask(mask);

        return evt;
    }

    private static List<Varbindsdecode> getTrapVarbindsDecode(MibValueSymbol trapValueSymbol) {
        Map<String, Varbindsdecode> decode = new LinkedHashMap<String, Varbindsdecode>();

        int vbNum = 1;
        for (MibValue vb : getTrapVars(trapValueSymbol)) {
            String parmName = "parm[#" + vbNum + "]";

            SnmpObjectType snmpObjectType = ((SnmpObjectType) ((ObjectIdentifierValue) vb).getSymbol().getType());
            if (snmpObjectType.getSyntax().getClass().equals(IntegerType.class)) {
                IntegerType integerType = (IntegerType) snmpObjectType.getSyntax();

                if (integerType.getAllSymbols().length > 0) {
                    SortedMap<Integer, String> map = new TreeMap<Integer, String>();
                    for (MibValueSymbol sym : integerType.getAllSymbols()) {
                        map.put(new Integer(sym.getValue().toString()), sym.getName());
                    }

                    for (Entry<Integer, String> entry : map.entrySet()) {
                        if (!decode.containsKey(parmName)) {
                            Varbindsdecode newVarbind = new Varbindsdecode();
                            newVarbind.setParmid(parmName);
                            decode.put(newVarbind.getParmid(), newVarbind);
                        }

                        Decode d = new Decode();
                        d.setVarbinddecodedstring(entry.getValue());
                        d.setVarbindvalue(entry.getKey().toString());
                        decode.get(parmName).addDecode(d);
                    }
                }
            }

            vbNum++;
        }

        return new ArrayList<Varbindsdecode>(decode.values());
    }

    private static Events convertMibToEvents(Mib mib, String ueibase) {
        Events events = new Events();
        for (MibSymbol sym : getAllSymbolsFromMib(mib)) {
            if (!(sym instanceof MibValueSymbol)) {
                continue;
            }

            MibValueSymbol vsym = (MibValueSymbol) sym;
            if ((!(vsym.getType() instanceof SnmpNotificationType)) && (!(vsym.getType() instanceof SnmpTrapType))) {
                continue;
            }

            events.addEvent(getTrapEvent(vsym, ueibase));
        }
        return events;
    }

    @SuppressWarnings("unchecked")
    private static Collection<MibSymbol> getAllSymbolsFromMib(Mib mib) {
        return mib.getAllSymbols();
    }

    public static void prettyPrintXML(InputStream docStream, OutputStream out) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(docStream);

        OutputFormat fmt = new OutputFormat(doc);
        fmt.setLineWidth(72);
        fmt.setIndenting(true);
        fmt.setIndent(2);
        XMLSerializer ser = new XMLSerializer(out, fmt);
        ser.serialize(doc);
    }

    public Mib getMib() {
        return m_mib;
    }
}