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

import { v2 } from './axiosInstances'
import {
  GenerateSystemReportRequest,
  GenerateSystemReportResult,
  SystemReportFormatter,
  SystemReportPlugin
} from '@/types/systemReport'

// null (not []) on failure so the page can distinguish an error from an empty set.
export const getSystemReportPlugins = async (): Promise<SystemReportPlugin[] | null> => {
  try {
    const resp = await v2.get('/system-report/plugins')
    return Array.isArray(resp.data) ? resp.data : []
  } catch (_err) {
    return null
  }
}

export const getSystemReportFormatters = async (): Promise<SystemReportFormatter[] | null> => {
  try {
    const resp = await v2.get('/system-report/formatters')
    return Array.isArray(resp.data) ? resp.data : []
  } catch (_err) {
    return null
  }
}

// must match SystemReportRestService.DOWNLOAD_COOKIE
const DOWNLOAD_COOKIE = 'systemReportDownload'

const hasDownloadCookie = (token: string) =>
  document.cookie.split(';').some(c => c.trim() === `${DOWNLOAD_COOKIE}=${token}`)

/**
 * Generate the report with a native form post into a hidden iframe, so the browser
 * streams the (possibly very large) file to disk instead of holding it in memory.
 * Resolves once the download starts (the server echoes the token in a cookie), or
 * when an error page loads in the iframe instead.
 */
export const generateSystemReport = async (request: GenerateSystemReportRequest): Promise<GenerateSystemReportResult> => {
  // A form post sends no X-Requested-With, so an expired session would get a Basic-auth
  // challenge in the hidden frame; check the session with a request that does first.
  try {
    await v2.get('/system-report/formatters', { headers: { 'X-Requested-With': 'XMLHttpRequest' }})
  } catch (err) {
    const status = (err as { response?: { status?: number }})?.response?.status
    return { success: false, message: status === 401 ? 'your session has expired. Log in again, then retry.' : undefined }
  }

  // not crypto.randomUUID(): it is missing outside secure contexts (plain-http installs)
  const token = `${Date.now().toString(36)}${Math.random().toString(36).slice(2)}`
  const frameName = `system-report-${token}`

  const iframe = document.createElement('iframe')
  iframe.name = frameName
  iframe.style.display = 'none'
  document.body.appendChild(iframe)

  const form = document.createElement('form')
  form.method = 'POST'
  form.action = `${v2.defaults.baseURL}/system-report/generate`
  form.target = frameName
  form.style.display = 'none'
  const fields: [string, string][] = [
    ['formatter', request.formatter],
    ...request.plugins.map(p => ['plugins', p] as [string, string]),
    ...(request.output ? [['output', request.output] as [string, string]] : []),
    ['downloadToken', token]
  ]
  for (const [name, value] of fields) {
    const input = document.createElement('input')
    input.type = 'hidden'
    input.name = name
    input.value = value
    form.appendChild(input)
  }

  return new Promise((resolve) => {
    const finish = (result: GenerateSystemReportResult) => {
      clearInterval(poll)
      iframe.removeEventListener('load', check)
      // leave the frame in place for a while so removing it cannot cancel a download it started
      setTimeout(() => iframe.remove(), result.success ? 60_000 : 0)
      resolve(result)
    }
    // Run on load and on every poll: Firefox fires no load event for an error or reset frame.
    const check = () => {
      if (hasDownloadCookie(token)) {
        finish({ success: true })
        return
      }
      // null for a cross-origin frame (SSO redirect, browser error page): a failure either way
      const doc = iframe.contentDocument
      if (!doc) {
        finish({ success: false })
        return
      }
      // still the frame's initial about:blank (request in flight), or what a download leaves behind
      if (doc.URL === 'about:blank' || doc.readyState !== 'complete') {
        return
      }
      // anything else that rendered is an error; only the server's short text/plain reasons are shown
      const text = doc.body?.textContent?.trim() ?? ''
      const isReason = doc.contentType === 'text/plain' && text.length > 0 && text.length <= 200 && !text.includes('\n')
      finish({ success: false, message: isReason ? text : undefined })
    }
    iframe.addEventListener('load', check)
    const poll = setInterval(check, 500)

    document.body.appendChild(form)
    form.submit()
    form.remove()
  })
}
