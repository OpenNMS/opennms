import { describe, expect, test } from 'vitest'
import { hasErrors, validateCollection, validateQuery, validateSource } from '@/lib/jdbcDataCollectionValidator'
import { getDefaultCollection } from '@/stores/jdbcDataCollectionStore'
import type { JdbcQuery } from '@/types/jdbcDataCollection'

const TYPES = ['gauge', 'counter', 'string']

const query = (overrides: Partial<JdbcQuery> = {}): JdbcQuery => ({
  name: 'nodes',
  recheckInterval: 0,
  ifType: 'ignore',
  resourceType: '',
  instanceColumn: '',
  queryString: 'SELECT COUNT(*) AS n FROM node',
  columns: [{ name: 'n', dataSourceName: '', type: 'gauge', alias: 'nodeCount' }],
  ...overrides
})

describe('jdbcDataCollectionValidator', () => {
  test('checks the source id', () => {
    expect(validateSource({ id: 'my-sql_2.0', collections: [] }, [])).toEqual({})
    expect(validateSource({ id: ' ', collections: [] }, []).id).toBe('Id is required.')
    expect(validateSource({ id: '../etc', collections: [] }, []).id).toContain('letter or digit')
    expect(validateSource({ id: 'mysql', collections: [] }, ['mysql']).id).toContain('already exists')
  })

  test('accepts the default collection with a name', () => {
    expect(hasErrors(validateCollection({ ...getDefaultCollection(), name: 'c' }, []))).toBe(false)
  })

  test('checks the collection settings', () => {
    const errors = validateCollection({ name: 'taken', rrdStep: 0, rras: ['RRA:AVG:0.5:1:1'], queries: [] }, ['taken'])

    expect(errors.name).toContain('unique across all sources')
    expect(errors.rrdStep).toContain('positive')
    expect(errors.rras).toContain('is not of the form')
    expect(validateCollection({ name: 'c', rrdStep: 300, rras: [], queries: [] }, []).rras).toBe('At least one RRA is required.')
    expect(validateCollection({ name: 'c', rrdStep: 300, rras: ['RRA:MAX:.5:288:366'], queries: [] }, [])).toEqual({})
  })

  test('accepts a valid query', () => {
    expect(validateQuery(query(), [], TYPES)).toEqual({})
  })

  test('checks the query fields', () => {
    const errors = validateQuery(query({ name: 'other', recheckInterval: -1, ifType: '', queryString: ' ' }), ['other'], TYPES)

    expect(errors.name).toContain('already has a query')
    expect(errors.recheckInterval).toBeDefined()
    expect(errors.ifType).toBeDefined()
    expect(errors.queryString).toBeDefined()
  })

  test('requires the resource type and the instance column together', () => {
    expect(validateQuery(query({ resourceType: 'pgDatabase' }), [], TYPES).instanceColumn).toBeDefined()
    expect(validateQuery(query({ instanceColumn: 'datname' }), [], TYPES).resourceType).toBeDefined()
    expect(validateQuery(query({ resourceType: 'pgDatabase', instanceColumn: 'datname' }), [], TYPES)).toEqual({})
  })

  test('checks every column and reports per row', () => {
    const errors = validateQuery(query({
      columns: [
        { name: 'a', type: 'gauge', alias: 'same' },
        { name: '', type: 'float', alias: 'same' },
        { name: 'c', type: 'string', alias: '12345678901234567890' }
      ]
    }), [], TYPES)

    expect(hasErrors(errors)).toBe(true)
    expect(errors.columnErrors?.[0]).toEqual({})
    expect(errors.columnErrors?.[1]).toEqual({ name: 'Name is required.', type: 'Type is required.', alias: 'Alias \'same\' is used by another column.' })
    expect(errors.columnErrors?.[2].alias).toBe('At most 19 characters.')
    expect(validateQuery(query({ columns: [] }), [], TYPES).columns).toBeDefined()
  })
})
