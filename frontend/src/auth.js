export const AUTH_UPDATED_EVENT = 'patrakosh:auth-updated'
export const AUTH_CLEARED_EVENT = 'patrakosh:auth-cleared'

function authStorage() {
  return window.sessionStorage
}

export function getAuth() {
  const userRaw = authStorage().getItem('patrakosh_user')
  let user = null

  if (userRaw) {
    try {
      user = JSON.parse(userRaw)
    } catch {
      authStorage().removeItem('patrakosh_user')
    }
  }

  return { user }
}

export function setAuth(user, { notify = true } = {}) {
  authStorage().setItem('patrakosh_user', JSON.stringify(user))

  if (notify) {
    window.dispatchEvent(new CustomEvent(AUTH_UPDATED_EVENT, { detail: { user } }))
  }
}

export function clearAuth({ notify = true } = {}) {
  authStorage().removeItem('patrakosh_user')

  if (notify) {
    window.dispatchEvent(new CustomEvent(AUTH_CLEARED_EVENT))
  }
}

export function isAuthed() {
  return !!getAuth().user
}
