//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 20: System.out.println -> log().error. - dj@opennms.org
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.notification.bobject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.opennms.core.utils.ThreadCategory;

/**
 * This is a class to store and execute a console command
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 * @since 1.8.1
 */
public class Command {
    /**
     * The name of the command
     */
    private String m_commandName;

    /**
     * The types of the command, useful for supporting classes to do things
     * based on what this command is for. Basically an identifying name.
     */
    private List<String> m_types;

    /**
     * The comments for the command
     */
    private String m_commandComments;

    /**
     * The map of arguments in the command
     */
    private List<Argument> m_arguments;

    /**
     * A boolean that indicates if this command requires data passed to it from
     * an input stream.
     */
    private boolean m_useStream;

    /**
     * Default constructor, intializes the members
     */
    public Command() {
        m_arguments = new ArrayList<Argument>();
        m_types = new ArrayList<String>();
        m_useStream = false;
    }

    /**
     * Creates a new Command objects with identical values to the current
     * Command object.
     *
     * @return a copy of this command
     */
    public Command copy() {
        Command copy = new Command();

        copy.setCommandName(m_commandName);
        copy.setCommandComments(m_commandComments);
        copy.setUseStream(m_useStream);

        for (String type : m_types) {
            copy.addType(type);
        }

        for (Argument oldArg : m_arguments) {
            Argument newArg = new Argument();

            newArg.setSwitch(oldArg.getSwitch());
            newArg.setSubstitution(oldArg.getSubstitution());
            newArg.setValue(oldArg.getValue());
            newArg.setStreamed(oldArg.isStreamed());

            copy.addArgument(newArg);
        }

        return copy;
    }

    /**
     * Sets the command name
     *
     * @param aName
     *            the name of the command
     */
    public void setCommandName(String aName) {
        m_commandName = aName;
    }

    /**
     * Returns the command name
     *
     * @return the name of the command
     */
    public String getCommandName() {
        return m_commandName;
    }

    /**
     * Adds an identifier type for command
     *
     * @param aType
     *            the notification type
     */
    public void addType(String aType) {
        m_types.add(aType);
    }

    /**
     * Returns the notification type of the command
     *
     * @return true if contains the type
     * @param aType a {@link java.lang.String} object.
     */
    public boolean isOfType(String aType) {
        return m_types.contains(aType);
    }

    /**
     * Returns the first type in the list as a string
     *
     * @return the type
     */
    public String getType() {
        return m_types.get(0);
    }

    /**
     * Sets the comments for the command
     *
     * @param someComments
     *            the comments for the command
     */
    public void setCommandComments(String someComments) {
        m_commandComments = someComments;
    }

    /**
     * Returns the comments for the command
     *
     * @return the comments for the command
     */
    public String getCommandComments() {
        return m_commandComments;
    }

    /**
     * Adds a argument to the list of arguments
     *
     * @param anArgument
     *            the argument to be added.
     */
    public void addArgument(Argument anArgument) {
        m_arguments.add(anArgument);
    }

    /**
     * This method sets the boolean that indicates if this command requires an
     * input stream.
     *
     * @param aBool
     *            true if a stream should be used, false otherwise
     */
    public void setUseStream(boolean aBool) {
        m_useStream = aBool;
    }

    /**
     * Returns the list of arguments
     *
     * @return the list of arguments
     */
    public List getArguments() {
        return m_arguments;
    }

    /**
     * This method sets an argument specified by the switch param to the given
     * value param
     *
     * @param aSwitch
     *            the argument to set
     * @param aValue
     *            the value to set
     */
    public void setArgumentValue(String aSwitch, String aValue) {
        for (Argument arg : m_arguments) {
            if (arg.getSwitch().equals(aSwitch)) {
                arg.setValue(aValue);
                break;
            }
        }
    }

    /**
     * This method determines if this command has a given switch as a member of
     * its arguments
     *
     * @param aSwitch
     *            the switch to check for
     * @return true if the command has the switch, false otherwise
     */
    public boolean hasSwitch(String aSwitch) {
        return getArgumentSwitches().contains(aSwitch);
    }

    /**
     * This method gets the list of switches that Notify knows about that will
     * map to a given console command.
     *
     * @return a list of parameter switches
     */
    public List<String> getArgumentSwitches() {
        List<String> switches = new ArrayList<String>();

        for (Argument argument : m_arguments) {
            switches.add(argument.getSwitch());
        }

        return switches;
    }

