#include <cppunit/extensions/HelperMacros.h>

class PingTest : public CppUnit::TestFixture
{
  CPPUNIT_TEST_SUITE( PingTest );
  CPPUNIT_TEST( testCreateSocket );
  CPPUNIT_TEST( testCreateDatagram );
  CPPUNIT_TEST_SUITE_END();

public:
  void setUp();
  void tearDown();

  void testCreateSocket();
  void testCreateDatagram();
};


// Registers the fixture into the 'registry'
CPPUNIT_TEST_SUITE_REGISTRATION( PingTest );


void 
PingTest::setUp()
{
}


void 
PingTest::tearDown()
{
}


void 
PingTest::testCreateSocket()
{

}

void 
PingTest::testCreateDatagram()
{
  

}

