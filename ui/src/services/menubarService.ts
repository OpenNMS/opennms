import { v2 } from './axiosInstances'
import { MainMenu } from '@/types/mainMenu'

const endpoint = 'menubar'

const getMainMenu = async (): Promise<MainMenu | false> => {
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
