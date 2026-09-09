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
 * Copy text to the clipboard, over HTTPS or plain HTTP.
 *
 * Extracted from ConfigurationHelper (which still re-exports it, so its callers
 * are unchanged) so that copying does not require importing that module and its
 * cronstrue / ip-regex / is-valid-domain dependencies.
 *
 * The fallback is not optional polish: `navigator.clipboard` exists only in a
 * secure context, and an OpenNMS server reached over plain HTTP on anything but
 * localhost is not one — so on a typical deployment the modern API is simply
 * absent and the textarea path is what actually runs.
 *
 * Call this synchronously from the click handler. Both paths need the transient
 * user activation that a click provides, and awaiting anything first can lose it.
 */
export const copyToClipboard = (text: string): Promise<void> => {
  if (navigator.clipboard && window.isSecureContext) {
    return navigator.clipboard.writeText(text)
  }

  const textArea = document.createElement('textarea')
  textArea.value = text
  // Park it outside the viewport so selecting it does not scroll the page.
  textArea.style.position = 'fixed'
  textArea.style.left = '-999999px'
  textArea.style.top = '-999999px'
  document.body.appendChild(textArea)
  textArea.focus()
  textArea.select()

  return new Promise<void>((resolve, reject) => {
    try {
      if (document.execCommand('copy')) {
        resolve()
      } else {
        reject(new Error('Copy command was rejected by the browser.'))
      }
    } catch (error) {
      reject(error instanceof Error ? error : new Error(String(error)))
    } finally {
      textArea.remove()
    }
  })
}

const useClipboard = () => ({ copyToClipboard })

export default useClipboard
