"""
This is a collection of unit tests for the new ONMS provisioning functionality.

Usage:
  To run all tests:
    python TestONMSCheckProvisioning.py
"""

import unittest
import xmlrpclib
from MockONMSXMLRPC import *
from DelegatingProvisioner import *
#from org.opennms.netmgt.xmlrpcd import OpenNMSProvisioner

class ONMSTest(unittest.TestCase):
    """
    This is base testing class, and handles the connection to the 
    XML-RPC server, and passes the test functions to the XML-RPC server.
    """
    
    def setUp(self):
        """
        Set up stuff for the tests
        """
        # Connect to the XML-RPC server
        #self.server = DelegatingProvisioner(MockONMSXMLRPC()) # Use this for testing
        self.server = DelegatingProvisioner(xmlrpclib.Server('http://localhost:9192/RPC2') )
        
class ONMSServiceTest(ONMSTest):
    """
    This is the base class for all the service unit tests
    """
    
    def setUp(self):
        "Set up stuff for the tests"
        # Run the base class setup
        ONMSTest.setUp(self)
        self.function = self._getFunction()
        self.parameters = self._getParameters()
        
    def _getFunction(self):
        "In the subclass, return the function to call"
        return None
        
    def _getParameters(self):
        "In the subclass, this should return a dict of the parameters to use"
        return {}
    
    def _testBadValue(self, test_parm, value, expected_fault = FAULT_DATA_INVALID):
        """
        This function tests bad values (usually out of bounds conditions).
        It expects the function to fail, and fails if the function passes or returns
        the incorrect failt code.
        
        Parameters:
                test_parm - which parameter (from parameters) to test
                value - value to test
                expected_fault - fault which should be thrown
        """
        parms = self.parameters.copy()
        parms[test_parm] = value
        try:
            self.function(**parms)
            self.fail('%s set to %s should return an error' % (test_parm, repr(value)))
        except xmlrpclib.Fault, fault:
            self.failIf(fault.faultCode != expected_fault,
                            'Returned Fault %d, expected Fault %d' %
                            (fault.faultCode, expected_fault))
    
    def _testGoodValues(self, test_parm, values):
        """
        This function tests good values.  It expects the function to be successful
        for each value.  If any value fails, this test will fail
        
        Parameters:
                test_parm - which parameter (from parameters) to test
                values - a list of values to test
        """
        parms = self.parameters.copy()
        # Try creating a config for all valid values
        for value in values:
            parms[test_parm] = value
            try:
                retVal = self.function(**parms)
                self.failIf(not retVal,
                            'Call failed with %s set to %s.' 
                            % (test_parm, repr(value)))
            except xmlrpclib.Fault, fault:
                self.fail('Call failed with %s set to %s. Fault: %d - %s' 
                          % (test_parm, repr(value), fault.faultCode, 
                             fault.faultString))
            service_id = parms['serviceid']
            retVal = self.server.getServiceConfiguration(service_id, service_id)
            retVal['serviceid'] = service_id
            if parms != retVal:
                self.fail('Resulting configuration is not correct\nTried:\n%s\nConfigured:\n%s' %
                           (parms, retVal))
             
    # The following unit tests will be run on all test cases
    def test_RetriesUnderBounds(self):
        "Test retries < 0"
        self._testBadValue('retries', -1)
                            
    def test_RetriesAllValues(self):
        "Test valid retries 0..3"
        self._testGoodValues('retries', range(0, 4))
                    
    def test_TimeoutUnderBounds(self):
        "Test timeout < 1"
        self._testBadValue('timeout', 0)

    def test_TimeoutAllValues(self):
        "Test all valid timeouts 3..15"
        self._testGoodValues('timeout', 
                             range(sectomilis(3), sectomilis(16), sectomilis(1)))
                    
    def test_IntervalUnderBounds(self):
        "Test interval < 1"
        self._testBadValue('interval', 0)
            
    def test_IntervalAllData(self):
        "Test all valid intervals 1..60"
        self._testGoodValues('interval', 
                             range(mintomilis(1),mintomilis(61), mintomilis(1)))
                    
    def test_DowntimeIntervalUnderBounds(self):
        "Test downtime_interval < 1"
        self._testBadValue('downtime_interval', 0)
            
    def test_DowntimeIntervalAllData(self):
        "Test all valid downtime_intervals 1..60"
        self._testGoodValues('downtime_interval', 
                                 range(mintomilis(1),mintomilis(61),mintomilis(1)))
                              
    def test_DowntimeDurationUnderBounds(self):
        "Test downtime_duration < 1"
        self._testBadValue('downtime_duration', 0)
            
    def test_DowntimeDurationAllData(self):
        "Test all valid downtime_durations 1..60"
        self._testGoodValues('downtime_duration', 
                                 range(mintomilis(1),mintomilis(61), mintomilis(1)))
                    
    def test_PortUnderBounds(self):
        "Test port < 1"
        if self.parameters.has_key('port'):
            self._testBadValue('port', 0)
            
    def test_PortOverBounds(self):
        "Test port > 65535"
        if self.parameters.has_key('port'):
            self._testBadValue('port', 65536)
        
    def test_PortAllData(self):
        "Test all valid ports 1..65535"
        if self.parameters.has_key('port'):
            self._testGoodValues('port', range(1,65536,100))
            
    def test_HostnameOverBounds(self):
        "Test hostname > 512 characters"
        if self.parameters.has_key('hostname'):
            testdata = 'a'*513
            self._testBadValue('hostname', testdata)
            
    def test_HostnameGoodData(self):
        "Test hostname with good data"
        if self.parameters.has_key('hostname'):
            testdata = ('', 'a'*512)
            self._testGoodValues('hostname', testdata)
            
    def test_ContentCheckOverBounds(self):
        "Test content_check > 128 characters"
        if self.parameters.has_key('content_check'):
            testdata = 'a'*129
            self._testBadValue('content_check', testdata)
            
    def test_ContentCheckGoodData(self):
        "Test content_check with good data"
        if self.parameters.has_key('content_check'):
            testdata = ('', 'a'*128)
            self._testGoodValues('content_check', testdata)
            
    def test_ResponseCodeUnderBounds(self):
        "Test response_code < 100"
        if self.parameters.has_key('response_code'):
            self._testBadValue('response_code', '99')
            
    def test_ResponseCodeOverBounds(self):
        "Test response_code > 599"
        if self.parameters.has_key('response_code'):
            self._testBadValue('response_code', '600')
            
    def test_ResponseCodeAllData(self):
        "Test all valid response_codes 100..599"
        if self.parameters.has_key('response_code'):
            self._testGoodValues('response_code', 
                                 [str(code) for code in range(100,600)])
                                 
    def test_ResponseCodeRange(self):
        "Test response code range 200-300"
        if self.parameters.has_key('response_code'):
            self._testGoodValues('response_code', ('200-300',))
            
    def test_DefaultResponseCode(self):
        "Test a default response code (blank)"
        if self.parameters.has_key('response_code'):
            self._testGoodValues('response_code', (''))
            
    def test_URLOverBounds(self):
        "Test URL > 512 characters"
        if self.parameters.has_key('url'):
            testdata = '/'+'a'*512
            self._testBadValue('url', testdata)
            
    def test_URLUnderBounds(self):
        "Test URL == ''"
        if self.parameters.has_key('url'):
            self._testBadValue('url', '')
            
    def test_URLGoodData(self):
        "Test URL with good data"
        if self.parameters.has_key('url'):
            testdata = ('/', '/'+'a'*511)
            self._testGoodValues('url', testdata)
            
    def test_URLBadProtocol(self):
        "Test URL has a bad protocol"
        pass
