import { useState, useEffect, useRef } from 'react'
import { bookingApi } from '../../api'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export default function PatientDashboard() {
  const [bookings, setBookings]   = useState([])
  const [loading, setLoading]     = useState(true)
  const [queue, setQueue]         = useState([])
  const [activeBooking, setActiveBooking] = useState(null)
  const stompRef = useRef(null)

  useEffect(() => {
    loadBookings()
  }, [])

  // Connect WebSocket when active booking selected
  useEffect(() => {
    if (!activeBooking) return
    connectWebSocket(activeBooking.doctorId)
    return () => { if (stompRef.current) stompRef.current.deactivate() }
  }, [activeBooking])

  const loadBookings = async () => {
    try {
      const res = await bookingApi.getMyBookings()
      setBookings(res.data)
      // Auto-select today's confirmed booking
      const today = new Date().toISOString().split('T')[0]
      const todayBooking = res.data.find(b =>
        b.status === 'CONFIRMED' && b.bookingDate === today
      )
      if (todayBooking) setActiveBooking(todayBooking)
    } catch (err) {
      console.error(err)
    } finally { setLoading(false) }
  }

  const connectWebSocket = (doctorId) => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      onConnect: () => {
        client.subscribe(`/topic/queue/${doctorId}`, (msg) => {
          setQueue(JSON.parse(msg.body))
        })
        // Load initial queue via REST
        bookingApi.getQueue(doctorId).then(r => setQueue(r.data))
      },
      onStompError: (err) => console.error('WebSocket error', err)
    })
    client.activate()
    stompRef.current = client
  }

  const cancelBooking = async (bookingId) => {
    if (!confirm('Cancel this booking?')) return
    try {
      await bookingApi.cancelBooking(bookingId)
      loadBookings()
    } catch (err) {
      alert(err.response?.data?.message || 'Cannot cancel')
    }
  }

  const getMyQueuePosition = () => {
    if (!activeBooking || !queue.length) return null
    const idx = queue.findIndex(q => q.tokenNumber === activeBooking.tokenNumber)
    return idx === -1 ? null : idx
  }

  const myPosition = getMyQueuePosition()

  if (loading) return <div className="loading">Loading your bookings...</div>

  return (
    <div className="container">
      <h2 className="page-title">My Dashboard</h2>

      {/* Today's active booking token card */}
      {activeBooking && (
        <div className="token-card">
          <div className="token-label">Your Token Number</div>
          <div className="token-number">#{activeBooking.tokenNumber}</div>
          <div className="token-meta">
            Dr. {activeBooking.doctorName} · {activeBooking.slotTime}
            {myPosition !== null && (
              <div style={{ marginTop: 8, fontSize: '1.1rem', fontWeight: 700 }}>
                {myPosition === 0
                  ? '🟢 It\'s your turn!'
                  : `${myPosition} patient${myPosition > 1 ? 's' : ''} ahead of you`}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Live Queue for today's doctor */}
      {activeBooking && queue.length > 0 && (
        <div className="card">
          <div className="card-title">🔴 Live Queue — Dr. {activeBooking.doctorName}</div>
          <div className="queue-list">
            {queue.map((q, i) => (
              <div key={q.bookingId}
                className={`queue-row ${i === 0 ? 'active' : ''} ${q.tokenNumber === activeBooking.tokenNumber ? 'mine' : ''}`}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <span className="queue-token">#{q.tokenNumber}</span>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>
                      {q.tokenNumber === activeBooking.tokenNumber ? '👤 You' : `Patient`}
                    </div>
                    <div style={{ fontSize: '0.8rem', color: '#6b7280' }}>{q.slotTime}</div>
                  </div>
                </div>
                {i === 0 && <span className="badge badge-confirmed">Now</span>}
                {q.tokenNumber === activeBooking.tokenNumber && i !== 0 && (
                  <span className="badge badge-pending">Your Turn</span>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* All Bookings List */}
      <div className="card">
        <div className="card-title">📋 All My Bookings</div>
        {bookings.length === 0 ? (
          <div className="empty">
            <div className="empty-icon">📅</div>
            <p>No bookings yet. <a href="/patient/search" style={{ color: '#1a56db' }}>Book a doctor</a></p>
          </div>
        ) : (
          bookings.map(b => (
            <div key={b.bookingId} style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              padding: '14px 0', borderBottom: '1px solid #f1f5f9'
            }}>
              <div>
                <div style={{ fontWeight: 700 }}>Dr. {b.doctorName}</div>
                <div style={{ fontSize: '0.83rem', color: '#6b7280' }}>
                  {b.specialization} · {b.bookingDate} · {b.slotTime}
                </div>
                {b.tokenNumber && (
                  <div style={{ fontSize: '0.83rem', color: '#1a56db', fontWeight: 600 }}>
                    Token #{b.tokenNumber}
                  </div>
                )}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <span className={`badge badge-${b.status.toLowerCase()}`}>{b.status}</span>
                {(b.status === 'PENDING' || b.status === 'CONFIRMED') && (
                  <button className="btn btn-danger btn-sm"
                    onClick={() => cancelBooking(b.bookingId)}>Cancel</button>
                )}
                {b.status === 'CONFIRMED' && b.bookingDate === new Date().toISOString().split('T')[0] && (
                  <button className="btn btn-primary btn-sm"
                    onClick={() => setActiveBooking(b)}>Track</button>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
