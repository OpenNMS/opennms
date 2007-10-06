package org.opennms.netmgt.mib2events;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpNotificationType;
import net.percederberg.mibble.snmp.SnmpTrapType;
import net.percederberg.mibble.snmp.SnmpType;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.w3c.dom.Document;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.AlarmData;

public class Mib2Events
{
	/* Command-line help */
	private static final String DEFAULT_UEIBASE = "uei.opennms.org/mib2events/";
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
	private static String mibloc;
	private static String ueibase = DEFAULT_UEIBASE;
	
	public static void main( String[] args ) {
        MibLoader loader = new MibLoader();
        Mib mib = null;
        File file;
        URL url;
        
        // Check command-line arguments
        parseCli(args);
        /*
        if (args.length < 2) {
        	printHelp("You must specify at least a MIB file or URL");
        	System.exit(1);
        }
        if (args[pos].equals("--mib")) {
        	mibloc = args[pos+1];
        	pos += 2;
        }
        if (args[pos].equals("--ueibase")) {
        	ueibase = args[pos+1];
        	pos += 2;
        }
        */

        try {
        	try {
	        	url = new URL(mibloc);
	        } catch (MalformedURLException e) {
	        	url = null;
	        }
	        if (url == null) {
	        	file = new File(mibloc);
	        	loader.addDir(file.getParentFile());
	        	mib = loader.load(file);
	        } else {
	        	mib = loader.load(url);
	        }
	        if (mib.getLog().warningCount() > 0) {
	        	mib.getLog().printTo(System.err);
	        }
        } catch (FileNotFoundException e) {
        	printError(mibloc, e);
        	System.exit(1);
        } catch (IOException e) {
        	printError(mibloc, e);
        } catch (MibLoaderException e) {
        	e.getLog().printTo(System.err);
        	System.exit(1);
        }
        printEvents(loader, ueibase);
	}
	
	public static void parseCli(String[] argv) {
		Options opts = new Options();
		opts.addOption("m", "mib", true, "Pathname or URL of MIB file to scan for traps");
		opts.addOption("b", "ueibase", true, "Base UEI for resulting events");
		
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine cmd = parser.parse(opts, argv);
			if (cmd.hasOption('m')) {
				mibloc = cmd.getOptionValue('m');
			} else {
				printHelp("You must specify a MIB file pathname or URL");
				System.exit(1);
			}
			if (cmd.hasOption("b")) {
				ueibase = cmd.getOptionValue('b');
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
		String enterpriseOid = ".1";
		if (trapValueSymbol.getType() instanceof SnmpNotificationType) {
			String trapOid = trapValueSymbol.getValue().toString();
			enterpriseOid = trapOid;
			Matcher m = Pattern.compile("(.*)\\.\\d+$").matcher(trapOid);
			if (m.matches()) {
				enterpriseOid = m.group(1);
			}
		} else if (trapValueSymbol.getType() instanceof SnmpTrapType) {
			SnmpTrapType v1trap = (SnmpTrapType)trapValueSymbol.getType();
			enterpriseOid = v1trap.getEnterprise().toString();
		}
		return enterpriseOid;
	}
	
	public static String getTrapSpecificType(MibValueSymbol trapValueSymbol) {
		String specificType = "0";
		if (trapValueSymbol.getType() instanceof SnmpNotificationType) {
			String trapOid = trapValueSymbol.getValue().toString();
			Matcher m = Pattern.compile(".*\\.(\\d+)$").matcher(trapOid);
			if (m.matches()) {
				specificType = m.group(1);
			}
		} else if (trapValueSymbol.getType() instanceof SnmpTrapType) {
			specificType = trapValueSymbol.getValue().toString();
		}
		return specificType;
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
	
	public static ArrayList<MibValue> getTrapVars(MibValueSymbol trapValueSymbol) {
		ArrayList<MibValue> vals = new ArrayList<MibValue>();
		if (trapValueSymbol.getType() instanceof SnmpNotificationType) {
			SnmpNotificationType v2notif = (SnmpNotificationType)trapValueSymbol.getType();
			vals = v2notif.getObjects();
		} else if (trapValueSymbol.getType() instanceof SnmpTrapType) {
			SnmpTrapType v1trap = (SnmpTrapType)trapValueSymbol.getType();
			vals = v1trap.getVariables();
		}
		return vals;
	}
	
	public static Logmsg getTrapEventLogmsg(MibValueSymbol trapValueSymbol) {
		Logmsg msg = new Logmsg();
		msg.setDest("logndisplay");
		msg.setContent("Log messages coming soon");
		return msg;
	}
	
	public static String getTrapEventDescr(MibValueSymbol trapValueSymbol) {
		String descr;
		Matcher m = Pattern.compile("^", Pattern.MULTILINE).matcher(((SnmpType)trapValueSymbol.getType()).getDescription());
		descr = m.replaceAll("\t");
		m = Pattern.compile("^").matcher(descr);
		descr = m.replaceAll("\n");
		m = Pattern.compile("$").matcher(descr);
		descr = m.replaceAll("\n");
		StringBuffer dbuf = new StringBuffer(descr);
		dbuf.append("\n");
		
		int vbNum = 1;
		for (MibValue vb : getTrapVars(trapValueSymbol)) {
			dbuf.append("\n\t").append(vb.getName()).append("=%parm[#").append(vbNum).append("]%");
			vbNum++;
		}
		
		descr = dbuf.toString();
		return descr;
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
		evt.setAlarmData(getTrapEventAlarmData());
		evt.setDescr(getTrapEventDescr(trapValueSymbol));
		
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
	
	public static void printEvents(MibLoader loader, String ueibase) {
		Mib[] mibs = loader.getAllMibs();
		
		for (int i = 0;i < mibs.length; i++) {
			if (mibs[i].isLoaded()) {
				printEvents(mibs[i], ueibase);
			}
		}
	}
	
	public static void printEvents(Mib mib, String ueibase) {
		Collection syms = mib.getAllSymbols();
		Iterator<MibSymbol> symIter = syms.iterator();
		MibSymbol sym = null;
		MibValueSymbol vsym = null;
		Iterator<MibValue> trapVarbinds;
		Events events = new Events();
		StringWriter writer = new StringWriter();
		
		while (symIter.hasNext()) {
			sym = symIter.next();
			if (! (sym instanceof MibValueSymbol))
				continue;
			vsym = (MibValueSymbol)sym;
			if ((! (vsym.getType() instanceof SnmpNotificationType)) && (! (vsym.getType() instanceof SnmpTrapType)))
				continue;
			
			events.addEvent(getTrapEvent(vsym, ueibase));
		}
		
		if (events.getEventCount() < 1) {
			System.err.println("No trap definitions found in this MIB (" + mib.getName() + "), exiting");
			System.exit(0);
		}
		
		try {
			events.marshal(writer);
			prettyPrintXML(new ByteArrayInputStream(writer.toString().getBytes()), (OutputStream)System.out);
		} catch (MarshalException e) {
			System.err.println("Fatal: caught MarshalException:" + e);
		} catch (ValidationException e) {
			System.err.println("Fatal: caught ValidationException:" + e);
		} catch (Exception e) {
			System.err.println("Fatal: Unhandled exception: " + e);
		}
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
}