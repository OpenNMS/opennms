import API from '@/services'
import { VuexContext } from '@/types'
import { uniq } from 'lodash'

const getGraphDefinitionsByResourceIds = async (context: VuexContext, ids: string[]) => {
  const definitions = []
  const promises = []

  for (const id of ids) {
    promises.push(API.getGraphDefinitionsByResourceId(id))
  }

  const results = await Promise.all(promises)

  for (const result of results) {
    definitions.push([...result.name])
  }

  const uniqueSortedDefinitions = uniq(definitions.sort())

  context.commit('SAVE_DEFINITIONS', uniqueSortedDefinitions)
}

const getDefinitionData = async (context: VuexContext, definition: string) => {
  const resp = await API.getDefinitionData(definition)

  if (resp) {
    context.commit('SAVE_DEFINITION_DATA')
  }
}

export default {
  getGraphDefinitionsByResourceIds,
  getDefinitionData
}
