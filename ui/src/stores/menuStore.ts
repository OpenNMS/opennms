import { defineStore } from 'pinia'
import API from '@/services'
import { MainMenu, NotificationSummary } from '@/types/mainMenu'
import { defaultMainMenu, defaultNotificationSummary } from './fakeMenuData'

// Set this to true to use local/fake data instead of making API call
const useFakeMenuData = false
const useFakeUserNotificationData = false

export const useMenuStore = defineStore('menuStore', () => {
  const mainMenu = ref({} as MainMenu)
  const notificationSummary = ref({} as NotificationSummary)

  const getMainMenu = async () => {
    // for using local data for dev/debugging purposes
    if (useFakeMenuData) {
      mainMenu.value = defaultMainMenu
      return
    }

    const resp = await API.getMainMenu()

    if (resp) {
      mainMenu.value = resp as MainMenu
    }
  }

  const getNotificationSummary = async () => {
    // for using local data for dev/debugging purposes
    if (useFakeUserNotificationData) {
      notificationSummary.value = defaultNotificationSummary
      return
    }

    const resp = await API.getNotificationSummary()

    if (resp) {
      notificationSummary.value = resp as NotificationSummary
    }
  }

  return {
    mainMenu,
    notificationSummary,
    getMainMenu,
    getNotificationSummary
  }
})

