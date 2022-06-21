import { describe, it, expect } from 'vitest'
import { formatTopologyGraphs, orderPowergridGraph } from './topology.helpers'
import { TopologyGraphList, TopologyGraph } from '@/types/topology'

describe('topology.helpers.ts', () => {
  describe('formatTopologyGraphs()', () => {
    const graphs: TopologyGraphList[] = [
      {
        'graphs': [
          {
            'namespace': 'application',
            'description': 'Displays all defined applications and their calculated states.',
            'label': 'Application Graph'
          }
        ],
        'description': 'Displays all defined applications and their calculated states.',
        'id': 'application',
        'label': 'Application Graph'
      },
      {
        'graphs': [
          {
            'namespace': 'bsm',
            'description': 'Displays the hierarchy of the defined Business Services and their computed operational states.',
            'label': 'Business Service Graph'
          }
        ],
        'description': 'Displays the hierarchy of the defined Business Services and their computed operational states.',
        'id': 'bsm',
        'label': 'Business Service Graph'
      },
      {
        'graphs': [
          {
            'namespace': 'powergrid:cities',
            'description': 'The Cities Layer.',
            'label': 'Cities'
          },
          {
            'namespace': 'powergrid:substations',
            'description': 'The Substations Layer.',
            'label': 'Substations'
          },
          {
            'namespace': 'powergrid:switches',
            'description': 'The Switches Layer.',
            'label': 'Switches'
          },
          {
            'namespace': 'powergrid:transformers',
            'description': 'The Transformers Layer.',
            'label': 'Transformers'
          }
        ],
        'id': 'cities.transformers.substations.switches',
        'label': 'PowerGrid'
      },
      {
        'graphs': [
          {
            'namespace': 'nodes',
            'description': 'This Topology Provider displays the topology information discovered by the Enhanced Linkd daemon. It uses the SNMP information of several protocols like OSPF, ISIS, LLDP and CDP to generate an overall topology.',
            'label': 'All'
          }
        ],
        'description': 'This Topology Provider displays the topology information discovered by the Enhanced Linkd daemon. It uses the SNMP information of several protocols like OSPF, ISIS, LLDP and CDP to generate an overall topology.',
        'id': 'nodes',
        'label': 'All'
      },
      {
        'graphs': [
          {
            'namespace': 'vmware',
            'description': 'The VMware Topology Provider displays the infrastructure information gathered by the VMware Provisioning process.',
            'label': 'VMware Topology Provider'
          }
        ],
        'description': 'The VMware Topology Provider displays the infrastructure information gathered by the VMware Provisioning process.',
        'id': 'vmware',
        'label': 'VMware Topology Provider'
      }
    ]
    const graphsFormatted: TopologyGraphList[] = [
      {
        'graphs': [
          {
            'namespace': 'application',
            'description': 'Displays all defined applications and their calculated states.',
            'label': 'Application Graph',
            'index': 0
          }
        ],
        'id': 'application',
        'label': 'Application Graph',
        'description': 'Displays all defined applications and their calculated states.',
        'type': 'application'
      },
      {
        'graphs': [
          {
            'namespace': 'bsm',
            'description': 'Displays the hierarchy of the defined Business Services and their computed operational states.',
            'label': 'Business Service Graph',
            'index': 0
          }
        ],
        'id': 'bsm',
        'label': 'Business Service Graph',
        'description': 'Displays the hierarchy of the defined Business Services and their computed operational states.',
        'type': 'bsm'
      },
      {
        'graphs': [
          {
            'namespace': 'powergrid:cities',
            'description': 'The Cities Layer.',
            'label': 'Cities',
            'index': 0
          },
          {
            'namespace': 'powergrid:substations',
            'description': 'The Substations Layer.',
            'label': 'Substations',
            'index': 1
          },
          {
            'namespace': 'powergrid:switches',
            'description': 'The Switches Layer.',
            'label': 'Switches',
            'index': 2
          },
          {
            'namespace': 'powergrid:transformers',
            'description': 'The Transformers Layer.',
            'label': 'Transformers',
            'index': 3
          }
        ],
        'id': 'cities.transformers.substations.switches',
        'label': 'PowerGrid',
        'type': 'powergrid'
      },
      {
        'graphs': [
          {
            'namespace': 'nodes',
            'description': 'This Topology Provider displays the topology information discovered by the Enhanced Linkd daemon. It uses the SNMP information of several protocols like OSPF, ISIS, LLDP and CDP to generate an overall topology.',
            'label': 'All',
            'index': 0
          }
        ],
        'id': 'nodes',
        'label': 'All',
        'description': 'This Topology Provider displays the topology information discovered by the Enhanced Linkd daemon. It uses the SNMP information of several protocols like OSPF, ISIS, LLDP and CDP to generate an overall topology.',
        'type': 'nodes'
      },
      {
        'graphs': [
          {
            'namespace': 'vmware',
            'description': 'The VMware Topology Provider displays the infrastructure information gathered by the VMware Provisioning process.',
            'label': 'VMware Topology Provider',
            'index': 0
          }
        ],
        'id': 'vmware',
        'label': 'VMware Topology Provider',
        'description': 'The VMware Topology Provider displays the infrastructure information gathered by the VMware Provisioning process.',
        'type': 'vmware'
      }
    ]

    it('should have added index and type properties', () => {
      expect(formatTopologyGraphs(graphs)).toEqual(graphsFormatted)
    })
    it('should return empty list', () => {
      expect(formatTopologyGraphs([])).toEqual([])
    })
  })

  describe('orderPowergridGraph()', ()=> {
    const id = 'switches.substations.transformers.cities'
    const graphs: TopologyGraph[] = [
      {
        'namespace': 'powergrid:cities',
        'description': 'The Cities Layer.',
        'label': 'Cities',
        'index': 0
      },
      {
        'namespace': 'powergrid:substations',
        'description': 'The Substations Layer.',
        'label': 'Substations',
        'index': 1
      },
      {
        'namespace': 'powergrid:switches',
        'description': 'The Switches Layer.',
        'label': 'Switches',
        'index': 2
      },
      {
        'namespace': 'powergrid:transformers',
        'description': 'The Transformers Layer.',
        'label': 'Transformers',
        'index': 3
      }
    ]
    const graphsOrdered: object = {
      graphs: [
        {
          'namespace': 'powergrid:switches',
          'description': 'The Switches Layer.',
          'label': 'Switches',
          'index': 0
        },
        {
          'namespace': 'powergrid:substations',
          'description': 'The Substations Layer.',
          'label': 'Substations',
          'index': 1
        },
        {
          'namespace': 'powergrid:transformers',
          'description': 'The Transformers Layer.',
          'label': 'Transformers',
          'index': 2
        },
        {
          'namespace': 'powergrid:cities',
          'description': 'The Cities Layer.',
          'label': 'Cities',
          'index': 3
        }
      ]
    }

    it('should return an ordered list', () => {
      expect(orderPowergridGraph(graphs, id)).toEqual(graphsOrdered)
    })
  })
})
