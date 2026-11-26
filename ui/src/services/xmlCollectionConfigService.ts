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

import { createResultWithPayload, createSuccessResponse } from '@/types/validation'
import type { ValidationResult, ValidationResultWithPayload } from '@/types/validation'
import type {
  XmlCollection,
  XmlCollectionMetadata,
  XmlCollectionSettings,
  XmlCollectionSummary,
  XmlGroupFile,
  XmlGroupFileSummary,
  XmlUploadResult
} from '@/types/xmlCollectionConfig'
import { v2 } from './axiosInstances'
import { failure, failureWithPayload, ifMatchHeaders } from './configServiceSupport'

const endpoint = '/xml-collection'

const encode = (name: string) => encodeURIComponent(name)

// -------------------------------------------------------------------- settings

export const getXmlCollectionSettings = async (): Promise<ValidationResultWithPayload<XmlCollectionSettings>> => {
  try {
    const response = await v2.get(`${endpoint}/settings`)
    return createResultWithPayload<XmlCollectionSettings>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<XmlCollectionSettings>(error, 'Failed to retrieve the XML collector settings.')
  }
}

export const updateXmlCollectionSettings = async (settings: XmlCollectionSettings): Promise<ValidationResult> => {
  try {
    await v2.put(`${endpoint}/settings`, settings)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, 'Failed to save the XML collector settings.')
  }
}

// ----------------------------------------------------------------- collections

export const getXmlCollections = async (): Promise<ValidationResultWithPayload<XmlCollectionSummary[]>> => {
  try {
    const response = await v2.get(`${endpoint}/collections`)
    return createResultWithPayload<XmlCollectionSummary[]>(true, '', response.data ?? [])
  } catch (error) {
    return failureWithPayload<XmlCollectionSummary[]>(error, 'Failed to retrieve the XML data collections.')
  }
}

export const getXmlCollection = async (name: string): Promise<ValidationResultWithPayload<XmlCollection>> => {
  try {
    const response = await v2.get(`${endpoint}/collections/${encode(name)}`)
    return createResultWithPayload<XmlCollection>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<XmlCollection>(error, `Failed to retrieve XML data collection '${name}'.`)
  }
}

export const createXmlCollection = async (collection: XmlCollection): Promise<ValidationResult> => {
  try {
    await v2.post(`${endpoint}/collections`, collection)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to create XML data collection '${collection.name}'.`)
  }
}

export const updateXmlCollection = async (name: string, collection: XmlCollection): Promise<ValidationResult> => {
  try {
    await v2.put(`${endpoint}/collections/${encode(name)}`, collection, ifMatchHeaders(collection.version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to save XML data collection '${name}'.`)
  }
}

export const deleteXmlCollection = async (name: string, version?: string): Promise<ValidationResult> => {
  try {
    await v2.delete(`${endpoint}/collections/${encode(name)}`, ifMatchHeaders(version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to delete XML data collection '${name}'.`)
  }
}

// ----------------------------------------------------------------- group files

export const getXmlGroupFiles = async (): Promise<ValidationResultWithPayload<XmlGroupFileSummary[]>> => {
  try {
    const response = await v2.get(`${endpoint}/groups`)
    return createResultWithPayload<XmlGroupFileSummary[]>(true, '', response.data ?? [])
  } catch (error) {
    return failureWithPayload<XmlGroupFileSummary[]>(error, 'Failed to retrieve the XML group files.')
  }
}

export const getXmlGroupFile = async (id: string): Promise<ValidationResultWithPayload<XmlGroupFile>> => {
  try {
    const response = await v2.get(`${endpoint}/groups/${encode(id)}`)
    return createResultWithPayload<XmlGroupFile>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<XmlGroupFile>(error, `Failed to retrieve group file '${id}'.`)
  }
}

export const createXmlGroupFile = async (groupFile: XmlGroupFile): Promise<ValidationResult> => {
  try {
    await v2.post(`${endpoint}/groups`, groupFile)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to create group file '${groupFile.id}'.`)
  }
}

export const updateXmlGroupFile = async (groupFile: XmlGroupFile): Promise<ValidationResult> => {
  try {
    await v2.put(`${endpoint}/groups/${encode(groupFile.id)}`, groupFile, ifMatchHeaders(groupFile.version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to save group file '${groupFile.id}'.`)
  }
}

export const deleteXmlGroupFile = async (id: string, version?: string): Promise<ValidationResult> => {
  try {
    await v2.delete(`${endpoint}/groups/${encode(id)}`, ifMatchHeaders(version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to delete group file '${id}'.`)
  }
}

// -------------------------------------------------------------------- metadata

export const getXmlCollectionMetadata = async (): Promise<ValidationResultWithPayload<XmlCollectionMetadata>> => {
  try {
    const response = await v2.get(`${endpoint}/metadata`)
    return createResultWithPayload<XmlCollectionMetadata>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<XmlCollectionMetadata>(error, 'Failed to retrieve the XML collector metadata.')
  }
}

// ----------------------------------------------------------- download / upload

export const downloadXmlCollectionConfiguration = async () => {
  try {
    return await v2.get(`${endpoint}/download`, { responseType: 'blob' })
  } catch (error) {
    console.error('Failed to download the XML collector configuration.', error)
    return false
  }
}

/** Replaces settings, collections and group files with a file produced by the download. */
export const uploadXmlCollectionConfiguration = async (file: File): Promise<ValidationResult> => {
  try {
    const formData = new FormData()
    formData.append('upload', file)
    await v2.post(`${endpoint}/upload`, formData)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, 'Failed to upload the XML collector configuration.')
  }
}

/**
 * Imports one file of the former on-disk format: an xml-datacollection-config.xml replaces every collection,
 * an xml-datacollection/*.xml creates or replaces the group file named after it (or given as id).
 */
export const uploadXmlCollectionXml = async (file: File, id?: string): Promise<ValidationResultWithPayload<XmlUploadResult>> => {
  try {
    const formData = new FormData()
    formData.append('upload', file)
    if (id) {
      formData.append('id', id)
    }
    const response = await v2.post(`${endpoint}/upload/xml`, formData)
    return createResultWithPayload<XmlUploadResult>(true, '', response.data)
  } catch (error) {
    return { ...failure(error, `Failed to import '${file.name}'.`), payload: undefined }
  }
}
