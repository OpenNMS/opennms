package org.opennms.netmgt.provision.adapters.link;


public abstract class EndPointStatusValidators {
    
    public static EndPointStatusValidator ping() {
        return new PingEndPointStatusValidator();
    }
    
    public static EndPointStatusValidator match(final String oid, final String regex) {
        return new MatchingSnmpEndPointStatusValidator(regex, oid);
    }
    
    public static EndPointStatusValidator and(final EndPointStatusValidator... validators) {
        return new AndEndPointStatusValidator(validators);
    }
    
    public static EndPointStatusValidator or(final EndPointStatusValidator... validators) {
        return new OrEndPointStatusValidator(validators);
    }
    
}
