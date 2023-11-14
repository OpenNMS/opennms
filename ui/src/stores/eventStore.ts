import { defineStore } from 'pinia'
import API from '@/services'
import { Event, QueryParameters } from '@/types'

export const useEventStore = defineStore('eventStore', () => {
  const events = ref([] as Event[])
  const totalCount = ref(0)

  const getEvents = async (queryParameters?: QueryParameters) => {
    const resp = await API.getEvents(queryParameters)

    if (resp) {
      events.value = resp.event
      totalCount.value = resp.totalCount
    }
  }

  return {
    events,
    totalCount,
    getEvents
  }
})
