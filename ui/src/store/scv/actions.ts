import { VuexContext } from '@/types'
import API from '@/services'
import { State } from './state'
import { SCVCredentials } from '@/types/scv'

interface ContextWithState extends VuexContext {
  state: State
}

const getAliases = async (context: VuexContext) => {
  const aliases = await API.getAliases()
  context.commit('SAVE_ALIASES', aliases)
}

const getCredentialsByAlias = async (context: VuexContext, alias: string) => {
  const credentials = await API.getCredentialsByAlias(alias)
  if (credentials) {
    context.commit('SAVE_CREDENTIALS', credentials)
    context.commit('SET_IS_EDITING', true)
  }
}

const addCredentials = async (context: ContextWithState) => {
  const success = await API.addCredentials(context.state.credentials)
  if (success) {
    context.dispatch('clearCredentials')
    context.dispatch('getAliases')
  }
}

const updateCredentials = async (context: ContextWithState) => {
  const success = await API.updateCredentials(context.state.credentials)
  if (success) {
    context.dispatch('clearCredentials')
  }
}

const setValue = (context: VuexContext, keyVal: Record<string, string>) => {
  context.commit('MERGE_CREDENTIALS', keyVal)
}

const clearCredentials = async (context: VuexContext) => {
  context.commit('SAVE_CREDENTIALS', {
    id: undefined,
    alias: '',
    username: '',
    password: '',
    attributes: {}
  } as SCVCredentials)
  context.commit('SET_IS_EDITING', false)
}

export default {
  getAliases,
  getCredentialsByAlias,
  addCredentials,
  updateCredentials,
  setValue,
  clearCredentials
}
