/*
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
require('ipaddress-js');

test('isValidIPAddress(abc)', () => {
	expect(window.isValidIPAddress('abc')).toBeFalsy();
});
test('isValidIPAddress(1.2.3.4)', () => {
	expect(window.isValidIPAddress('1.2.3.4')).toBeTruthy();
});
test('isValidIPAddress(::1)', () => {
	expect(window.isValidIPAddress('::1')).toBeTruthy();
});

// ip-address >= 10.5.0 rejects leading-zero IPv4 octets (ambiguous: octal to
// C parsers, decimal to Java). Pin that deliberately — see NMS-20181.
test('isValidIPAddress rejects leading-zero octets', () => {
  expect(window.isValidIPAddress('010.1.1.1')).toBeFalsy();
  expect(window.isValidIPAddress('1.2.3.04')).toBeFalsy();
  expect(window.isValidIPAddress('01.02.03.04')).toBeFalsy();
  expect(window.isValidIPAddress('10.1.1.1')).toBeTruthy();
});

test('isValidIPAddressRange', () => {
  expect(window.checkIpRange('10.0.0.0', '10.0.0.10')).toBeTruthy();
});
test('checkIpRange rejects inverted IPv4 range', () => {
  expect(window.checkIpRange('10.0.0.10', '10.0.0.0')).toBeFalsy();
});
test('checkIpRange accepts IPv6 range', () => {
  expect(window.checkIpRange('::1', '::2')).toBeTruthy();
  expect(window.checkIpRange('2001:db8::1', '2001:db8::ffff')).toBeTruthy();
  expect(window.checkIpRange('fe80::1', 'fe80::1')).toBeTruthy();
});
test('checkIpRange rejects inverted IPv6 range', () => {
  expect(window.checkIpRange('::2', '::1')).toBeFalsy();
});
test('checkIpRange rejects mixed-family range', () => {
  expect(window.checkIpRange('10.0.0.1', '::1')).toBeFalsy();
  expect(window.checkIpRange('::1', '10.0.0.1')).toBeFalsy();
});