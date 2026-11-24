///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { rest, v2 } from './axiosInstances'
import { serverMessage } from './serverMessage'
import {
  GraphCollection,
  GraphCollectionGraph,
  GraphCollectionTimespan,
  GraphCollectionWithEtag,
  ResolveOptions,
  ResolvedGraphCollection,
  SyntheticGraphCollectionKind
} from '@/types/graphCollection'
import { createResultWithPayload, ValidationResult, ValidationResultWithPayload } from '@/types/validation'

// Graph Collections are the KSC reports of old. This talks to /api/v2/graph-collections;
// see docs/modules/development/pages/rest/graph_collections.adoc for the contract.
const endpoint = '/graph-collections'

const seg = (value: string | number): string => encodeURIComponent(String(value))

/**
 * The server's explanation of a failed write, or the fallback. A 400 body is plain text listing
 * every problem one per line; 412 and 404 bodies are one sentence; 403 carries no useful body.
 */
export const graphCollectionErrorMessage = (err: any, fallback: string): string => {
  const status = err?.response?.status
  if (status === 403) {
    return 'You do not have permission to change graph collections.'
  }
  return explanation(err) || fallback
}

/** The one-per-line problems of a 400 body, for a form that can show them next to the fields. */
export const graphCollectionProblems = (err: any): string[] => {
  if (err?.response?.status !== 400) {
    return []
  }
  return explanation(err).split('\n').map((line: string) => line.trim()).filter(Boolean)
}

// the API's own explanation of a failed call, or '' (see serverMessage)
const explanation = (err: any): string => serverMessage(err, '')

/** True when a write was refused because the collection changed since it was read (412). */
export const isStaleWrite = (err: any): boolean => err?.response?.status === 412

/** A result that also carries the HTTP status of a failure, so an editor can tell a 412 from a 400. */
export interface GraphCollectionResult<T> extends ValidationResultWithPayload<T> {
  status?: number
}

/** True when a failed result came from a 412; see isStaleWrite. */
export const isStaleResult = (result: GraphCollectionResult<unknown>): boolean => result.status === 412

const failure = <T>(err: any, fallback: string): GraphCollectionResult<T> => {
  const problems = graphCollectionProblems(err)
  const result: GraphCollectionResult<T> = createResultWithPayload<T>(false, graphCollectionErrorMessage(err, fallback))
  if (problems.length > 1) {
    result.errors = problems
  }
  if (typeof err?.response?.status === 'number') {
    result.status = err.response.status
  }
  return result
}

// axios lowercases response header names
const etagOf = (resp: { headers?: Record<string, unknown> }): string | null => {
  const etag = resp.headers?.etag
  return typeof etag === 'string' && etag ? etag : null
}

const ifMatch = (etag?: string | null) => (etag ? { headers: { 'If-Match': etag }} : {})

const resolveParams = (options?: ResolveOptions) => {
  const params: Record<string, string> = {}
  if (options?.timespan) {
    params.timespan = options.timespan
  }
  if (options?.graphtype) {
    params.graphtype = options.graphtype
  }
  return params
}

/** Every collection, with its graphs, sorted by title by the server. */
export const getGraphCollections = async (): Promise<ValidationResultWithPayload<GraphCollection[]>> => {
  try {
    const resp = await v2.get(endpoint)
    return createResultWithPayload(true, '', Array.isArray(resp.data) ? resp.data as GraphCollection[] : [])
  } catch (err: any) {
    return failure(err, 'Failed to load the graph collections.')
  }
}

/** One collection with the ETag to send back as If-Match when saving it. */
export const getGraphCollection = async (id: number): Promise<ValidationResultWithPayload<GraphCollectionWithEtag>> => {
  try {
    const resp = await v2.get(`${endpoint}/${seg(id)}`)
    return createResultWithPayload(true, '', { collection: resp.data as GraphCollection, etag: etagOf(resp) })
  } catch (err: any) {
    return failure(err, `Failed to load graph collection ${id}.`)
  }
}

export const getGraphCollectionTimespans = async (): Promise<ValidationResultWithPayload<GraphCollectionTimespan[]>> => {
  try {
    const resp = await v2.get(`${endpoint}/timespans`)
    return createResultWithPayload(true, '', Array.isArray(resp.data) ? resp.data as GraphCollectionTimespan[] : [])
  } catch (err: any) {
    return failure(err, 'Failed to load the graph timespans.')
  }
}

/** A stored collection prepared for rendering, optionally with a timespan or graph type override. */
export const getResolvedGraphCollection = async (
  id: number,
  options?: ResolveOptions
): Promise<ValidationResultWithPayload<GraphCollectionWithEtag<ResolvedGraphCollection>>> => {
  try {
    const resp = await v2.get(`${endpoint}/${seg(id)}/resolved`, { params: resolveParams(options) })
    return createResultWithPayload(true, '', { collection: resp.data as ResolvedGraphCollection, etag: etagOf(resp) })
  } catch (err: any) {
    return failure(err, `Failed to load graph collection ${id}.`)
  }
}

