export const rescanCopy: Record<string, unknown> = {
  true: 'Scan all nodes',
  dbonly: 'No scanning',
  false: 'Scan added nodes only'
}

export const rescanItems = [
  { name: rescanCopy.true, value: 1 },
  { name: rescanCopy.false, value: 0 },
  { name: rescanCopy.dbonly, value: 2 }
]
