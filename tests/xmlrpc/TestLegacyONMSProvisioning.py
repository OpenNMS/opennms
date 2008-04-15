"""
This is a collection of unit tests to test the legacy OpenNMS provisioning
Note that it can take a while for these tests to run
To run:
    python ONMSLegacyTests.py
to run a specific test:
    python ONMSLegacyTests.py ONMSTest.testName
"""

import unittest
from openNMSlib import *
from TestXMLRPCServer import *

# This should be the server that ONMS is running on
TEST_SERVER = 'onms1.opennms.com'

# Set to True to get messages about what is happening
DEBUG = False

MONITORED_SERVICES = (
    'DNS',
    'HTTP',
    'HTTPS',
    'ICMP',
    'Postgres',
    'Telnet',
    'MySQL',
    'SMTP',
    'POP3',
    'IMAP',
    'FTP',
    'SSH',
)

server = TestXMLRPCServer()

def log(text):
    """ Simple function that prints text to the screen if in DEBUG mode """
    if DEBUG:
        print text
        
def logResults(heading, response):
    """ Helper function to print out the resulting information from a call """
    log("*** " + heading + " ***")
    log("    Event:   " + str(response.event))
    log("    txNo:    " + str(response.txNo))
    log("    uei:     " + str(response.uei))
    log("    message: " + str(response.message))
        
