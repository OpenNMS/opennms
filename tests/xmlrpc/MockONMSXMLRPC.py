"""
This creates a fake XMLRPC server object which is used to test the unit tests
with until the real system is done
"""
import xmlrpclib

# Define the XML-RPC error numbers
FAULT_DATA_INVALID = 1
FAULT_URL_INVALID = 2

# Define the XML-RPC error message
# Note that these are just generic
error_messages = {
    FAULT_DATA_INVALID : 'Invalid data',
    FAULT_URL_INVALID : 'Invalid URL',
}

def mintomilis(min):
    """
    Helper function that converts minutes to miliseconds
    """
    return min*60*1000
    
def sectomilis(sec):
    """
    Helper function that converts seconds to miliseconds
    """
    return sec*1000
    
class SystemMethods:
    """
    Helper class for MockONMSXMLRPC
    """
    def __init__(self):
        self.methods = [
        'AddServiceICMP',
        'AddServiceDNS',
        'AddServiceTCP',
        'AddServiceHTTP',
        'AddServiceHTTPS',
        'AddServiceDatabase',
        ]
        self.helpstrings = {
        'AddServiceICMP'     : 'ICMP help string goes here',
        'AddServiceDNS'      : 'DNS help string goes here',
        'AddServiceTCP'      : 'TCP help string goes here',
        'AddServiceHTTP'     : 'HTTP help string goes here',
        'AddServiceHTTPS'    : 'HTTPS help string goes here',
        'AddServiceDatabase' : 'Database help string goes here',
        }
        
    def listMethods(self):
        """
        Returns a list of methods available on the XML-RPC server
        """
        return self.methods
        
    def methodHelp(self, method):
        """
        Returns a help string for the given method
        """
        try:
            help = self.helpstrings[method]
        except:
            help = ''
        return help
        
