import { Node } from 'v-network-graph'
import store from '@/store'

export const useTopologyFocus = () => {
  // add single object to focus
  const addFocusObject = async (obj: Node) => {
    store.dispatch('topologyModule/addFocusObject', obj)
  }

  // add multiple objects to focus
  const addFocusObjects = async (objects: Node[]) => {
    for (const obj of objects) {
      store.dispatch('topologyModule/addFocusObject', obj)
    }
  }

  // replace focus with these objects
  const replaceFocusObjects = async (objects: Node[]) => {
    store.dispatch('topologyModule/replaceFocusObjects', objects)
  }

  // remove focused objects by id
  const removeFocusObjectsByIds = async (ids: string[]) => {
    for (const id of ids) {
      store.dispatch('topologyModule/removeFocusObject', id)
    }
  }

  // sets the default focused node
  const useDefaultFocus = () => {
    store.dispatch('topologyModule/useDefaultFocus')
  }

  return { addFocusObject, addFocusObjects, replaceFocusObjects, removeFocusObjectsByIds, useDefaultFocus }
}
