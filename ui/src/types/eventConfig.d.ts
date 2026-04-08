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

import { CreateEditMode, Pagination, Sorting } from '.'

export type EventConfigSource = {
  id: number
  name: string
  vendor: string
  description: string
  enabled: boolean
  eventCount: number
  fileOrder: number
  uploadedBy: string
  createdTime: Date
  lastModified: Date
}

export type EventConfigEvent = {
  id: number
  uei: string
  eventLabel: string
  description: string
  severity: string
  enabled: boolean
  xmlContent: string
  createdTime: Date
  lastModified: Date
  modifiedBy: string
  sourceName: string
  vendor: string
  fileOrder: number
}

export type EventConfigStoreState = {
  sources: EventConfigSource[]
  sourcesPagination: Pagination
  sourcesSearchTerm: string
  sourcesSorting: Sorting
  isLoading: boolean
  activeTab: number
  uploadedSources: Array<UploadedSourceNamesResponse>
  uploadedEventConfigFilesReportDialogState: {
    visible: boolean
  }
  deleteEventConfigSourceDialogState: {
    visible: boolean
    eventConfigSource: EventConfigSource | null
  }
  changeEventConfigSourceStatusDialogState: {
    visible: boolean
    eventConfigSource: EventConfigSource | null
  }
  createEventConfigSourceDialogState: {
    visible: boolean
  }
}

export type EventConfigDetailStoreState = {
  events: EventConfigEvent[]
  eventsPagination: Pagination
  eventsSearchTerm: string
  eventsSorting: Sorting
  selectedSource: EventConfigSource | null
  isLoading: boolean
  deleteEventConfigEventDialogState: {
    visible: boolean
    eventConfigEvent: EventConfigEvent | null
  }
  changeEventConfigEventStatusDialogState: {
    visible: boolean
    eventConfigEvent: EventConfigEvent | null
  }
  deleteEventConfigSourceDialogState: {
    visible: boolean
    eventConfigSource: EventConfigSource | null
  }
  changeEventConfigSourceStatusDialogState: {
    visible: boolean
    eventConfigSource: EventConfigSource | null
  }
}

export type EventConfigFilesUploadResponse = {
  errors: [
    {
      file: string
      error: string
    }
  ]
  success: [
    {
      file: string
    }
  ]
}

export type EventConfigSourcesResponse = {
  sources: EventConfigSource[]
  totalRecords: number
}

export type EventConfigEventsResponse = {
  events: EventConfigEvent[]
  totalRecords: number
}

export interface DrawerState {
  visible: boolean
  isEventEditorModal: boolean
}

export type UploadEventFileType = {
  file: File
  isValid: boolean
  errors: string[]
  isDuplicate: boolean
}

export type EventModificationStoreState = {
  selectedSource: EventConfigSource | null
  eventModificationState: {
    isEditMode: CreateEditMode
    eventConfigEvent: EventConfigEvent | null
  }
}

export type EventFormErrors = {
  uei?: string
  eventLabel?: string
  description?: string
  severity?: string
  logmsg?: string
  dest?: string
  maskElements?: Array<{ name?: string; value?: string }>
  varbinds?: Array<{ index?: string; value?: string, type?: string }>
  varbindsDecode?: Array<{ parmId?: string; decode?: Array<{ key?: string; value?: string }> }>
  reductionKey?: string
  alarmType?: string
  clearKey?: string
}

export type UploadedSourceNamesResponse = {
  id: number
  name: string
}
