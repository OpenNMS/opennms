import { describe, expect, test } from 'vitest'
import { mock } from 'vitest-mock-extended'
import { getTableCssClasses, hasEgressFlow, hasIngressFlow } from '@/components/Nodes/utils'
import { Node, NodeColumnSelectionItem } from '@/types'

describe('Nodes utils test', () => {
  test('test getTableCssClasses', async () => {
    let result = getTableCssClasses([])
    expect(result).toEqual(['tl1'])

    const defaultColumns: NodeColumnSelectionItem[] = [
      { id: 'id', label: 'ID', selected: true, order: 0 },
      { id: 'label', label: 'Node Label', selected: true, order: 1 },
      { id: 'ipaddress', label: 'IP Address', selected: true, order: 2 },
      { id: 'location', label: 'Location', selected: false, order: 3 },
      { id: 'foreignSource', label: 'Foreign Source', selected: true, order: 4 },
      { id: 'foreignId', label: 'Foreign ID', selected: true, order: 5 },
      { id: 'sysContact', label: 'Sys Contact', selected: true, order: 6 },
      { id: 'sysLocation', label: 'Sys Location', selected: true, order: 7 },
      { id: 'sysDescription', label: 'Sys Description', selected: true, order: 8 },
      { id: 'flows', label: 'Flows', selected: true, order: 9 }
    ]

    result = getTableCssClasses(defaultColumns)
    
    // tl1: implicit 'action' column
    // tr2: 'id' column is right-aligned
    // tc10: 'flows' column is center-aligned
    // 10 items: implicit action + 9 selected columns ('location' is unselected)
    expect(result).toEqual(['tl1', 'tr2', 'tl3', 'tl4', 'tl5', 'tl6', 'tl7', 'tl8', 'tl9', 'tc10'])
  })

  test('test hasEgressFlow, hasIngressFlow', async () => {
    const both = mock<Node>()
    both.lastEgressFlow = 1699909194000
    both.lastIngressFlow = 1699909194000

    expect(hasEgressFlow(both)).toBeTruthy()
    expect(hasIngressFlow(both)).toBeTruthy()

    const egress = mock<Node>()
    egress.lastEgressFlow = 1699909194000
    egress.lastIngressFlow = 0

    expect(hasEgressFlow(egress)).toBeTruthy()
    expect(hasIngressFlow(egress)).toBeFalsy()

    const ingress = mock<Node>()
    ingress.lastEgressFlow = 0
    ingress.lastIngressFlow = 1699909194000

    expect(hasEgressFlow(ingress)).toBeFalsy()
    expect(hasIngressFlow(ingress)).toBeTruthy()

    const neither = mock<Node>()
    neither.lastEgressFlow = 0
    neither.lastIngressFlow = 0

    expect(hasEgressFlow(neither)).toBeFalsy()
    expect(hasIngressFlow(neither)).toBeFalsy()
  })
})
