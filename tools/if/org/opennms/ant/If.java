package org.opennms.ant;

import java.io.File;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.util.regexp.*;


public class If extends Task
{        

    protected Task thenCaller;
    protected Task elseCaller;
    
    protected String value;
    protected String eq;
    protected String ne;    
    protected String rePattern;
    protected String negativeRePattern;

    protected String file;
    protected String directory;
    protected String exists;
    protected String canRead;
    protected String canWrite;    


    public void execute() {
        int requiredAttributesCount = 0;
        if( this.value != null )     { requiredAttributesCount++; }
        if( this.file != null )      { requiredAttributesCount++; }        
        if( this.directory != null ) { requiredAttributesCount++; }        
        if( this.exists != null )    { requiredAttributesCount++; }
        if( this.canRead != null )   { requiredAttributesCount++; }        
        if( this.canWrite != null )  { requiredAttributesCount++; }        
        
        if(requiredAttributesCount == 0) {
            throw new BuildException("One attribute from value, isFile, isDirectory, exists, canRead, or canWrite must be set.", this.location);            
        }
        else if(requiredAttributesCount > 1) {
            throw new BuildException("Only one attribute from value, isFile, isDirectory, exists, canRead, and canWrite can be set.", this.location);
        }

        if(this.thenCaller == null ) {
            throw new BuildException("Child then is required.", this.location );
        }       

        boolean conditionValue = false;
        
        if( this.value != null ) {
            conditionValue = this.handleValueConditional();                        
        }
        else if( this.file != null ) {
            File path = new File(this.file);
            conditionValue = path.isFile();
        }
        else if( this.directory != null ) {            
            File path = new File(this.directory);
            conditionValue = path.isDirectory();            
        }
        else if( this.exists != null ) {            
            File path = new File(this.exists);
            conditionValue = path.exists();
        }
        else if( this.canRead != null ) {            
            File path = new File(this.canRead);
            conditionValue = path.canRead();
        }
        else if( this.canWrite != null ) {            
            File path = new File(this.canWrite);
            conditionValue = path.canWrite();
        }
        
        if(conditionValue) {        
            this.thenCaller.execute();
        }
        else if(this.elseCaller != null) {
            this.elseCaller.execute();
        }
    }


    protected boolean handleValueConditional() {
        boolean returnVal = false;
        
        int valueRequiredAttributesCount = 0;
        
        if( this.eq != null ) { valueRequiredAttributesCount++; }
        if( this.ne != null ) { valueRequiredAttributesCount++; }
        if( this.rePattern != null ) { valueRequiredAttributesCount++; }
        if( this.negativeRePattern != null ) { valueRequiredAttributesCount++; }
        
        if( valueRequiredAttributesCount == 0 ) {
            throw new BuildException( "One attribute from eq, ne, regexp, or negativeRegexp is required when using the value attribute." );
        }
        else if( valueRequiredAttributesCount > 1 ) {
            throw new BuildException( "Only one attribute from eq, ne, regexp, and negativeRegexp can be set when using the value attribute." );
        }

        if( this.eq != null ) {
            returnVal = this.value.equals(this.eq);            
        }
        else if( this.ne != null ) {
            returnVal = !this.value.equals(this.ne);                        
        }
        else if( this.rePattern != null ) {
            RegexpMatcher re = (new RegexpMatcherFactory()).newRegexpMatcher();                    
            re.setPattern(this.rePattern);            
            returnVal = re.matches(value);
        }
        else if( this.negativeRePattern != null ) {
            RegexpMatcher re = (new RegexpMatcherFactory()).newRegexpMatcher();                    
            re.setPattern(this.negativeRePattern);            
            returnVal = !re.matches(value);
        }
        
        return( returnVal );        
    }
    
    
    public CallTarget createThen() {
       this.thenCaller = this.createTask("antcall");                   
       return (CallTarget)this.thenCaller;
    }

    
    public CallTarget createElse() {
       this.elseCaller = this.createTask("antcall");            
       return (CallTarget)this.elseCaller;
    }
    

    /** thenCall is an alias for then. */
    public CallTarget createThenCall() {
        return( this.createThen() );
    }

    
    /** elseCall is an alias for else. */
    public CallTarget createElseCall() {
        return( this.createElse() );
    }

    
    public If createThenIf() {
        this.thenCaller = this.createTask("if");
        return (If)this.thenCaller;
    }

    
    public If createElseIf() {
        this.elseCaller = this.createTask("if");
        return (If)this.elseCaller;
    }
 

    public Property createThenProperty() {
        this.thenCaller = this.createTask("property");
        return (Property)this.thenCaller;
    }
    
    
    public Property createElseProperty() {
        this.elseCaller = this.createTask("property");
        return (Property)this.elseCaller;        
    }
    
        
    public void setValue(String value) {
        this.value = value;
    }   

    
    /** Match is an alias for eq. */
    public void setMatch(String eq) {
        this.eq = eq;
    }

    public void setEq(String eq) {
        this.eq = eq;
    }


    public void setNe(String ne) {
        this.ne = ne;
    }
    
    
    public void setRegexp(String rePattern) {
        this.rePattern = rePattern;
    }

    
    public void setNegativeRegexp(String negativeRePattern) {
        this.negativeRePattern = negativeRePattern;
    }
    
    
    public void setIsFile(String file) {
        this.file = file;
    }

    
    public void setIsDirectory(String directory) {
        this.directory = directory;
    }
    
    
    public void setExists(String exists) {
        this.exists = exists;
    }

    
    public void setCanRead(String canRead) {
        this.canRead = canRead;
    }

    
    public void setCanWrite(String canWrite) {
        this.canWrite = canWrite;
    }

    
    protected Task createTask( String name ) {
        Task task = this.project.createTask(name);
        task.setOwningTarget(this.target);
        task.setTaskName(this.getTaskName());
        task.setLocation(this.location);
        task.init();
        
        return( task );
    }
}
