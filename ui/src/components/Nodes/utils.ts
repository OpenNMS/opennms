import {
  Node,
  NodeColumnSelectionItem
} from '@/types'
import { isNumber } from '@/lib/utils'

/**
 * Construct an array of Feather Table CSS classes for the given configured node table columns. 
 * These start with 't', then ('l', 'r', 'c') for (left, right, center), then the 1 based column index.
 * e.g. 'tl1': left-align 1st column
 * 'tr7': right-align 7th colunn
 */
export const getTableCssClasses = (columns: NodeColumnSelectionItem[]) => {
  const classes: string[] = columns.filter(col => col.selected).map((col, i) => {
    let t = 'tl'

    if (col.id === 'id') {
      t = 'tr'
    } else if (col.id === 'flows') {
      t = 'tc'
    }

    // +2 : one since Feather table column classes are 1-based, one for the first action column which isn't in 'columns'
    return `${t}${i + 2}`
  })

  // add 'action' column
  return ['tl1', ...classes]
}

export const hasIngressFlow = (node: Node) => {
  return node.lastIngressFlow && isNumber(node.lastIngressFlow)
}

export const hasEgressFlow = (node: Node) => {
  return node.lastEgressFlow && isNumber(node.lastEgressFlow)
}
