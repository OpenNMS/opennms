///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

/**
 * Encode a value for a v2 REST FIQL search ('_s') that queryParametersHandler puts into the URL
 * without encoding.
 *
 * Free text can contain FIQL-structural characters (',' ';' '(' ')') and URL-structural
 * characters ('&' '#' '%' '+' space). Raw, they change the FIQL expression or break the URL.
 *
 * The value is URL-decoded two times on the server: one time by the servlet container, and one
 * time by CXF (search.decode.values=true on the v2 endpoints). CXF splits the FIQL expression on
 * ',' ';' '(' ')' before its decode, so a double-encoded value arrives as a literal. A strict
 * encoder is used because encodeURIComponent does not encode ! ' ( ) *.
 *
 * Note: '*' is ALWAYS a wildcard on the server, also when it is encoded here. CXF decodes the
 * value before it looks for '*', and it has no escape for '*'. So the encoding of '*' does not
 * make it literal. Use encodeFiqlPattern when a value can contain a wildcard.
 */
export const encodeFiqlValue = (value: string): string => {
  const strictEncode = (s: string): string =>
    encodeURIComponent(s).replace(/[!'()*]/g, c => '%' + c.charCodeAt(0).toString(16).toUpperCase())
  return strictEncode(strictEncode(value))
}

/**
 * Encode a FIQL value in which '*' is the wildcard. '*' stays raw and each segment between stars
 * is encoded with encodeFiqlValue, so all other characters arrive at the server as literals.
 * Leading, trailing and consecutive stars become empty segments, which encode to '' and so stay
 * unchanged.
 *
 * On the server, a value with a '*' uses SQL LIKE. CXF then escapes '%' and '_', so they are
 * literals. A value without '*' is an exact match.
 */
export const encodeFiqlPattern = (value: string): string => value.split('*').map(encodeFiqlValue).join('*')
