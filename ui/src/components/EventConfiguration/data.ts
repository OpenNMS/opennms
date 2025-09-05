import { BreadCrumb } from '@/types'
import { EventConfEvent } from '@/types/eventConfig'

export const breadcrumbItems: BreadCrumb[] = [
  {
    label: 'Home',
    to: '/',
    isAbsoluteLink: false
  },
  {
    label: 'File Editor',
    to: '#',
    isAbsoluteLink: false,
    position: 'last'
  }
]

export const eventConfigEvents: EventConfEvent[] = [
  {
    id: 1,
    uei: 'uei-1',
    eventLabel: 'Event 1',
    description: 'Description for Event 1',
    enabled: true,
    xmlContent: '<xml>...</xml>',
    createdTime: new Date(),
    lastModified: new Date(),
    modifiedBy: 'admin',
    sourceName: 'Source 1',
    vendor: 'Vendor 1',
    fileOrder: 1
  },
  {
    id: 2,
    uei: 'uei-2',
    eventLabel: 'Event 2',
    description: 'Description for Event 2',
    enabled: false,
    xmlContent: '<xml>...</xml>',
    createdTime: new Date(),
    lastModified: new Date(),
    modifiedBy: 'user1',
    sourceName: 'Source 2',
    vendor: 'Vendor 2',
    fileOrder: 2
  },
  {
    id: 3,
    uei: 'uei-3',
    eventLabel: 'Event 3',
    description: 'Description for Event 3',
    enabled: true,
    xmlContent: '<xml>...</xml>',
    createdTime: new Date(),
    lastModified: new Date(),
    modifiedBy: 'user2',
    sourceName: 'Source 3',
    vendor: 'Vendor 3',
    fileOrder: 3
  }
]