    /**
     * This method executes the command using a Process. The method will decide
     * if an input stream needs to be used.
     *
     * @return the return code of the command
     */
    public int execute() {
        int returnCode = 0;

        List<String> args = new ArrayList<String>();

        args.add(m_commandName);

        // put the non streamed arguments into the argument array
        for (Argument curArg : m_arguments) {
            // only non streamed arguments go into this list
            if (!curArg.isStreamed()) {
                if (!curArg.getSubstitution().equals("")) {
                    args.add(curArg.getSubstitution());
                }
                if (curArg.getValue() != null && !curArg.getValue().equals("")) {
                    args.add(curArg.getValue());
                }
            }
        }

        try {
            // set up the process
            String[] arguments = args.toArray(new String[args.size()]);

            Process command = Runtime.getRuntime().exec(arguments);

            // see if we need to build a streamed argument buffer
            if (m_useStream) {
                // make sure the output we are writing is buffered
                BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(command.getOutputStream(), "UTF-8"));

                StringBuffer buffer = new StringBuffer();

                // now write each streamed argument to the processes input
                // buffer
                for (Argument curArg : m_arguments) {
                    if (curArg.isStreamed()) {
                        if (!curArg.getSubstitution().equals("")) {
                            buffer.append(curArg.getSubstitution());
                        }
                        if (!curArg.getValue().equals("")) {
                            buffer.append(curArg.getValue());
                        }
                    }
                }

                // put the streamed argumetns into the stream
                processInput.write(buffer.toString());

                processInput.flush();
                processInput.close();
            }

            returnCode = command.waitFor();
        } catch (IOException e) {
            log().error("Command failed with IOExceptoin: " + e, e);
        } catch (InterruptedException e) {
            log().error("Command failed with InterruptedExceptoin: " + e, e);
        }

        log().debug("command complete with return code " + returnCode);

        return returnCode;
    }

    /**
     * Returns a String representation of the command as it would look to
     * execute on the console.
     *
     * @return a string representation
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(m_commandName + " ");

        for (Argument arg : m_arguments) {
            buffer.append(arg.getSubstitution() + " " + arg.getValue());
        }

        return buffer.toString();
    }

    /**
     * Convenience method for creating arrays of strings suitable for use as
     * command-line parameters when executing an external process.
     *
     * <p>
     * The default {@link java.lang.Runtime#exec(java.lang.String[])}method
     * will split a single string based on spaces, but it does not respect
     * spaces within quotation marks, and it will leave the quotation marks in
     * the resulting substrings. This method solves those problems by replacing
     * all in-quote spaces with the given delimiter, removes the quotes, and
     * then splits the resulting string by the remaining out-of-quote spaces. It
     * then goes through each substring and replaces the delimiters with spaces.
     * </p>
     *
     * <p>
     * <em>Caveat:</em> This method does not respect escaped quotes! It will
     * simply remove them and leave the stray escape characters.
     * </p>
     *
     * @param s
     *            the string to split
     * @param delim
     *            a char that does not already exist in <code>s</code>
     * @return An array of strings split by spaces outside of quotes.
     * @throws java.lang.IllegalArgumentException
     *             If <code>s</code> is null or if <code>delim</code>
     *             already exists in <code>s</code>.
     */
    public static String[] createCommandArray(String s, char delim) {
        if (s == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (s.indexOf(delim) != -1) {
            throw new IllegalArgumentException("String parameter cannot already contain delimiter character: " + delim);
        }

        char[] chars = s.toCharArray();
        boolean inquote = false;
        StringBuffer buffer = new StringBuffer();

        // append each char to a StringBuffer, but
        // leave out quote chars and replace spaces
        // inside quotes with the delim char
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '"') {
                inquote = (inquote) ? false : true;
            } else if (inquote && chars[i] == ' ') {
                buffer.append(delim);
            } else {
                buffer.append(chars[i]);
            }
        }

        s = buffer.toString();

        // split the new string by the whitespaces that were not in quotes
        List<String> arrayList = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(s);

        while (tokenizer.hasMoreTokens()) {
            arrayList.add(tokenizer.nextElement().toString());
        }

        // put the strings in the arraylist into a string[]
        String[] list = arrayList.toArray(new String[arrayList.size()]);

        // change all the delim characters back to spaces
        for (int i = 0; i < list.length; i++) {
            list[i] = list[i].replace(delim, ' ');
        }

        return list;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