/**
 * A collection built on the fly, one graph per SNMP interface of a node (by id, by
 * `foreignSource:foreignId`, or by domain), resolved and ready to render. Nothing is stored.
 */
export const getResolvedSyntheticGraphCollection = async (
  kind: SyntheticGraphCollectionKind,
  key: string,
  options?: ResolveOptions
): Promise<ValidationResultWithPayload<ResolvedGraphCollection>> => {
  try {
    const resp = await v2.get(`${endpoint}/resolved/${kind}/${seg(key)}`, { params: resolveParams(options) })
    return createResultWithPayload(true, '', resp.data as ResolvedGraphCollection)
  } catch (err: any) {
    return failure(err, `Failed to build the graph collection for ${kind} ${key}.`)
  }
}

/** Create a collection; the server assigns the id and returns the saved collection. */
export const createGraphCollection = async (
  collection: GraphCollection
): Promise<GraphCollectionResult<GraphCollectionWithEtag>> => {
  try {
    const resp = await v2.post(endpoint, collection)
    return createResultWithPayload(true, '', { collection: resp.data as GraphCollection, etag: etagOf(resp) })
  } catch (err: any) {
    return failure(err, 'Failed to create the graph collection.')
  }
}

/**
 * Replace a collection's title, options and graph list. Pass the ETag it was read with so a
 * concurrent edit is refused (412) instead of overwritten; see isStaleWrite.
 */
export const updateGraphCollection = async (
  id: number,
  collection: GraphCollection,
  etag?: string | null
): Promise<GraphCollectionResult<GraphCollectionWithEtag>> => {
  try {
    const resp = await v2.put(`${endpoint}/${seg(id)}`, collection, ifMatch(etag))
    return createResultWithPayload(true, '', { collection: resp.data as GraphCollection, etag: etagOf(resp) })
  } catch (err: any) {
    return failure(err, `Failed to save graph collection ${id}.`)
  }
}

export const deleteGraphCollection = async (id: number, etag?: string | null): Promise<GraphCollectionResult<void>> => {
  try {
    await v2.delete(`${endpoint}/${seg(id)}`, ifMatch(etag))
    return createResultWithPayload(true, '')
  } catch (err: any) {
    return failure(err, `Failed to delete graph collection ${id}.`)
  }
}

/** Append a graph to a collection, or insert it at a zero-based `index`. Returns the saved collection. */
export const addGraphToCollection = async (
  id: number,
  graph: GraphCollectionGraph,
  options?: { index?: number, etag?: string | null }
): Promise<GraphCollectionResult<GraphCollectionWithEtag>> => {
  try {
    const config = {
      ...ifMatch(options?.etag),
      params: options?.index === undefined ? {} : { index: options.index }
    }
    const resp = await v2.post(`${endpoint}/${seg(id)}/graphs`, graph, config)
    return createResultWithPayload(true, '', { collection: resp.data as GraphCollection, etag: etagOf(resp) })
  } catch (err: any) {
    return failure(err, `Failed to add the graph to graph collection ${id}.`)
  }
}

export interface NodeSearchResult {
  nodes: { id: number, label: string }[]
  totalCount: number
}

/**
 * The nodes whose label contains the term, as one page of at most `limit` ordered by label,
 * with the count of every match. Backs the node picker of the list page, which must not list
 * every node: the v1 endpoint is paged where the resource tree walk is not, and its ilike
 * comparator is case-insensitive where v2 FIQL wildcards are not. null when the search failed.
 */
export const searchNodes = async (term: string, limit: number): Promise<NodeSearchResult | null> => {
  const filter = term ? `&comparator=ilike&label=${encodeURIComponent(`%${term}%`)}` : ''
  try {
    const resp = await rest.get(`/nodes?limit=${limit}&orderBy=label&order=asc${filter}`)
    const found: any[] = resp.status === 204 ? [] : (resp.data?.node ?? [])
    return {
      nodes: found
        .map(n => ({ id: Number(n.id), label: String(n.label ?? n.id) }))
        .filter(n => Number.isInteger(n.id)),
      totalCount: Number(resp.data?.totalCount ?? 0)
    }
  } catch (_err) {
    return null
  }
}

/** Re-read ksc-performance-reports.xml from disk, picking up edits made outside the API. */
export const reloadGraphCollections = async (): Promise<ValidationResult> => {
  try {
    await v2.post(`${endpoint}/reload`)
    return createResultWithPayload(true, '')
  } catch (err: any) {
    return failure(err, 'Failed to reload the graph collections from disk.')
  }
}
