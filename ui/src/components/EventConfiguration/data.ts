import { BreadCrumb } from '@/types'
import { EventConfSourceMetadata } from '@/types/eventConfig'

export const data: EventConfSourceMetadata[] = [
  {
    filename: 'example-event-config.xml',
    eventCount: 42,
    fileOrder: 1,
    username: 'admin',
    now: new Date(),
    vendor: 'OpenNMS',
    description: 'Example event configuration file for demonstration purposes.',
    id:1
  },
  {
    filename: 'another-event-config.xml',
    eventCount: 15,
    fileOrder: 2,
    username: 'user1',
    now: new Date(),
    vendor: 'OpenNMS',
    description: 'Another event configuration file with different settings.',
    id:2
  },
  {
    filename: 'custom-event-config.xml',
    eventCount: 30,
    fileOrder: 3,
    username: 'user2',
    now: new Date(),
    vendor: 'CustomVendor',
    description: 'Custom event configuration file for specific vendor requirements.',
    id:3
  }
]

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