import axios, { type AxiosInstance } from 'axios'

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api/v1',
  timeout: 10000
})

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('cq_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  (resp) => resp,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('cq_token')
      localStorage.removeItem('cq_user')
      localStorage.removeItem('cq_role')
      if (location.pathname !== '/login') location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default instance