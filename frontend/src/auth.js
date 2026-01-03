export function getAuth() {
  const token = localStorage.getItem('patrakosh_token')
  const userRaw = localStorage.getItem('patrakosh_user')
  const user = userRaw ? JSON.parse(userRaw) : null
  return { token, user }
}

export function setAuth(token, user) {
  localStorage.setItem('patrakosh_token', token)
  localStorage.setItem('patrakosh_user', JSON.stringify(user))
}

export function clearAuth() {
  localStorage.removeItem('patrakosh_token')
  localStorage.removeItem('patrakosh_user')
}

export function isAuthed() {
  return !!localStorage.getItem('patrakosh_token')
}
