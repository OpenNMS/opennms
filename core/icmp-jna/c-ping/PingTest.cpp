/**
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