class MockONMSXMLRPC:
    """
    This is a mock object that pretends to be the XML-RPC server. 
    Each function implements enough to return the correct test data.
    """
    def __init__(self):
        # Set up the system methods
        self.system = SystemMethods()
        self.configs = {}
        
    def Fault(self, error_number):
        # Function that emulates sending an XML-RPC fault
        raise xmlrpclib.Fault(error_number, error_messages[error_number])
        
    def _checkRetries(self, retries):
        # Check if the retries value is valid
        if not isinstance(retries, int):
            self.Fault(FAULT_DATA_INVALID)
        if retries < 0: #or retries > 3:
            self.Fault(FAULT_DATA_INVALID)
            
    def _checkTimeout(self, timeout):
        # Check if the timeout value is valid
        if not isinstance(timeout, int):
            self.Fault(FAULT_DATA_INVALID)
        if timeout < sectomilis(1): #or timeout > mintomilis(60):
            self.Fault(FAULT_DATA_INVALID)
            
    def _checkInterval(self, interval):
        # Check if the interval value is valid
        if not isinstance(interval, int):
            self.Fault(FAULT_DATA_INVALID)
        if interval < mintomilis(1): #or interval > mintomilis(60):
            self.Fault(FAULT_DATA_INVALID)
            
    def _checkDowntimeInterval(self, interval):
        self._checkInterval(interval)
        
    def _checkDowntimeDuration(self, duration):
        self._checkInterval(duration)
        
    def _checkPort(self, port):
        if not isinstance(port, int):
            self.Fault(FAULT_DATA_INVALID)
        if port < 1 or port > 65535:
            self.Fault(FAULT_DATA_INVALID)

    def _checkHostname(self, hostname):
        if not isinstance(hostname, basestring):
            self.Fault(FAULT_DATA_INVALID)
        if len(hostname) > 512:
            self.Fault(FAULT_DATA_INVALID)
                        
    def _checkURL(self, url):
        if not isinstance(url, basestring):
            self.Fault(FAULT_DATA_INVALID)
        if len(url) > 512:
            self.Fault(FAULT_DATA_INVALID)
        if len(url) == 0:
            self.Fault(FAULT_DATA_INVALID)
            
    def _checkContentCheck(self, check):
        if not isinstance(check, basestring):
            self.Fault(FAULT_DATA_INVALID)
        if len(check) > 128:
            self.Fault(FAULT_DATA_INVALID)
            
    def _checkResponseCode(self, code):
        if not isinstance(code, int):
            self.Fault(FAULT_DATA_INVALID)
        if code < 100 or code > 599:
            self.Fault(FAULT_DATA_INVALID)
            
    def _checkUsername(self, name):
        if not isinstance(name, basestring):
            self.Fault(FAULT_DATA_INVALID)
        if len(name) > 64:
            self.Fault(FAULT_DATA_INVALID)
            
    def _checkPassword(self, pw):
        if not isinstance(pw, basestring):
            self.Fault(FAULT_DATA_INVALID)
        if len(pw) > 64:
            self.Fault(FAULT_DATA_INVALID)
            
    def _checkDriver(self, driver):
        if not isinstance(driver, basestring):
            self.Fault(FAULT_DATA_INVALID)
        if len(driver) > 128:
            self.Fault(FAULT_DATA_INVALID)
        if driver == '':
            self.Fault(FAULT_DATA_INVALID)
        
    def _validateSchedule(self, retries, timeout, interval,
                          downtime_interval, downtime_duration):
        self._checkRetries(retries)
        self._checkTimeout(timeout)
        self._checkInterval(interval)
        self._checkDowntimeInterval(downtime_interval)
        self._checkDowntimeDuration(downtime_duration)
        
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
        self._validateSchedule(retries, timeout, interval,
                              downtime_interval, downtime_duration)
        # Set the configuration
        self.configs[serviceid] = {
                                      'retries':retries,
                                      'timeout':timeout,
                                      'interval':interval,
                                      'downtime_interval':downtime_interval,
                                      'downtime_duration':downtime_duration
                                  }
        return True
       
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
        self._validateSchedule(retries, timeout, interval,
                              downtime_interval, downtime_duration)
        self._checkPort(port)
        self._checkHostname(lookup)
        # Set the configuration
        self.configs[serviceid] = {
                                      'retries':retries,
                                      'timeout':timeout,
                                      'interval':interval,
                                      'downtime_interval':downtime_interval,
                                      'downtime_duration':downtime_duration,
                                      'port':port,
                                      'lookup':lookup
                                  }
        return True
       
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
        self._validateSchedule(retries, timeout, interval,
                              downtime_interval, downtime_duration)
        self._checkPort(port)
        self._checkContentCheck(banner)
        # Set the configuration
        self.configs[serviceid] = {
                                      'retries':retries,
                                      'timeout':timeout,
                                      'interval':interval,
                                      'downtime_interval':downtime_interval,
                                      'downtime_duration':downtime_duration,
                                      'port':port,
                                      'banner':banner,
                                  }
        return True
       
    def addServiceHTTP( self, 
                        serviceid, 
                        retries, 
                        timeout, 
                        interval, 
                        downtime_interval,
                        downtime_duration, 
                        port, 
                        response, 
                        response_text, 
                        url ):
        """
        addServiceHTTP function
        """
        self._validateSchedule(retries, timeout, interval,
                              downtime_interval, downtime_duration)
        self._checkPort(port)
        self._checkResponseCode(response)
        self._checkContentCheck(response_text)
        self._checkURL(url)
        # Set the configuration
        self.configs[serviceid] = {
                                      'retries':retries,
                                      'timeout':timeout,
                                      'interval':interval,
                                      'downtime_interval':downtime_interval,
                                      'downtime_duration':downtime_duration,
                                      'port':port,
                                      'response':response,
                                      'response_text':response_text,
                                      'url':url
                                  }
        return True
       
    def addServiceHTTPS( self,
                         serviceid, 
                         retries, 
                         timeout, 
                         interval,
                         downtime_interval,
                         downtime_duration, 
                         port, 
                         response, 
                         response_text, 
                         url ):
        """
        addServiceHTTPS function
        """
        self._validateSchedule(retries, timeout, interval,
                              downtime_interval, downtime_duration)
        self._checkPort(port)
        self._checkResponseCode(response)
        self._checkContentCheck(response_text)
        self._checkURL(url)
        # Set the configuration
        self.configs[serviceid] = {
                                      'retries':retries,
                                      'timeout':timeout,
                                      'interval':interval,
                                      'downtime_interval':downtime_interval,
                                      'downtime_duration':downtime_duration,
                                      'port':port,
                                      'response':response,
                                      'response_text':response_text,
                                      'url':url
                                  }
        return True
       
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
        self._validateSchedule(retries, timeout, interval,
                              downtime_interval, downtime_duration)
        self._checkUsername(user)
        self._checkPassword(password)
        self._checkDriver(driver)
        self._checkURL(url)
        # Set the configuration
        self.configs[serviceid] = {
                                      'retries':retries,
                                      'timeout':timeout,
                                      'interval':interval,
                                      'downtime_interval':downtime_interval,
                                      'downtime_duration':downtime_duration,
                                      'user':user,
                                      'password':password,
                                      'driver':driver,
                                      'url':url
                                  }
        return True
        
    def getServiceConfiguration( self,
                                 package,
                                 serviceid):
        """
        getServiceConfiguration function
        """
        # Note that for our purposes we do not worry about the package
        # For testing, send the serviceID as the package
        if self.configs.has_key(serviceid):
            return self.configs[serviceid]
        else:
            return {}