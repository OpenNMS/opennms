//this file is copied from vue-ui.
import axios from 'axios'

const v2 = axios.create({
  baseURL: import.meta.env.VITE_BASE_V2_URL?.toString() || '/opennms/api/v2',
  withCredentials: true
})

export { v2 }