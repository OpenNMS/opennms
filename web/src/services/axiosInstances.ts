import axios from 'axios'

console.log(import.meta.env.VITE_BASE_V2_URL)

const v2 = axios.create({
  baseURL: import.meta.env.VITE_BASE_V2_URL?.toString() || '/opennms/api/v2',
  withCredentials: true
})

const rest = axios.create({
  baseURL: import.meta.env.VITE_BASE_REST_URL?.toString() || '/opennms/rest',
  withCredentials: true
})

export { v2, rest }
