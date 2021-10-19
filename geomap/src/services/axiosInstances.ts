//this file is copied from vue-ui.
import axios from 'axios'

const v2 = axios.create({
  baseURL: import.meta.env.VITE_BASE_V2_URL?.toString() || '/opennms/api/v2',
  withCredentials: true
})

const rest = axios.create({
  baseURL: import.meta.env.VITE_BASE_REST_URL?.toString() || '/opennms/rest',
  withCredentials: true,
  headers: {
    put: {
      Accept: 'application/x-www-form-urlencoded'
    },
}
})

export { v2, rest }