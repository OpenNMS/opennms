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

const numericSeverityLevel = (severity: string | undefined) => {
  if (severity) {
    switch (severity.toUpperCase()) {
      case 'NORMAL':
        return 11
      case 'WARNING':
        return 22
      case 'MINOR':
        return 33
      case 'MAJOR':
        return 44
      case 'CRITICAL':
        return 55
      default:
        return 0
    }
  }
  return 0
}

const stringToFixedFloat = (floatAsString: string, decimalPoints: number): string => {
  if (floatAsString) {
    const num = parseFloat(floatAsString)

    if (!Number.isNaN(num)) {
      return num.toFixed(decimalPoints)
    }
  }

  return floatAsString
}

export { numericSeverityLevel, stringToFixedFloat }
