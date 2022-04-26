import { rest } from './axiosInstances'
import { SCVCredentials } from '@/types/scv'
import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/scv'

const getAliases = async (): Promise<string[]> => {
  try {
    startSpinner()
    const resp = await rest.get(endpoint)
    return resp.data
  } catch (err) {
    showSnackBar({ msg: 'Failed to return alises.' })
    return []
  } finally {
    stopSpinner()
  }
}

const getCredentialsByAlias = async (alias: string): Promise<SCVCredentials | null> => {
  try {
    startSpinner()
    const resp = await rest.get(`${endpoint}/${alias}`)
    return resp.data
  } catch (err) {
    showSnackBar({ msg: 'Failed to retrieve credentials.' })
    return null
  } finally {
    stopSpinner()
  }
}

const addCredentials = async (credentials: SCVCredentials): Promise<number | null> => {
  try {
    startSpinner()
    const resp = await rest.post(endpoint, credentials)
    showSnackBar({ msg: 'Alias added.' })
    return resp.status
  } catch (err) {
    showSnackBar({ msg: 'Failed to add credentials.' })
    return null
  } finally {
    stopSpinner()
  }
}

const updateCredentials = async (credentials: SCVCredentials): Promise<number | null> => {
  try {
    startSpinner()
    const resp = await rest.put(`${endpoint}/${credentials.alias}`, credentials)
    showSnackBar({ msg: 'Alias updated.' })
    return resp.status
  } catch (err) {
    showSnackBar({ msg: 'Failed to update alias credentials.' })
    return null
  } finally {
    stopSpinner()
  }
}

export {
  getAliases,
  getCredentialsByAlias,
  addCredentials,
  updateCredentials
}
