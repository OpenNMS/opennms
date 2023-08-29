import { defineStore } from 'pinia'
import API from '@/services'

export const useLogStore = defineStore('logStore', () => {
  const logs = ref([] as string[])
  const log = ref('')
  const searchValue = ref('')
  const selectedLog = ref('')
  const reverseLog = ref(false)

  const getFilteredLogs = () => {
    return logs.value
      .filter(logName => !searchValue.value|| (searchValue.value && logName.includes(searchValue.value)))
  }

  const getLog = async (name: string) => {
    const resp = await API.getLog(name, reverseLog.value)

    log.value = resp
    selectedLog.value = name
  }
 
  const getLogs = async () => {
    const resp = await API.getLogs()
    logs.value = resp
  }

  const setReverseLog = async (reverse: boolean) => {
    reverseLog.value = reverse
  }

  const setSearchValue = async (value: string) => {
    searchValue.value = value
  }

  return {
    logs,
    log,
    searchValue,
    selectedLog,
    reverseLog,
    getFilteredLogs,
    getLog,
    getLogs,
    setReverseLog,
    setSearchValue
  }
})
