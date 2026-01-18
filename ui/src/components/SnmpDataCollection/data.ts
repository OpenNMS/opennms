import { SnmpCollectionSource } from '@/types/snmpDataCollection'

export const data: SnmpCollectionSource[] = [
  {
    id: 1,
    name: 'Cisco Devices',
    vendor: 'Cisco',
    description: 'Data collection source for Cisco devices',
    enabled: true,
    createdTime: new Date('2023-01-15T10:00:00Z'),
    lastModified: new Date('2023-06-10T12:30:00Z'),
    uploadedBy: 'admin'
  },
  {
    id: 2,
    name: 'Juniper Routers',
    vendor: 'Juniper',
    description: 'Data collection source for Juniper routers',
    enabled: false,
    createdTime: new Date('2023-02-20T14:15:00Z'),
    lastModified: new Date('2023-05-05T09:45:00Z'),
    uploadedBy: 'network_ops'
  },
  {
    id: 3,
    name: 'HP Switches',
    vendor: 'HP',
    description: 'Data collection source for HP switches',
    enabled: true,
    createdTime: new Date('2023-03-10T08:30:00Z'),
    lastModified: new Date('2023-04-22T11:20:00Z'),
    uploadedBy: 'tech_team'
  }
]