#        if self.parameters.has_key('url'):
#            self._testBadValue('url', 'a.com', FAULT_URL_INVALID)
            
    def test_UserOverBounds(self):
        "Test user > 64 characters"
        if self.parameters.has_key('user'):
            testdata = 'a'*65
            self._testBadValue('user', testdata)
            
    def test_UserGoodData(self):
        "Test username with good data"
        if self.parameters.has_key('user'):
            testdata = ('', 'a'*64)
            self._testGoodValues('user', testdata)
            
    def test_PasswordOverBounds(self):
        "Test password > 64 characters"
        if self.parameters.has_key('password'):
            testdata = 'a'*65
            self._testBadValue('password', testdata)
            
    def test_PasswordGoodData(self):
        "Test password with good data"
        if self.parameters.has_key('password'):
            testdata = ('', 'a'*64)
            self._testGoodValues('password', testdata)
            
    def test_DriverOverBounds(self):
        "Test driver > 128 characters"
        if self.parameters.has_key('driver'):
            testdata = 'a'*129
            self._testBadValue('driver', testdata)
            
    def test_DriverUnderBounds(self):
        "Test driver == ''"
        if self.parameters.has_key('driver'):
            self._testBadValue('driver', '')
            
    def test_DriverGoodData(self):
        "Test driver with good data"
        if self.parameters.has_key('driver'):
            testdata = ('a', 'a'*128)
            self._testGoodValues('driver', testdata)
            
class TestSystemCalls(ONMSTest):

    def test_listMethods(self):
        "Test the system.listMethods function"
        methods = self.server.system.listMethods()
        # Test that it returns a list of items
        self.failUnless(isinstance(methods, list))
        
    def test_methodHelp(self):
        "Test the system.methodHelp function"
        methods = self.server.system.listMethods()
        # Itterate over each method
        for method in methods:
            help = self.server.system.methodHelp(method)
            # Test that it returns a string
            self.failUnless(isinstance(help, basestring))
        
