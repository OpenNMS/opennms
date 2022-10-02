import { AxiosResponse } from 'axios'
import { v2 } from './axiosInstances'
import { MainMenuDefinition } from '@/types/mainMenu'

const endpoint = 'menubar'

const getMainMenu = async (): Promise<MainMenuDefinition | false> => {
  try {
    const resp = await v2.get(endpoint)
    return resp.data
  } catch (err) {
    return false
  }
}

export {
  getMainMenu
}
