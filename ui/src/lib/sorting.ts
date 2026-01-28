import { SORT } from '@featherds/table'
import { FeatherSortObject } from '@/types'

// Sort predicate function to use when both values are strings or can be cast to strings
export const sortPredicate = (a: any, b: any, currentSort: FeatherSortObject) => {
  const valueA = String(a[currentSort.property] ?? '')
  const valueB = String(b[currentSort.property] ?? '')

  let compareVal = valueA.localeCompare(valueB)

  if (currentSort.value === SORT.DESCENDING && compareVal !== 0) {
    compareVal = -compareVal
  }

  return compareVal
}
