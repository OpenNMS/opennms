export const rescanCopy: Record<string, unknown> = {
  true: 'Scan New and Existing Nodes',
  dbonly: 'Database Steps Only',
  false: 'Scan New Nodes Only'
}

export const rescanItems = [
  { name: rescanCopy.true, value: 1 },
  { name: rescanCopy.false, value: 0 },
  { name: rescanCopy.dbonly, value: 2 }
]
