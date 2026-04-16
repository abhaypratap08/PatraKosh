export const APP_BASE_URL = import.meta.env.BASE_URL || '/'
export const IS_STATIC_PREVIEW_HOST =
  typeof window !== 'undefined' && /github\.io$/i.test(window.location.hostname)

export function appPath(path = '/') {
  const normalized = path.startsWith('/') ? path : `/${path}`
  return IS_STATIC_PREVIEW_HOST ? `${APP_BASE_URL}#${normalized}` : normalized
}

export function currentAppPath() {
  if (typeof window === 'undefined') {
    return '/'
  }

  if (IS_STATIC_PREVIEW_HOST) {
    return window.location.hash.replace(/^#/, '') || '/'
  }

  return window.location.pathname
}