class TestAddServiceICMP(ONMSServiceTest):
    """
    Test the AddServiceICMP function
    """

    def _getFunction(self):
        return self.server.addServiceICMP
        
    def _getParameters(self):
        return { 'serviceid'         : 'RS-ICMP-1',
                 'retries'           : 1,
                 'timeout'           : sectomilis(1),
                 'interval'          : mintomilis(1),
                 'downtime_interval' : mintomilis(1),
                 'downtime_duration' : mintomilis(1) }

class TestAddServiceDNS(ONMSServiceTest):
          
    def _getFunction(self):
        return self.server.addServiceDNS
        
    def _getParameters(self):
        return { 'serviceid'         : 'RS-DNS-1',
                 'retries'           : 1,
                 'timeout'           : sectomilis(1),
                 'interval'          : mintomilis(1),
                 'downtime_interval' : mintomilis(1),
                 'downtime_duration' : mintomilis(1),
                 'port'              : 1,
                 'lookup'          : '' } 
    
class TestAddServiceTCP(ONMSServiceTest):
    
    def _getFunction(self):
        return self.server.addServiceTCP
        
    def _getParameters(self):
        return { 'serviceid'         : 'RS-TCP-1',
                 'retries'           : 1,
                 'timeout'           : sectomilis(1),
                 'interval'          : mintomilis(1),
                 'downtime_interval' : mintomilis(1),
                 'downtime_duration' : mintomilis(1),
                 'port'              : 1,
                 'banner'            : '' }
                     
class TestAddServiceHTTP(ONMSServiceTest):
    
    def _getFunction(self):
        return self.server.addServiceHTTP
        
    def _getParameters(self):
        return { 'serviceid'         : 'RS-HTTP-1',
                 'retries'           : 1,
                 'timeout'           : sectomilis(1),
                 'interval'          : mintomilis(1),
                 'downtime_interval' : mintomilis(1),
                 'downtime_duration' : mintomilis(1),
                 'port'              : 1,
                 'hostname'          : 'example.com',
                 'response'          : '',
                 'response_text'     : '',
                 'url'               : '/',
                 'user'          : '',
                 'password'          : '',
                 'agent'             : '' }
    
class TestAddServiceHTTPS(ONMSServiceTest):
    
    def _getFunction(self):
        return self.server.addServiceHTTPS
        
    def _getParameters(self):
        return { 'serviceid'         : 'RS-HTTPS-1',
                 'retries'           : 1,
                 'timeout'           : sectomilis(1),
                 'interval'          : mintomilis(1),
                 'downtime_interval' : mintomilis(1),
                 'downtime_duration' : mintomilis(1),
                 'port'              : 1,
                 'hostname'          : 'example.com',
                 'response'          : '',
                 'response_text'     : '',
                 'url'               : '/index.html',
                 'user'          : '',
                 'password'          : '',
                 'agent'             : '' }
    
class TestAddServiceDatabase(ONMSServiceTest):
    
    def _getFunction(self):
        return self.server.addServiceDatabase
        
    def _getParameters(self):
        return { 'serviceid'         : 'RS-DB-1',
                 'retries'           : 1,
                 'timeout'           : sectomilis(1),
                 'interval'          : mintomilis(1),
                 'downtime_interval' : mintomilis(1),
                 'downtime_duration' : mintomilis(1),
                 'user'              : '', 
                 'password'          : '',
                 'driver'            : 'driver', 
                 'url'               : 'jdbc://a.com' }
          
if __name__ == '__main__':
    # Perform the unit tests
    testRunner = unittest.TextTestRunner(verbosity=2)
    #print "\nTesting system calls"
    #testRunner.run(unittest.makeSuite(TestSystemCalls))
    print "\nTesting TestAddServiceICMP"
    testRunner.run(unittest.makeSuite(TestAddServiceICMP))
    print "\nTesting TestAddServiceDNS"
    testRunner.run(unittest.makeSuite(TestAddServiceDNS))
    print "\nTesting TestAddServiceTCP"
    testRunner.run(unittest.makeSuite(TestAddServiceTCP))
    print "\nTesting TestAddServiceHTTP"
    testRunner.run(unittest.makeSuite(TestAddServiceHTTP))
    print "\nTesting TestAddServiceHTTPS"
    testRunner.run(unittest.makeSuite(TestAddServiceHTTPS))
    print "\nTesting TestAddServiceDatabase"
    testRunner.run(unittest.makeSuite(TestAddServiceDatabase))
    
