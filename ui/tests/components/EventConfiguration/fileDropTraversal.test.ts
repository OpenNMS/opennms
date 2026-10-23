import {
  collectDroppedFiles,
  DroppedEntry,
  filesFromEntries
} from '@/components/EventConfiguration/fileDropTraversal'
import { describe, expect, it } from 'vitest'

const fileEntry = (name: string): DroppedEntry => ({
  isFile: true,
  isDirectory: false,
  file: resolve => resolve(new File(['<x/>'], name, { type: 'text/xml' }))
})

// readEntries() hands out batches; the real API caps them at ~100 per call
const dirEntry = (children: DroppedEntry[], batchSize = 100): DroppedEntry => {
  let served = 0
  return {
    isFile: false,
    isDirectory: true,
    createReader: () => ({
      readEntries: (resolve) => {
        const batch = children.slice(served, served + batchSize)
        served += batch.length
        resolve(batch)
      }
    })
  }
}

describe('fileDropTraversal', () => {
  it('collects plain file entries', async () => {
    const files = await filesFromEntries([fileEntry('a.xml'), null, fileEntry('b.xml')])
    expect(files.map(f => f.name)).toEqual(['a.xml', 'b.xml'])
  })

  it('walks folders recursively', async () => {
    const files = await filesFromEntries([
      dirEntry([fileEntry('top.xml'), dirEntry([fileEntry('nested.xml')])])
    ])
    expect(files.map(f => f.name)).toEqual(['top.xml', 'nested.xml'])
  })

  it('drains readEntries batches until empty, not just the first one', async () => {
    const children = Array.from({ length: 233 }, (_, i) => fileEntry(`f${i}.xml`))
    const files = await filesFromEntries([dirEntry(children)])
    expect(files).toHaveLength(233)
  })

  it('falls back to the plain file list when entries are unavailable', async () => {
    const plain = new File(['<x/>'], 'plain.xml', { type: 'text/xml' })
    const dataTransfer = {
      items: [{ webkitGetAsEntry: () => null }],
      files: [plain]
    } as unknown as DataTransfer
    const files = await collectDroppedFiles(dataTransfer)
    expect(files.map(f => f.name)).toEqual(['plain.xml'])
  })

  it('prefers entries when any item provides one', async () => {
    const dataTransfer = {
      items: [{ webkitGetAsEntry: () => fileEntry('from-entry.xml') }],
      files: []
    } as unknown as DataTransfer
    const files = await collectDroppedFiles(dataTransfer)
    expect(files.map(f => f.name)).toEqual(['from-entry.xml'])
  })
})
