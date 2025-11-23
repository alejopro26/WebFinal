import { useEffect, useMemo, useState } from 'react'
import './index.css'
import SockJS from 'sockjs-client'
import { Client as StompClient } from '@stomp/stompjs'

function App() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [token, setToken] = useState('')
  const [rooms, setRooms] = useState([])
  const [newRoomName, setNewRoomName] = useState('')
  const [newRoomPrivate, setNewRoomPrivate] = useState(false)
  const [newRoomPassword, setNewRoomPassword] = useState('')
  const [selectedRoomId, setSelectedRoomId] = useState(null)
  const [messages, setMessages] = useState([])
  const [text, setText] = useState('')
  const [connecting, setConnecting] = useState(false)

  const stomp = useMemo(() => new StompClient({
    // debug: (str) => console.log(str),
    reconnectDelay: 3000,
  }), [])

  async function login() {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    })
    if (!res.ok) {
      alert('Error de login')
      return
    }
    const data = await res.json()
    setToken(data.token)
    await loadRooms()
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
    if (!res.ok) {
      alert('No se pudo crear la sala')
      return
    }
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
      alert('Selecciona una sala y haz login')
      return
    }
    setConnecting(true)

    const sock = new SockJS('/api/ws')
    stomp.webSocketFactory = () => sock

    stomp.onConnect = () => {
      setConnecting(false)
      stomp.subscribe(`/topic/rooms/${selectedRoomId}`, (frame) => {
        const msg = JSON.parse(frame.body)
        setMessages((prev) => [...prev, msg])
      })
      loadHistory()
    }

    stomp.onStompError = () => {
      setConnecting(false)
      alert('Error en STOMP')
    }

    // enviar JWT en frame CONNECT
    stomp.connectHeaders = { Authorization: `Bearer ${token}` }
    stomp.activate()
  }

  function send() {
    if (!text || !selectedRoomId || !stomp.active) return
    stomp.publish({
      destination: `/app/chat.send/${selectedRoomId}`,
      body: JSON.stringify({ content: text })
    })
    setText('')
  }

  useEffect(() => {
    return () => { if (stomp.active) stomp.deactivate() }
  }, [stomp])

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: 24 }}>
      <h2>ChatNexus</h2>

      {!token && (
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <input placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)} />
          <input placeholder="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          <button onClick={login}>Login</button>
        </div>
      )}

      {token && (
        <div style={{ marginTop: 16 }}>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <input placeholder="Nueva sala" value={newRoomName} onChange={(e) => setNewRoomName(e.target.value)} />
            <label>
              <input type="checkbox" checked={newRoomPrivate} onChange={(e) => setNewRoomPrivate(e.target.checked)} /> privada
            </label>
            {newRoomPrivate && (
              <input placeholder="Contraseña" value={newRoomPassword} onChange={(e) => setNewRoomPassword(e.target.value)} />
            )}
            <button onClick={createRoom}>Crear</button>
            <button onClick={loadRooms}>Refrescar salas</button>
          </div>

          <div style={{ marginTop: 12 }}>
            <select value={selectedRoomId ?? ''} onChange={(e) => setSelectedRoomId(Number(e.target.value))}>
              <option value="">Selecciona sala</option>
              {rooms.map(r => (
                <option key={r.id} value={r.id}>{r.name}</option>
              ))}
            </select>
            <button onClick={connectRoom} disabled={connecting} style={{ marginLeft: 8 }}>
              {connecting ? 'Conectando...' : 'Conectar'}
            </button>
            <button onClick={loadHistory} style={{ marginLeft: 8 }}>Cargar historial</button>
          </div>

          <div style={{ marginTop: 16 }}>
            <input style={{ width: '70%' }} placeholder="Escribe un mensaje" value={text} onChange={(e) => setText(e.target.value)} />
            <button onClick={send} style={{ marginLeft: 8 }}>Enviar</button>
          </div>

          <div style={{ marginTop: 16, background: '#1e1f23', padding: 12, borderRadius: 8 }}>
            <h4>Mensajes</h4>
            <div>
              {messages.map((m, i) => (
                <div key={i}>
                  <b>{m.sender}</b>: {m.content}
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default App
