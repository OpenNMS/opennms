package org.opennms.util.ilr;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;


public class ArgumentParser {

    private String m_programName;
    private Object m_argHandler;
    private Method m_argMethod;
    private String m_argHelp;
    PrintStream out = System.err;
    private Map<Option, Method> m_options = new LinkedHashMap<Option, Method>();
     
    public ArgumentParser(String programName, Object argHandler) {
        m_programName = programName;
        m_argHandler = argHandler;  
        Method [] methods = m_argHandler.getClass().getMethods();
        for(Method m: methods){
            if(m.isAnnotationPresent(Option.class)){
                Option option = m.getAnnotation(Option.class);
                m_options.put(option, m);
            }
            if(m.isAnnotationPresent(Arguments.class)) {
                m_argMethod = m;
                m_argHelp = m.getAnnotation(Arguments.class).help();
            }
        }
    }
    public void processArgs(String[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        for(int i = 0 ; i < args.length ; i++){
            String arg = args[i];
            if (arg.equals("-h") || arg.equals("--help")){
                printHelpOptions();
            }
            else if(arg.startsWith("--")){
                Option o = findOptionByLongName(arg.substring(2));
                executeOption(o, arg);
            }
            else if(arg.startsWith("-")){
                Option o = findOptionByShortName(arg.substring(1));
                executeOption(o, arg);
            }else{
                m_argMethod.invoke(m_argHandler, arg);
            }
        }
        
    }
    public void printHelpOptions() {
        out.printf("Usage: %s <options> <arguments> \n", m_programName);
        out.printf("   where <arguments> is %s\n", m_argHelp);
        out.printf("Options: \n");
        out.printf("   -%-5s or --%-15s : %s\n", "h", "help", "print this help");
        for(Option o : m_options.keySet()){
            out.printf("   -%-5s or --%-15s : %s\n", o.shortName(), o.longName(), o.help());
        }
      
        
    }
    public void executeOption(Option o, String arg) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if(o != null){
            Method m = m_options.get(o);
            m.invoke(m_argHandler);      
        }else{
            throw new IllegalArgumentException("Illegal Argument: " + arg);
        }
    }
    public Option findOptionByShortName(String arg) {
        for(Option o : m_options.keySet()){
            if(arg.equals(o.shortName())){
                return o;
            }
        }
        return null;
    }
    public Option findOptionByLongName(String arg) {
        for(Option o : m_options.keySet()){
            if(arg.equals(o.longName())){
                return o;
            }
        }
        return null;
    }
}
