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

const isLegacyPluginUrl = (extensionId: string, moduleFileName: string) => {
  if (moduleFileName == 'uiextension' &&
     (extensionId === 'cloudUiExtension' || extensionId === 'uiextension' || extensionId === 'uiExtension')) {
    return true
  }

  return false
}

const externalComponent = (url: string) => {
  try {
    // this is the extensionId part of the url
    // should be used in the future when all plugins have unique extensionIds
    const extensionId = (
      url
        .split('/')
        .reverse()[1]
        .match(/^([^?]+)/) as any[]
    )[1]

    // for compatibility with legacy plugins
    const moduleFileName = (
      url
        .split('/')
        .reverse()[0]
        .match(/^(.*?)\.es/) as any[]
    )[1]

    const name = isLegacyPluginUrl(extensionId, moduleFileName) ? moduleFileName : extensionId

    if (window[name]) {
      return new Promise((resolve) => {
        resolve(window[name])
      })
    }

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    //@ts-ignore
    window[name] = new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.type = 'module'
      script.async = true
      script.crossOrigin = 'use-credentials'

      script.addEventListener('load', () => {
        resolve(window[name])
      })
      script.addEventListener('error', () => {
        reject(new Error(`Error loading ${url}`))
      })
      script.src = url
      document.head.appendChild(script)
    })

    return window[name] as any
  } catch (err) {
    console.log('Errors with component url.', url)
  }
}

const addStylesheet = (url: string) => {
  const exists = document.querySelector(`link[href='${url}']`)
  if (exists) return

  const head = document.head
  const link = document.createElement('link')

  link.type = 'text/css'
  link.rel = 'stylesheet'
  link.href = url

  head.prepend(link)
}

/**
 * Get the Rest url for the UiExtensionService Rest API (see features/ui-extension).
 * Accessing this url will return the Javascript module code for the plugin.
 */
const getJSPath = (baseRestUrl: string, extensionId: string, rootPath: string, fileName: string) => {
  return `${baseRestUrl}/plugins/ui-extension/module/${extensionId}?path=${rootPath}/${fileName}`
}

/**
 * Get the Rest url for the UiExtensionService Rest API (see features/ui-extension).
 * Accessing this url will return the CSS code for the plugin.
 */
const getCSSPath = (baseRestUrl: string, extensionId: string) => {
  return `${baseRestUrl}/plugins/ui-extension/css/${extensionId}`
}

export { externalComponent, addStylesheet, getJSPath, getCSSPath }
