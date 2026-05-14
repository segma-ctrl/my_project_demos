/**
 * 后端基地址：微服务走网关（默认 9000）；可通过环境变量 VITE_API_BASE 覆盖。
 */
export const API_BASE = (import.meta.env.VITE_API_BASE ?? 'http://localhost:9000').replace(/\/$/, '')

export function apiUrl(path) {
  const p = path.startsWith('/') ? path : `/${path}`
  return `${API_BASE}${p}`
}
