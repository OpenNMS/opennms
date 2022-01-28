import { useStore } from 'vuex'

export const useConfigurationToast = () => {
  const store = useStore()
  const updateToast = (toastVal: {}) => {
    store.dispatch('configuration/updateToastValue', toastVal)
  }
  return { updateToast }
}
