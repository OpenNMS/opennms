import { SCVCredentials } from '@/types/scv'
import { State } from './state'

const SAVE_ALIASES = (state: State, aliases: string[]) => {
  state.aliases = aliases
}

const SAVE_CREDENTIALS = (state: State, credentials: SCVCredentials) => {
  state.credentials = credentials
  state.dbCredentials = credentials
}

const MERGE_CREDENTIALS = (state: State, credentials: Record<string, string>) => {
  state.credentials = { ...state.credentials, ...credentials }
}

const SET_IS_EDITING = (state: State, bool: boolean) => {
  state.isEditing = bool
}

const ADD_ATTRIBUTE = (state: State) => {
  state.credentials.attributes = { ...state.credentials.attributes, ...{ '': '' } } // adds empty key/val inputs in form
}

const REMOVE_ATTRIBUTE = (state: State, key: string) => {
  const attributes = state.credentials.attributes
  delete attributes[key]
  state.credentials.attributes = attributes
}

const UPDATE_ATTRIBUTE = (state: State, attribute: { key: string, keyVal: { key: string, value: string } }) => {
  const attributes = state.credentials.attributes

  // updating the value
  if (attribute.key === attribute.keyVal.key) {
    attributes[attribute.key] = attribute.keyVal.value
    return
  }

  // else remove and replace the key
  delete attributes[attribute.key]
  attributes[attribute.keyVal.key] = attribute.keyVal.value
  state.credentials.attributes = attributes
}

export default {
  SAVE_ALIASES,
  SAVE_CREDENTIALS,
  MERGE_CREDENTIALS,
  SET_IS_EDITING,
  ADD_ATTRIBUTE,
  REMOVE_ATTRIBUTE,
  UPDATE_ATTRIBUTE
}
