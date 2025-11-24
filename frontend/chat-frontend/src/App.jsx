import { useEffect, useRef, useState } from 'react'
import './index.css'
import SockJS from 'sockjs-client'
import { Client as StompClient } from '@stomp/stompjs'

function App() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [token, setToken] = useState('')
  const [rooms, setRooms] = useState([])
  const [myRooms, setMyRooms] = useState([])
  const [newRoomName, setNewRoomName] = useState('')
  const [newRoomPrivate, setNewRoomPrivate] = useState(false)
  const [newRoomPassword, setNewRoomPassword] = useState('')
  const [selectedRoomId, setSelectedRoomId] = useState(null)
  const [messages, setMessages] = useState([])
  const [text, setText] = useState('')
  const [connecting, setConnecting] = useState(false)
  const [joinPassword, setJoinPassword] = useState('')
  const [users, setUsers] = useState([])
  const [me, setMe] = useState(null)
  const [dmTargetId, setDmTargetId] = useState(null)
  const [dmText, setDmText] = useState('')
  const [dmMessages, setDmMessages] = useState([])
  const [online, setOnline] = useState([])
  const [typingUsers, setTypingUsers] = useState([])
  const [admServerId, setAdmServerId] = useState('')
  const [admChannelId, setAdmChannelId] = useState('')
  const [aclRole, setAclRole] = useState('MEMBER')
  const [aclCanSend, setAclCanSend] = useState(true)
  const [aclCanSubscribe, setAclCanSubscribe] = useState(true)
  const [aclCanManage, setAclCanManage] = useState(false)
  const [servers, setServers] = useState([])
  const [admChannels, setAdmChannels] = useState([])
  const [fileToSend, setFileToSend] = useState(null)
  const [dmFile, setDmFile] = useState(null)
  const [chatServerId, setChatServerId] = useState('')
  const [chatChannelId, setChatChannelId] = useState('')
  const [channelMessages, setChannelMessages] = useState([])
  const [channelText, setChannelText] = useState('')
  const [channelFile, setChannelFile] = useState(null)
  const [admMembers, setAdmMembers] = useState([])
  const [aclList, setAclList] = useState([])
  const [toasts, setToasts] = useState([])
  const [userStatus, setUserStatus] = useState({})
  const [channelPerms, setChannelPerms] = useState({ role: null, canSend: null, canSubscribe: null, banned: null, muted: null })
  const [audit, setAudit] = useState([])
  const [health, setHealth] = useState(null)
  const [httpMetrics, setHttpMetrics] = useState(null)
  const [httpCounts, setHttpCounts] = useState(null)

  const stompRef = useRef(null)
  const toastSeq = useRef(0)

  async function login() {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    })
    if (!res.ok) { showNotif('Credenciales incorrectas', 'error'); return }
    const data = await res.json()
    setToken(data.token)
    await loadRooms()
    await loadMyRooms()
    await loadUsers()
    await loadMe()
    await loadServers()
  }

  async function loadRooms() {
    const res = await fetch('/api/rooms', {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.ok) {
      const data = await res.json()
      setRooms(data)
    }
  }

  async function loadMyRooms() {
    const res = await fetch('/api/rooms/my', {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.ok) {
      const data = await res.json()
      setMyRooms(data)
    }
  }

  async function loadUsers() {
    const res = await fetch('/api/users', {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.ok) {
      const data = await res.json()
      setUsers(data)
    }
  }

  async function loadMe() {
    const res = await fetch('/api/users/me', {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.ok) {
      const data = await res.json()
      setMe(data)
    }
  }

  async function loadServers() {
    const res = await fetch('/api/servers', {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.ok) setServers(await res.json())
  }

  async function loadAdmChannels(serverId) {
    if (!serverId) return
    const res = await fetch(`/api/servers/${serverId}/channels`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.ok) setAdmChannels(await res.json())
  }

  async function loadAdmMembers(serverId) {
    if (!serverId) return
    const res = await fetch(`/api/servers/${serverId}/members`, { headers: { Authorization: `Bearer ${token}` } })
    if (res.ok) setAdmMembers(await res.json())
  }

  async function loadAcl() {
    if (!admChannelId) return
    const res = await fetch(`/api/channels/${Number(admChannelId)}/acl`, { headers: { Authorization: `Bearer ${token}` } })
    if (res.ok) setAclList(await res.json())
  }

  function showNotif(msg, type = 'info') {
    const id = 't' + (++toastSeq.current)
    setToasts(prev => [...prev, { id, msg, type }])
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 3000)
  }

  async function loadChannelHistory() {
    if (!chatChannelId || !token) return
    const res = await fetch(`/api/channels/${chatChannelId}/messages`, { headers: { Authorization: `Bearer ${token}` } })
    if (res.ok) setChannelMessages(await res.json())
  }

  function subscribeChannel() {
    if (!stompRef.current?.active || !chatChannelId) return
    stompRef.current.subscribe(`/topic/channels/${chatChannelId}`, (frame) => {
      const msg = JSON.parse(frame.body)
      setChannelMessages((prev) => [...prev, msg])
    })
  }

  function sendChannel() {
    if (!chatChannelId || !stompRef.current?.active) return
    const doSend = (content) => {
      if (!content) return
      stompRef.current.publish({ destination: `/app/channel.send/${chatChannelId}`, body: JSON.stringify({ content }) })
      setChannelText('')
    }
    if (channelFile) {
      const form = new FormData()
      form.append('file', channelFile)
      fetch('/api/files', { method: 'POST', headers: { Authorization: `Bearer ${token}` }, body: form })
        .then(async (res) => {
          if (!res.ok) { const msg = await res.text(); throw new Error(msg || 'Error de subida') }
          const data = await res.json()
          doSend(data.url)
          showNotif('Adjunto de canal enviado')
        })
        .catch((err) => showNotif(err.message, 'error'))
        .finally(() => setChannelFile(null))
    } else {
      doSend(channelText)
      showNotif('Mensaje de canal enviado')
    }
  }

  async function joinRoom() {
    if (!selectedRoomId || !token) return
    const room = rooms.find(r => r.id === selectedRoomId)
    if (room && (room.private || room.isPrivate)) {
      if (!joinPassword || joinPassword.length === 0) { showNotif('Ingresa la contraseña de la sala', 'error'); return }
      const access = await fetch(`/api/rooms/${selectedRoomId}/access`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ password: joinPassword })
      })
      if (!access.ok) { showNotif('Contraseña incorrecta', 'error'); return }
    }
    const res = await fetch(`/api/rooms/${selectedRoomId}/join`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify(room && (room.private || room.isPrivate) ? { password: joinPassword } : {})
    })
    if (!res.ok) { showNotif('No se pudo unir a la sala', 'error'); return }
    setJoinPassword('')
    await loadMyRooms()
    connectRoom()
  }

  async function createRoom() {
    const res = await fetch('/api/rooms', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify({
        name: newRoomName,
        isPrivate: newRoomPrivate,
        password: newRoomPrivate ? newRoomPassword : null
      })
    })
    if (!res.ok) { showNotif('No se pudo crear la sala', 'error'); return }
    setNewRoomName('')
    setNewRoomPassword('')
    setNewRoomPrivate(false)
    await loadRooms()
  }

  async function loadHistory() {
    if (!selectedRoomId || !token) return
    const res = await fetch(`/api/rooms/${selectedRoomId}/messages`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.ok) {
      const data = await res.json()
      setMessages(data)
    }
  }

  function connectRoom() {
    if (!token || !selectedRoomId) {
      showNotif('Selecciona una sala e inicia sesión', 'error')
      return
    }
    setConnecting(true)
    const sock = new SockJS('/api/ws')
    const client = new StompClient({ reconnectDelay: 3000, webSocketFactory: () => sock })
    client.onConnect = async () => {
      setConnecting(false)
      stompRef.current.subscribe(`/topic/rooms/${selectedRoomId}`, (frame) => {
        const msg = JSON.parse(frame.body)
        setMessages((prev) => [...prev, msg])
      })
      stompRef.current.subscribe('/topic/presence', (frame) => {
        const list = JSON.parse(frame.body)
        setOnline(list)
      })
      stompRef.current.subscribe(`/topic/rooms/${selectedRoomId}/typing`, (frame) => {
        const data = JSON.parse(frame.body)
        setTypingUsers((prev) => {
          const set = new Set(prev)
          if (data.typing) set.add(data.user)
          else set.delete(data.user)
          return Array.from(set)
        })
      })
      if (!me) await loadMe()
      if (me?.id) {
        stompRef.current.subscribe(`/topic/dm/${me.id}`, (frame) => {
          const msg = JSON.parse(frame.body)
          setDmMessages((prev) => [...prev, msg])
        })
      }
      loadHistory()
    }
    client.onStompError = () => { setConnecting(false); showNotif('Error de conexión en tiempo real', 'error') }
    client.connectHeaders = { Authorization: `Bearer ${token}` }
    stompRef.current = client
    client.activate()
  }

  function send() {
    if (!selectedRoomId || !stompRef.current?.active) return
    const doSend = (content) => {
      if (!content) return
      stompRef.current.publish({ destination: `/app/chat.send/${selectedRoomId}`, body: JSON.stringify({ content }) })
      setText('')
    }
    if (fileToSend) {
      const form = new FormData()
      form.append('file', fileToSend)
      fetch('/api/files', { method: 'POST', headers: { Authorization: `Bearer ${token}` }, body: form })
        .then(async (res) => {
          if (!res.ok) { const msg = await res.text(); throw new Error(msg || 'Error de subida') }
          const data = await res.json()
          doSend(data.url)
          showNotif('Adjunto enviado')
        })
        .catch((err) => showNotif(err.message, 'error'))
        .finally(() => setFileToSend(null))
    } else {
      doSend(text)
      showNotif('Mensaje enviado')
    }
  }

  function typingEvent(isTyping) {
    if (!selectedRoomId || !stompRef.current?.active) return
    stompRef.current.publish({ destination: `/app/typing/${selectedRoomId}`, body: JSON.stringify({ typing: isTyping }) })
  }

  async function sendDm() {
    if (!dmTargetId || !stompRef.current?.active) return
    const doSend = (content) => {
      if (!content) return
      stompRef.current.publish({ destination: `/app/dm.send/${dmTargetId}`, body: JSON.stringify({ content }) })
      setDmText('')
    }
    if (dmFile) {
      const form = new FormData()
      form.append('file', dmFile)
      fetch('/api/files', { method: 'POST', headers: { Authorization: `Bearer ${token}` }, body: form })
        .then(async (res) => {
          if (!res.ok) { const msg = await res.text(); throw new Error(msg || 'Error de subida') }
          const data = await res.json()
          doSend(data.url)
          showNotif('Adjunto DM enviado')
        })
        .catch((err) => showNotif(err.message, 'error'))
        .finally(() => setDmFile(null))
    } else {
      doSend(dmText)
      showNotif('DM enviado')
    }
  }

  async function applyAcl() {
    if (!token || !admChannelId) return
    const url = `/api/channels/${Number(admChannelId)}/acl?role=${aclRole}&canSend=${aclCanSend}&canSubscribe=${aclCanSubscribe}&canManage=${aclCanManage}`
    const res = await fetch(url, { method: 'POST', headers: { Authorization: `Bearer ${token}` } })
    if (!res.ok) { const msg = await res.text(); showNotif(msg || 'No se pudo aplicar ACL', 'error') } else {
      showNotif('ACL aplicado')
      await loadAcl()
    }
  }

  async function loadUserStatus(userId) {
    const status = { banned: null, muted: null }
    if (admServerId) {
      const res = await fetch(`/api/mod/servers/${Number(admServerId)}/ban/${userId}`, { headers: { Authorization: `Bearer ${token}` } })
      if (res.ok) { const data = await res.json(); status.banned = !!data.banned }
    }
    if (admChannelId) {
      const res2 = await fetch(`/api/mod/channels/${Number(admChannelId)}/mute/${userId}`, { headers: { Authorization: `Bearer ${token}` } })
      if (res2.ok) { const data2 = await res2.json(); status.muted = !!data2.muted }
    }
    setUserStatus(prev => ({ ...prev, [userId]: status }))
  }

  async function computeChannelPerms() {
    if (!token || !chatServerId || !chatChannelId || !me?.id) return
    const [membersRes, aclRes, banRes, muteRes] = await Promise.all([
      fetch(`/api/servers/${Number(chatServerId)}/members`, { headers: { Authorization: `Bearer ${token}` } }),
      fetch(`/api/channels/${Number(chatChannelId)}/acl`, { headers: { Authorization: `Bearer ${token}` } }),
      fetch(`/api/mod/servers/${Number(chatServerId)}/ban/${me.id}`, { headers: { Authorization: `Bearer ${token}` } }),
      fetch(`/api/mod/channels/${Number(chatChannelId)}/mute/${me.id}`, { headers: { Authorization: `Bearer ${token}` } })
    ])
    if (!(membersRes.ok && aclRes.ok && banRes.ok && muteRes.ok)) return
    const members = await membersRes.json()
    const acl = await aclRes.json()
    const banned = !!(await banRes.json()).banned
    const muted = !!(await muteRes.json()).muted
    const mine = members.find(m => String(m.userId) === String(me.id))
    const role = mine?.role || 'MEMBER'
    const aclEntry = acl.find(a => String(a.channel?.id) === String(chatChannelId) && a.role === role)
    const canSend = aclEntry ? !!aclEntry.canSend : true
    const canSubscribe = aclEntry ? !!aclEntry.canSubscribe : true
    setChannelPerms({ role, canSend, canSubscribe, banned, muted })
  }

  async function loadAudit(type) {
    const res = await fetch(`/api/audit${type ? `?type=${type}` : ''}`, { headers: { Authorization: `Bearer ${token}` } })
    if (res.ok) setAudit(await res.json())
  }

  async function loadHealth() {
    const res = await fetch('/api/actuator/health', { headers: { Authorization: `Bearer ${token}` } })
    if (res.ok) setHealth(await res.json()); else showNotif('No se pudo obtener estado del sistema', 'error')
  }

  async function loadHttpRequests() {
    const res = await fetch('/api/actuator/metrics/http.server.requests', { headers: { Authorization: `Bearer ${token}` } })
    if (!res.ok) { showNotif('No se pudieron cargar las métricas HTTP', 'error'); return }
    const data = await res.json()
    setHttpMetrics(data)
    const methods = data.availableTags?.find(t => t.tag === 'method')?.values || []
    const statuses = data.availableTags?.find(t => t.tag === 'status')?.values || []
    if (methods.length && statuses.length) {
      const promises = []
      for (const m of methods) {
        for (const s of statuses) {
          promises.push(
            fetch(`/api/actuator/metrics/http.server.requests?tag=method:${m}&tag=status:${s}`, { headers: { Authorization: `Bearer ${token}` } })
              .then(r => r.ok ? r.json() : null)
              .then(j => {
                const count = j?.measurements?.find(mm => mm.statistic === 'COUNT')?.value ?? 0
                return { m, s, count }
              })
              .catch(() => ({ m, s, count: 0 }))
          )
        }
      }
      const results = await Promise.all(promises)
      const grid = {}
      methods.forEach(m => { grid[m] = {}; statuses.forEach(s => { grid[m][s] = 0 }) })
      results.forEach(({ m, s, count }) => { if (grid[m]) grid[m][s] = count })
      setHttpCounts({ methods, statuses, grid })
    } else {
      setHttpCounts(null)
    }
  }

  async function banUser(userId) {
    if (!token || !admServerId || !userId) return
    const res = await fetch(`/api/mod/servers/${Number(admServerId)}/ban/${userId}`, { method: 'POST', headers: { Authorization: `Bearer ${token}` } })
    if (!res.ok) showNotif('No se pudo banear al usuario', 'error'); else showNotif('Usuario baneado')
  }
  async function unbanUser(userId) {
    if (!token || !admServerId || !userId) return
    const res = await fetch(`/api/mod/servers/${Number(admServerId)}/unban/${userId}`, { method: 'POST', headers: { Authorization: `Bearer ${token}` } })
    if (!res.ok) showNotif('No se pudo desbanear al usuario', 'error'); else showNotif('Usuario desbaneado')
  }
  async function muteUser(userId) {
    if (!token || !admChannelId || !userId) return
    const res = await fetch(`/api/mod/channels/${Number(admChannelId)}/mute/${userId}`, { method: 'POST', headers: { Authorization: `Bearer ${token}` } })
    if (!res.ok) showNotif('No se pudo silenciar al usuario', 'error'); else showNotif('Usuario silenciado')
  }
  async function unmuteUser(userId) {
    if (!token || !admChannelId || !userId) return
    const res = await fetch(`/api/mod/channels/${Number(admChannelId)}/unmute/${userId}`, { method: 'POST', headers: { Authorization: `Bearer ${token}` } })
    if (!res.ok) showNotif('No se pudo quitar el silencio al usuario', 'error'); else showNotif('Usuario des-silenciado')
  }

  useEffect(() => {
    return () => { if (stompRef.current?.active) stompRef.current.deactivate() }
  }, [])

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: 24 }}>
      <h2>ChatNexus</h2>

      {!token && (
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <input placeholder="Correo electrónico" value={email} onChange={(e) => setEmail(e.target.value)} />
              <input placeholder="Contraseña" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          <button onClick={login} title="Iniciar sesión">Login</button>
        </div>
      )}

      {token && (
        <div style={{ marginTop: 16 }}>
          <div style={{ position: 'fixed', top: 16, right: 16, display: 'flex', flexDirection: 'column', gap: 8, zIndex: 9999 }}>
            {toasts.map(t => (
              <div key={t.id} style={{ background: t.type === 'error' ? '#7a1f1f' : '#1e1f23', color: '#fff', padding: 10, borderRadius: 8, boxShadow: '0 2px 8px rgba(0,0,0,0.3)' }}>
                {t.msg}
              </div>
            ))}
          </div>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <input placeholder="Nueva sala" value={newRoomName} onChange={(e) => setNewRoomName(e.target.value)} />
            <label>
              <input type="checkbox" checked={newRoomPrivate} onChange={(e) => setNewRoomPrivate(e.target.checked)} /> privada
            </label>
            {newRoomPrivate && (
              <input placeholder="Contraseña" value={newRoomPassword} onChange={(e) => setNewRoomPassword(e.target.value)} />
            )}
            <button onClick={createRoom} title="Crear nueva sala">Crear</button>
            <button onClick={loadRooms} title="Actualizar lista de salas">Refrescar salas</button>
          </div>

          <div style={{ marginTop: 12 }}>
            <select value={selectedRoomId ?? ''} onChange={(e) => setSelectedRoomId(Number(e.target.value))}>
              <option value="">Selecciona sala</option>
              {rooms.map(r => (
                <option key={r.id} value={r.id}>{r.name}</option>
              ))}
            </select>
            {(selectedRoomId && (rooms.find(r => r.id === selectedRoomId)?.private || rooms.find(r => r.id === selectedRoomId)?.isPrivate)) && (
              <input placeholder="Contraseña de sala" style={{ marginLeft: 8 }} value={joinPassword} onChange={(e) => setJoinPassword(e.target.value)} />
            )}
            <button onClick={joinRoom} style={{ marginLeft: 8 }} title="Unirse a la sala seleccionada">Unirse</button>
            <button onClick={connectRoom} disabled={connecting} style={{ marginLeft: 8 }}>
              {connecting ? 'Conectando...' : 'Conectar'}
            </button>
            <button onClick={loadHistory} style={{ marginLeft: 8 }} title="Cargar mensajes anteriores de la sala">Cargar historial</button>
          </div>

          <div style={{ marginTop: 16 }}>
            <input style={{ width: '50%' }} placeholder="Escribe un mensaje" value={text} onChange={(e) => { setText(e.target.value); typingEvent(e.target.value.length > 0) }} onBlur={() => typingEvent(false)} />
            <input type="file" style={{ width: '20%' }} onChange={(e) => setFileToSend(e.target.files?.[0] ?? null)} />
            <button onClick={send} style={{ marginLeft: 8 }} title="Enviar mensaje">Enviar</button>
            <button onClick={loadMyRooms} style={{ marginLeft: 8 }} title="Ver salas a las que perteneces">Mis salas</button>
          </div>

          <div style={{ marginTop: 16, background: '#1e1f23', padding: 12, borderRadius: 8 }}>
            <h4>Mensajes</h4>
            {typingUsers.length > 0 && (
              <div style={{ fontStyle: 'italic', marginBottom: 8 }}>
                {typingUsers.join(', ')} está(n) escribiendo...
              </div>
            )}
            <div>
              {messages.map((m, idx) => (
                <div key={m.id ?? idx} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span><b>{m.sender}</b>: {(/\/api\/files\//.test(m.content)) ? (
                    m.content.match(/\.png$|\.jpg$|\.jpeg$|\.gif$/) ? <img src={m.content} alt="adjunto" style={{ maxHeight: 120, borderRadius: 6 }} /> : <a href={m.content} target="_blank">Archivo</a>
                  ) : m.content}</span>
                  {(() => {
                    const role = myRooms.find(r => r.roomId === selectedRoomId)?.role
                    if (role === 'OWNER' || role === 'MODERATOR') {
                      return <button onClick={async () => {
                        await fetch(`/api/rooms/${selectedRoomId}/messages/${m.id}`, {
                          method: 'DELETE',
                          headers: { Authorization: `Bearer ${token}` }
                        })
                        await loadHistory()
                      }}>Borrar</button>
                    }
                    return null
                  })()}
                </div>
              ))}
            </div>
          </div>

          <div style={{ marginTop: 12 }}>
            <h4>Online</h4>
            <div>{online.join(', ')}</div>
          </div>

          <div style={{ marginTop: 24, background: '#232428', padding: 12, borderRadius: 8 }}>
            <h4>Mensajes Directos</h4>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <select value={dmTargetId ?? ''} onChange={(e) => setDmTargetId(Number(e.target.value))}>
                <option value="">Selecciona usuario</option>
                {users.filter(u => u.id !== me?.id).map(u => (
                  <option key={u.id} value={u.id}>{u.username ?? u.email}</option>
                ))}
              </select>
              <button onClick={async () => {
                if (!dmTargetId) { showNotif('Selecciona un usuario primero', 'error'); return }
                const res = await fetch(`/api/dm/${dmTargetId}`, { headers: { Authorization: `Bearer ${token}` } })
                if (res.ok) setDmMessages(await res.json())
              }} title="Cargar mensajes directos anteriores">Cargar historial</button>
            </div>
            <div style={{ marginTop: 12 }}>
              <input style={{ width: '50%' }} placeholder="Escribe un DM" value={dmText} onChange={(e) => setDmText(e.target.value)} />
              <input type="file" style={{ width: '20%' }} onChange={(e) => setDmFile(e.target.files?.[0] ?? null)} />
              <button onClick={sendDm} style={{ marginLeft: 8 }} title="Enviar mensaje directo">Enviar DM</button>
            </div>
            <div style={{ marginTop: 12 }}>
              {dmMessages.map((m, idx) => (
                <div key={m.id ?? idx}>
                  <b>{m.sender}</b>: {(/\/api\/files\//.test(m.content)) ? (
                    m.content.match(/\.png$|\.jpg$|\.jpeg$|\.gif$/) ? <img src={m.content} alt="adjunto" style={{ maxHeight: 120, borderRadius: 6 }} /> : <a href={m.content} target="_blank">Archivo</a>
                  ) : m.content}
                </div>
              ))}
            </div>
          </div>

          <div style={{ marginTop: 24, background: '#232428', padding: 12, borderRadius: 8 }}>
            <h4>Chat de canales</h4>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <select value={chatServerId} onChange={async (e) => { const id = e.target.value; setChatServerId(id); setChatChannelId(''); await loadAdmChannels(id) }}>
                <option value="">Selecciona servidor</option>
                {servers.map(s => (<option key={s.id} value={s.id}>{s.name}</option>))}
              </select>
              <select value={chatChannelId} onChange={(e) => setChatChannelId(e.target.value)} disabled={!chatServerId}>
                <option value="">Selecciona canal</option>
                {admChannels.map(c => (<option key={c.id} value={c.id}>{c.name}</option>))}
              </select>
              <button onClick={async () => {
                if (!chatServerId || !chatChannelId) { showNotif('Selecciona servidor y canal primero', 'error'); return }
                // asegurar membresía del servidor para evitar 403 en envío
                const joinRes = await fetch(`/api/servers/${Number(chatServerId)}/join`, { method: 'POST', headers: { Authorization: `Bearer ${token}` } })
                if (!joinRes.ok) { showNotif('No se pudo unir al servidor', 'error'); return }
                subscribeChannel();
                loadChannelHistory();
                showNotif('Canal conectado')
                await computeChannelPerms()
              }} title="Conectar al canal seleccionado">Conectar canal</button>
            </div>
            <div style={{ marginTop: 12 }}>
              <input style={{ width: '50%' }} placeholder="Mensaje canal" value={channelText} onChange={(e) => setChannelText(e.target.value)} />
              <input type="file" style={{ width: '20%' }} onChange={(e) => setChannelFile(e.target.files?.[0] ?? null)} />
              <button onClick={sendChannel} style={{ marginLeft: 8 }} disabled={channelPerms.banned || channelPerms.muted || channelPerms.canSend === false} title={channelPerms.banned ? 'Estás baneado del servidor' : channelPerms.muted ? 'Estás silenciado en el canal' : channelPerms.canSend === false ? 'No tienes permiso para enviar' : ''}>Enviar</button>
            </div>
            <div style={{ marginTop: 8, fontSize: 12 }}>
              <div>Tu rol en servidor: {channelPerms.role ?? '-'}</div>
              <div>Permisos canal: enviar {channelPerms.canSend === null ? '-' : (channelPerms.canSend ? 'sí' : 'no')} | suscribirse {channelPerms.canSubscribe === null ? '-' : (channelPerms.canSubscribe ? 'sí' : 'no')}</div>
              <div>Estado: baneado {channelPerms.banned === null ? '-' : (channelPerms.banned ? 'sí' : 'no')} | silenciado {channelPerms.muted === null ? '-' : (channelPerms.muted ? 'sí' : 'no')}</div>
              <div>
                <button onClick={() => loadAudit('ACL')} style={{ marginRight: 6 }} title="Ver eventos de permisos">Ver auditoría ACL</button>
                <button onClick={() => loadAudit('MOD')} title="Ver eventos de moderación">Ver auditoría MOD</button>
              </div>
              {audit.length > 0 && (
                <div style={{ marginTop: 8 }}>
                  {audit.slice().reverse().slice(0, 10).map((e, idx) => (
                    <div key={idx} style={{ background: '#1e1f23', padding: 6, borderRadius: 6, marginBottom: 4 }}>
                      <div>{new Date(e.timestamp).toLocaleString()}</div>
                      <div>{e.type} {e.action} actor={e.actor} target={e.target} {e.details ? `detalles=${e.details}` : ''}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>
            <div style={{ marginTop: 12 }}>
              {channelMessages.map((m, idx) => (
                <div key={m.id ?? idx}>
                  <b>{m.sender}</b>: {(/\/api\/files\//.test(m.content)) ? (
                    m.content.match(/\.png$|\.jpg$|\.jpeg$|\.gif$/) ? <img src={m.content} alt="adjunto" style={{ maxHeight: 120, borderRadius: 6 }} /> : <a href={m.content} target="_blank">Archivo</a>
                  ) : m.content}
                </div>
              ))}
            </div>
          </div>

          <div style={{ marginTop: 24, background: '#232428', padding: 12, borderRadius: 8 }}>
            <h4>Administración de canal/servidor</h4>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <select value={admServerId} onChange={async (e) => { const id = e.target.value; setAdmServerId(id); setAdmChannelId(''); await loadAdmChannels(id) }}>
                <option value="">Selecciona servidor</option>
                {servers.map(s => (<option key={s.id} value={s.id}>{s.name}</option>))}
              </select>
              <select value={admChannelId} onChange={(e) => setAdmChannelId(e.target.value)} disabled={!admServerId}>
                <option value="">Selecciona canal</option>
                {admChannels.map(c => (<option key={c.id} value={c.id}>{c.name}</option>))}
              </select>
            </div>
            <div style={{ marginTop: 12, display: 'flex', gap: 8, alignItems: 'center' }}>
              <select value={aclRole} onChange={(e) => setAclRole(e.target.value)}>
                <option value="OWNER">OWNER</option>
                <option value="MODERATOR">MODERATOR</option>
                <option value="MEMBER">MEMBER</option>
              </select>
              <label><input type="checkbox" checked={aclCanSend} onChange={(e) => setAclCanSend(e.target.checked)} /> puede enviar</label>
              <label><input type="checkbox" checked={aclCanSubscribe} onChange={(e) => setAclCanSubscribe(e.target.checked)} /> puede suscribirse</label>
              <label><input type="checkbox" checked={aclCanManage} onChange={(e) => setAclCanManage(e.target.checked)} /> puede gestionar</label>
              <button onClick={applyAcl} title="Guardar permisos para el rol seleccionado">Aplicar ACL canal</button>
              <button onClick={loadAcl} title="Recargar lista de ACLs">Ver ACL actual</button>
            </div>
            {aclList.length > 0 && (
              <div style={{ marginTop: 12 }}>
                <h5>ACL actual</h5>
                {aclList.filter(a => String(a.channel?.id) === String(admChannelId)).map(a => (
                  <div key={a.id} style={{ background: '#1e1f23', padding: 8, borderRadius: 6, marginBottom: 6 }}>
                    <div>Rol: {a.role}</div>
                    <div>Enviar: {a.canSend ? 'Sí' : 'No'} | Suscribir: {a.canSubscribe ? 'Sí' : 'No'} | Gestionar: {a.canManage ? 'Sí' : 'No'}</div>
                  </div>
                ))}
              </div>
            )}
            <div style={{ marginTop: 12 }}>
              <h5>Acciones sobre usuarios</h5>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: 8 }}>
                {(admMembers.length === 0 && admServerId && (
                  <button onClick={() => loadAdmMembers(admServerId)} title="Cargar lista de miembros del servidor">Cargar miembros</button>
                ))})}
                {(admMembers.length ? admMembers : users.filter(u => u.id !== me?.id)).map(u => (
                  <div key={u.id} style={{ background: '#1e1f23', padding: 8, borderRadius: 8 }}>
                    <div>{u.username ?? u.email}{u.role ? ` (${u.role})` : ''}</div>
                    <div style={{ display: 'flex', gap: 6, marginTop: 6 }}>
                      <button onClick={() => banUser(u.userId ?? u.id)}>Ban servidor</button>
                      <button onClick={() => unbanUser(u.userId ?? u.id)}>Unban</button>
                      <button onClick={() => muteUser(u.userId ?? u.id)}>Silenciar canal</button>
                      <button onClick={() => unmuteUser(u.userId ?? u.id)}>Quitar silencio</button>
                      <button onClick={() => loadUserStatus(u.userId ?? u.id)} title="Ver estado de ban/silencio">Estado</button>
                    </div>
                    {userStatus[(u.userId ?? u.id)] && (
                      <div style={{ marginTop: 6, fontSize: 12 }}>
                        Baneado: {userStatus[(u.userId ?? u.id)].banned === null ? '-' : (userStatus[(u.userId ?? u.id)].banned ? 'Sí' : 'No')} | 
                        Silenciado: {userStatus[(u.userId ?? u.id)].muted === null ? '-' : (userStatus[(u.userId ?? u.id)].muted ? 'Sí' : 'No')}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div style={{ marginTop: 24, background: '#232428', padding: 12, borderRadius: 8 }}>
            <h4>Salud y Métricas</h4>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <button onClick={loadHealth} title="Consultar estado del sistema">Ver health</button>
              <button onClick={loadHttpRequests} title="Consultar métricas de peticiones HTTP">Ver métricas HTTP</button>
            </div>
            <div style={{ marginTop: 12 }}>
              <div>Health: {health ? (health.status || JSON.stringify(health)) : '-'}</div>
              {httpMetrics && (
                <div style={{ marginTop: 8 }}>
                  <div>Nombre: {httpMetrics.name}</div>
                  <div>
                    {httpMetrics.measurements?.map((m, idx) => (
                      <span key={idx} style={{ marginRight: 12 }}>{m.statistic}: {m.value}</span>
                    ))}
                  </div>
                  <div style={{ marginTop: 6 }}>
                    {httpMetrics.availableTags?.map((t, idx) => (
                      <span key={idx} style={{ marginRight: 12 }}>{t.tag}: {t.values?.slice(0, 5).join(', ')}</span>
                    ))}
                  </div>
                  {httpCounts && (
                    <div style={{ marginTop: 12 }}>
                      <div style={{ fontWeight: 600, marginBottom: 6 }}>Conteo por método/estado</div>
                      <div style={{ display: 'grid', gridTemplateColumns: `140px repeat(${httpCounts.statuses.length}, 1fr) 120px`, gap: 6, alignItems: 'center' }}>
                        <div style={{ fontWeight: 600 }}>Método</div>
                        {httpCounts.statuses.map(s => (
                          <div key={s} style={{ background: '#1e1f23', padding: 6, borderRadius: 6, textAlign: 'center' }}>{s}</div>
                        ))}
                        <div style={{ background: '#1e1f23', padding: 6, borderRadius: 6, textAlign: 'center' }}>Total</div>
                        {httpCounts.methods.map(m => {
                          const totalFila = httpCounts.statuses.reduce((acc, s) => acc + Number(httpCounts.grid[m]?.[s] ?? 0), 0)
                          return (
                            <div key={`row-${m}`} style={{ display: 'contents' }}>
                              <div style={{ background: '#1e1f23', padding: 6, borderRadius: 6 }}>{m}</div>
                              {httpCounts.statuses.map(s => (
                                <div key={`${m}-${s}`} style={{ background: '#1e1f23', padding: 6, borderRadius: 6, textAlign: 'center' }}>{Number(httpCounts.grid[m]?.[s] ?? 0).toFixed(0)}</div>
                              ))}
                              <div style={{ background: '#1e1f23', padding: 6, borderRadius: 6, textAlign: 'center', fontWeight: 600 }}>{totalFila.toFixed(0)}</div>
                            </div>
                          )
                        })}
                        <div style={{ fontWeight: 600 }}>Total</div>
                        {httpCounts.statuses.map(s => {
                          const totalCol = httpCounts.methods.reduce((acc, m) => acc + Number(httpCounts.grid[m]?.[s] ?? 0), 0)
                          return (
                            <div key={`col-${s}`} style={{ background: '#1e1f23', padding: 6, borderRadius: 6, textAlign: 'center', fontWeight: 600 }}>{totalCol.toFixed(0)}</div>
                          )
                        })}
                        <div style={{ background: '#1e1f23', padding: 6, borderRadius: 6, textAlign: 'center', fontWeight: 700 }}>
                          {(() => {
                            const grand = httpCounts.methods.reduce((acc, m) => acc + httpCounts.statuses.reduce((acc2, s) => acc2 + Number(httpCounts.grid[m]?.[s] ?? 0), 0), 0)
                            return grand.toFixed(0)
                          })()}
                        </div>
                      </div>
                      <div style={{ marginTop: 12, display: 'flex', gap: 8 }}>
                        {['2', '3', '4', '5'].map(prefix => {
                          const total = httpCounts.methods.reduce((acc, m) => acc + httpCounts.statuses.filter(s => String(s).startsWith(prefix)).reduce((acc2, s) => acc2 + Number(httpCounts.grid[m]?.[s] ?? 0), 0), 0)
                          const label = prefix + 'xx'
                          return <div key={prefix} style={{ background: '#1e1f23', padding: 6, borderRadius: 6 }}>{label}: {total.toFixed(0)}</div>
                        })}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default App
