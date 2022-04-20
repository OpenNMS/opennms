import axios from 'axios'

const v2 = axios.create({
  baseURL: import.meta.env.VITE_BASE_V2_URL?.toString() || '/opennms/api/v2',
  withCredentials: true
})

const rest = axios.create({
  baseURL: import.meta.env.VITE_BASE_REST_URL?.toString() || '/opennms/rest',
  withCredentials: true
})

const restFile = axios.create({
  baseURL: import.meta.env.VITE_BASE_REST_URL?.toString() || '/opennms/rest',
  withCredentials: true,
  headers: {
    'Content-Type': 'multipart/form-data'
  }
})

export { v2, rest, restFile }
