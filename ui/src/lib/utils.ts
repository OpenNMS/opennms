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

export const isNumber = (value: any) => {
  return value !== null && value !== undefined && typeof(value) === 'number'
}

export const isConvertibleToInteger = (value: any) => {
  if (value === null || value === undefined || value === '') {
    return false
  }

  const num = Number(value)

  return !Number.isNaN(num) && Number.isInteger(num)
}

/**
 * Returns true if value is non-null and is a primitive string or a String object
 */
export const isString = (value: any) => {
  return value !== null && (typeof(value) === 'string' || value instanceof String)
}

export const ellipsify = (text: string, count: number) => {
  if (text && count && text.length > count) {
    return text.substring(0, count) + '...'
  }

  return text
}

/**
 * Returns whether the object has at least one valid (non-empty) string property.
 */
export const hasNonEmptyProperty = (obj?: any) => {
  if (!obj) {
    return false
  }

  const keys = Object.getOwnPropertyNames(obj)

  return keys.some(k => {
    const value = (obj as any)[k]
    return value && isString(value) && value.length > 0
  })
}