class ONMSTest(unittest.TestCase):
    def setUp(self):
        # Set up the ONMS connection
        self.connection = OpenNMSConnection()
        #self.connection.setDebug(1)  # Do we want this?
        self.connection.setSendHost('core.example.com')
        self.connection.setSource('CORE')
        self.connection.connect(TEST_SERVER)        
        # Set up the XMLRPC Server
        self.server = server
        # Set up the transaction number to use
        self.txNo = int(time.time())
        
    def tearDown(self):
        # Close the connection
        self.connection.close()
        
    def testAddInterface(self):
        # Send a service definition to ONMS
        self.txNo += 1
        self.connection.addInterface('192.168.0.1', 'testAddInterface', self.txNo)
        # Wait for a response
        response = self.server.run(self.txNo)
        logResults("testAddInterface -- results (Good)", response)
        self.assertEqual(response.event, EVENT.Success, "Adding an interface failed")
        
        # Try adding the interface again.  We should get a failure
        self.txNo += 1
        self.connection.addInterface('192.168.0.1', 'testAddInterface', self.txNo)
        # Wait for a response
        response = self.server.run(self.txNo)
        logResults("testAddInterface -- results (Bad)", response)
        self.assertEqual(response.event, EVENT.Failure, 
                    "Adding an interface that already existed should have failed")
        
        # Delete the interface
        self.txNo += 1
        self.connection.deleteInterface('192.168.0.1', 'testAddInterface', self.txNo)
        response = self.server.run(self.txNo)
        logResults("testAddInterface -- remove interface", response)
        
    def testDeleteInterface(self):
        # Add the interface
        self.txNo += 1
        self.connection.addInterface('192.168.0.2', 'testDeleteInterface', self.txNo)
        response = self.server.run(self.txNo)
        logResults("testDeleteInterface -- Add interface", response)
        
        # Try Deleting the interface
        self.txNo += 1
        self.connection.deleteInterface('192.168.0.2', 'testDeleteInterface',
                                        self.txNo)
        response = self.server.run(self.txNo)
        logResults("testDeleteInterface -- Delete interface", response)
        self.assertEqual(response.event, EVENT.Success,
                         "Deleting an interface failed")

        # Try deleting the interface again.
        # This doesn't return a failure
        self.txNo += 1
        self.connection.deleteInterface('192.168.0.2', 'testDeleteInterface', 
                                        self.txNo)
        response = self.server.run(self.txNo)
        logResults("testDeleteInterface -- Delete 2nd time", response)
        self.assertEqual(response.event, EVENT.Success, 
                    "Deleting an interface that doesn't exist should succeed")
        
    def testAddService(self):
        # First configure an interface
        self.txNo += 1
        self.connection.addInterface('192.168.0.3', 'testAddService', self.txNo)
        response = self.server.run(self.txNo)
        logResults('testAddService - Add interface', response)
        
        # Add all available services to the interface
        for service in MONITORED_SERVICES:
            self.txNo += 1
            self.connection.addService('192.168.0.3', service, self.txNo, 
                                       'testAddService')
            response = self.server.run(self.txNo)
            logResults('testAddService - %s' % service, response)
            self.assertEqual(response.event, EVENT.Success, 
                        "Adding service %s failed" % service)
        
        # Remove interface
        self.txNo += 1
        self.connection.deleteInterface('192.168.0.3', 'testAddService', self.txNo)
        response = self.server.run(self.txNo)
        logResults('testAddService - Remove interface', response)
        
        # Try adding a service to an interface that doesn't exist
        self.txNo += 1
        self.connection.addService('192.168.0.3', 'ICMP', self.txNo, 
                                   'testAddService')
        response = self.server.run(self.txNo)
        logResults('testAddService -- Adding to invalid interface', response)
        self.assertEqual(response.event, EVENT.Failure, 
                   "Adding a service to an interface that doesn't exist should fail")
        
    def testDeleteService(self):
        # First configure an interface
        self.txNo += 1
        self.connection.deleteInterface('192.168.0.4','testDeleteService', self.txNo)
        response = self.server.run(self.txNo)
        self.connection.addInterface('192.168.0.4','testDeleteService', self.txNo)
        response = self.server.run(self.txNo)
        logResults('testDeleteService -- Add interface', response)
        
        # Try deleting a service that doesn't exist
        self.txNo += 1
        self.connection.deleteService('192.168.0.4', 'ICMP', self.txNo, 
                                      'testDeleteService')
        response = self.server.run(self.txNo)
        logResults("testDeleteService - delete service that doesn't exist", response)
        self.assertEqual(response.event, EVENT.Success, 
                    "Deleting a service that doesn't exist should succeed")
        
        # Add a and delete all services
        for service in MONITORED_SERVICES:
            self.txNo += 1
            self.connection.addService('192.168.0.4', service, self.txNo, 
                                       'testDeleteService')
            response = self.server.run(self.txNo)
            logResults('testDeleteService - add %s' % service, response)
            
        # Now delete the services
        for service in MONITORED_SERVICES:   
            self.txNo += 1
            self.connection.deleteService('192.168.0.4', service, self.txNo, 
                                          'testDeleteService')
            response = self.server.run(self.txNo)
            logResults('testDeleteService - delete %s' % service, response)
            self.assertEqual(response.event, EVENT.Success, 
                        "Deleting service %s failed" % service)
        
        # Delete the interface
        self.txNo += 1
        self.connection.deleteInterface('192.168.0.4','testDeleteService', self.txNo)
        response = self.server.run(self.txNo)
        logResults('testDeleteService - Delete interface', response)
        
    def testDeleteServiceBug(self):
        """ This will test adding a service, removing the service, and adding it
            again. This will fail on the older version of ONMS.  The goal is that on
            a newer version, this will be fixed. 
        """
        # First configure an interface
        self.txNo += 1
        self.connection.deleteInterface('192.168.0.5','testDeleteServiceBug', 
                                         self.txNo)
        response = self.server.run(self.txNo)
        self.connection.addInterface('192.168.0.5','testDeleteServiceBug', self.txNo)
        response = self.server.run(self.txNo)
        logResults('testDeleteServiceBug -- Add interface', response)
        
        # Add a and delete all services
        for service in MONITORED_SERVICES:
            self.txNo += 1
            self.connection.addService('192.168.0.5', service, self.txNo, 
                                       'testDeleteServiceBug')
            response = self.server.run(self.txNo)
            logResults('testDeleteServiceBug - add %s' % service, response)
            
        # Now delete the services
        for service in MONITORED_SERVICES:   
            self.txNo += 1
            self.connection.deleteService('192.168.0.5', service, self.txNo, 
                                          'testDeleteServiceBug')
            response = self.server.run(self.txNo)
            logResults('testDeleteServiceBug - delete %s' % service, response)
            self.assertEqual(response.event, EVENT.Success, 
                        "Deleting service %s failed" % service)
        
        # Now try to add all the services again
        for service in MONITORED_SERVICES:
            self.txNo += 1
            self.connection.addService('192.168.0.5', service, self.txNo, 
                                       'testDeleteServiceBug')
            response = self.server.run(self.txNo)
            logResults('testDeleteServiceBug - add %s 2nd time' % service, response)
            self.assertEqual(response.event, EVENT.Success, 
                    """Adding service %s failed.
                       deleteService bug isn't fixed yet.
                       Note that this is expected to fail currently but should pass
                       when the bug is fixed in ONMS.
                    """ % service)
                    
        # Delete the interface
        self.txNo += 1
        self.connection.deleteInterface('192.168.0.5','testDeleteServiceBug',
                                        self.txNo)
        response = self.server.run(self.txNo)
        logResults('testDeleteService - Delete interface', response)
        
if __name__ == '__main__':
    # Perform the unit tests
    unittest.main();
    #server.run(0);
    #testRunner = unittest.TextTestRunner(verbosity=2)
    #print "\nTesting system calls"
    #testRunner.run(unittest.makeSuite(ONMSTest))
 
    
