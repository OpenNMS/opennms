import { AxiosResponse } from 'axios'
import { v2 } from './axiosInstances'
import useSpinner from '@/composables/useSpinner'

const endpoint = 'ui-menu'

const { startSpinner, stopSpinner } = useSpinner()

const getMainMenu = async (): Promise<AxiosResponse | false> => {
  startSpinner()

  try {
    return await v2.get(endpoint)
  } catch (err) {
    return false
  } finally {
    stopSpinner()
  }
}

export {
  getMainMenu
}
