import { filesToFolders, sortFilesAndFolders, getExtensionFromFilenameSafely } from '@/components/FileEditor/utils'
import { IFile } from '../src/store/fileEditor/state'
import { assert, test, expect } from 'vitest'

test('Creating folders', () => {
  const sample: string[] = [
    'alarmd/drools-rules.d/alarmd.drl',
    'alarmd/drools-rules.d/situations.drl',
    'ami-config.xml'
  ]

  const result: IFile = {
    name: 'etc',
    children: [
      {
        name: 'alarmd',
        fullPath: 'alarmd',
        children: [
          {
            name: 'drools-rules.d',
            fullPath: 'alarmd/drools-rules.d',
            children: [
              {
                name: 'alarmd.drl',
                fullPath: 'alarmd/drools-rules.d/alarmd.drl'
              },
              {
                name: 'situations.drl',
                fullPath: 'alarmd/drools-rules.d/situations.drl'
              }
            ]
          }
        ]
      },
      {
        name: 'ami-config.xml',
        fullPath: 'ami-config.xml'
      }
    ]
  }

  expect(filesToFolders(sample)).toEqual(result)
})

test('Sorting folders and files', () => {
  const sample: IFile[] = [
    {
      name: 'Ztest',
      children: [{ name: 'Z' }, { name: 'A' }]
    },
    {
      name: 'Atest',
      children: [{ name: 'A' }]
    }
  ]

  const sorted = sortFilesAndFolders(sample)
  const firstFolder = sorted[0].name
  const secondFolderFirstFile = (sorted[1] as Required<IFile>).children[0].name

  assert.equal(firstFolder, 'Atest')
  assert.equal(secondFolderFirstFile, 'A')
})

test('Getting the file extension', () => {
  assert.equal(getExtensionFromFilenameSafely('test'), '')
  assert.equal(getExtensionFromFilenameSafely('test.xml'), 'xml')
  assert.equal(getExtensionFromFilenameSafely('test.properties'), 'properties')
})
