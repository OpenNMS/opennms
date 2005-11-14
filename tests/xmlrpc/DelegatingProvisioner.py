"""
An object that delegates calls provision methods to a delegate.  This enables calling
class that don't support keyword arguments lists such as Jython loaded Java classes
and xmlrpc backed classes
"""

class DelegatingProvisioner:
    """
    This is an object that delegates provisioning calls to a delagate object.
    """
    def __init__(self, delegate):
        # Set up the system methods
        self.delegate = delegate
        
    def addServiceICMP( self,
                        serviceid,
                        retries,
                        timeout,
                        interval,
                        downtime_interval,
                        downtime_duration ):
       """
       addServiceICMP function
       """
       return self.delegate.addServiceICMP(serviceid, retries, timeout, interval, downtime_interval, downtime_duration)
       
    def addServiceDNS( self,
                       serviceid,
                       retries,
                       timeout,
                       interval, 
                       downtime_interval,
                       downtime_duration,
                       port,
                       lookup ):
        """
        addServiceDNS function
        """
        return self.delegate.addServiceDNS(serviceid, retries, timeout, interval, downtime_interval, downtime_duration, port, lookup)
       
    def addServiceTCP( self,
                       serviceid, 
                       retries, 
                       timeout, 
                       interval, 
                       downtime_interval,
                       downtime_duration, 
                       port, 
                       banner ):
        """
        addServiceTCP function
        """
        return self.delegate.addServiceTCP(serviceid, retries, timeout, interval, downtime_interval, downtime_duration, port, banner)
    
        """
        Java XMLRPC definition:
        boolean addServiceHTTP(String serviceId, int retries, int timeout, 
        int interval, int downTimeInterval, int downTimeDuration, String hostName,
        int port, String responseCode, String contentCheck, String url, 
        String user, String passwd, String agent) throws MalformedURLException;
        """
    def addServiceHTTP( self, 
                        serviceid, 
                        retries, 
                        timeout, 
                        interval, 
                        downtime_interval,
                        downtime_duration,
                        hostname, 
                        port,
                        response, 
                        response_text, 
                        url,
                        user,
                        password,
                        agent ):
        """
        addServiceHTTP function
        """        
        return self.delegate.addServiceHTTP(serviceid, retries, timeout, interval, downtime_interval, downtime_duration, hostname, port, response, response_text, url, user, password, agent)
       
    def addServiceHTTPS( self,
                         serviceid, 
                         retries, 
                         timeout, 
                         interval, 
                         downtime_interval,
                         downtime_duration, 
                         hostname,
                         port,
                         response, 
                         response_text, 
                         url,
                         user,
                         password,
                         agent ):
        """
        addServiceHTTPS function
        """
        return self.delegate.addServiceHTTPS(serviceid, retries, timeout, interval, downtime_interval, downtime_duration, hostname, port, response, response_text, url, user, password, agent)

    def addServiceDatabase( self, 
                            serviceid, 
                            retries, 
                            timeout, 
                            interval,
                            downtime_interval,
                            downtime_duration, 
                            user, 
                            password,
                            driver, 
                            url ):
        """
        addServiceDatabase function
        """
        return self.delegate.addServiceDatabase(serviceid, retries, timeout, interval, downtime_interval, downtime_duration, user, password, driver, url)
        
    def getServiceConfiguration( self,
                                 package,
                                 serviceid ):
        """
        getServiceConfiguration function
        """
        return self.delegate.getServiceConfiguration( package, serviceid )
