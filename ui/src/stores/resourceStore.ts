import { defineStore } from 'pinia'
import API from '@/services'
import { Resource } from '@/types'

export const useResourceStore = defineStore('resourceStore', () => {
  const resources = ref([] as Resource[])
  const nodeResource = ref({} as Resource)
  const searchValue = ref('')

  const getResources = async () => {
    const resp = await API.getResources()

    if (resp) {
      resources.value = resp.resource
    }
  }

  const getResourcesForNode = async (name: string) => {
    const resp = await API.getResourceForNode(name)

    if (resp) {
      nodeResource.value = resp
    }
  }

  const setSearchValue = (value: string) => {
    searchValue.value = value
  }

  return {
    resources,
    nodeResource,
    searchValue,
    getResources,
    getResourcesForNode,
    setSearchValue
  }
})
