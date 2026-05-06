import { useState, useEffect, useRef } from 'react'
import { doctorApi } from '../../api'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuth } from '../../context/AuthContext'

const DAYS = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY']

export default function DoctorDashboard() {
  const { user } = useAuth()
  const [tab, setTab]             = useState('queue')
  const [queue, setQueue]         = useState([])
  const [dashboard, setDashboard] = useState(null)
  const [availability, setAvailability] = useState([])
  const [leaves, setLeaves]       = useState([])
  const [loading, setLoading]     = useState(true)
  const [doctorId, setDoctorId]   = useState(null)
  const stompRef = useRef(null)

  // Availability form
  const [avForm, setAvForm] = useState({
    dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '13:00',
    slotDurationMinutes: 20, maxPatientsPerDay: 12
  })
  // Leave form
  const [leaveDate, setLeaveDate] = useState('')
  const [leaveReason, setLeaveReason] = useState('')
  const [msg, setMsg] = useState('')

  useEffect(() => {
    loadAll()
  }, [])

  const loadAll = async () => {
    try {
      const [dashRes, avRes, leaveRes] = await Promise.all([
        doctorApi.getDashboard(),
        doctorApi.getAvailability(),
        doctorApi.getLeaves()
      ])
      setDashboard(dashRes.data)
      setQueue(dashRes.data.liveQueue || [])
      setAvailability(avRes.data)
      setLeaves(leaveRes.data)

      // Get doctorId from queue or fetch separately
      if (dashRes.data.liveQueue?.length > 0) {
        // Connect WebSocket once we know who we are
        // For now use REST polling — WS connection needs doctorId
      }
    } catch (err) {
      console.error(err)
    } finally { setLoading(false) }
  }

  const markDone = async (bookingId) => {
    try {
      await doctorApi.markTokenDone(bookingId)
      // Reload queue
      const res = await doctorApi.getDashboard()
      setDashboard(res.data)
      setQueue(res.data.liveQueue || [])
    } catch (err) {
      alert(err.response?.data?.message || 'Error')
    }
  }

  const saveAvailability = async (e) => {
    e.preventDefault()
    setMsg('')
    try {
      await doctorApi.setAvailability({
        ...avForm,
        slotDurationMinutes: parseInt(avForm.slotDurationMinutes),
        maxPatientsPerDay: parseInt(avForm.maxPatientsPerDay)
      })
      setMsg('✅ Availability saved!')
      loadAll()
    } catch (err) {
      setMsg('❌ ' + (err.response?.data?.message || 'Error saving'))
    }
  }

  const markLeave = async (e) => {
    e.preventDefault()
    setMsg('')
    try {
      await doctorApi.markLeave({ leaveDate, reason: leaveReason })
      setMsg('✅ Leave marked!')
      setLeaveDate(''); setLeaveReason('')
      loadAll()
    } catch (err) {
      setMsg('❌ ' + (err.response?.data?.message || 'Error'))
    }
  }

  const cancelLeave = async (date) => {
    try {
      await doctorApi.cancelLeave(date)
      loadAll()
    } catch (err) { alert('Error cancelling leave') }
  }

  if (loading) return <div className="loading">Loading dashboard...</div>

  return (
    <div className="container">
      <h2 className="page-title">Doctor Dashboard</h2>

      {/* Stats */}
      {dashboard && (
        <div className="stats-row">
          <div className="stat-card">
            <div className="stat-value">{dashboard.patientsSeenToday}</div>
            <div className="stat-label">Patients Today</div>
          </div>
          <div className="stat-card">
            <div className="stat-value" style={{ color: '#059669' }}>
              ₹{dashboard.earningsToday || 0}
            </div>
            <div className="stat-label">Earnings Today</div>
          </div>
          <div className="stat-card">
            <div className="stat-value" style={{ color: '#d97706' }}>
              {queue.length}
            </div>
            <div className="stat-label">In Queue Now</div>
          </div>
        </div>
      )}

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 4, marginBottom: 20, background: 'white', padding: 6, borderRadius: 10, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
        {[['queue','🔴 Live Queue'],['availability','📅 Availability'],['leave','🏖 Leave']].map(([key, label]) => (
          <button key={key}
            className={`btn ${tab === key ? 'btn-primary' : 'btn-secondary'}`}
            style={{ flex: 1 }}
            onClick={() => setTab(key)}>
            {label}
          </button>
        ))}
      </div>

      {/* Live Queue Tab */}
      {tab === 'queue' && (
        <div className="card">
          <div className="card-title">Today's Queue</div>
          {queue.length === 0 ? (
            <div className="empty">
              <div className="empty-icon">✅</div>
              <p>No patients in queue right now</p>
            </div>
          ) : (
            <div className="queue-list">
              {queue.map((q, i) => (
                <div key={q.bookingId} className={`queue-row ${i === 0 ? 'active' : ''}`}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                    <span className="queue-token">#{q.tokenNumber}</span>
                    <div>
                      <div style={{ fontWeight: 700 }}>{q.patientName}</div>
                      <div style={{ fontSize: '0.82rem', color: '#6b7280' }}>Slot: {q.slotTime}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    {i === 0 && <span className="badge badge-confirmed">Now Serving</span>}
                    {i === 0 && (
                      <button className="btn btn-success btn-sm"
                        onClick={() => markDone(q.bookingId)}>
                        ✓ Done
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Availability Tab */}
      {tab === 'availability' && (
        <div className="card">
          <div className="card-title">Set Working Hours</div>
          {msg && <div className={`alert ${msg.startsWith('✅') ? 'alert-success' : 'alert-error'}`}>{msg}</div>}
          <form onSubmit={saveAvailability}>
            <div className="form-row">
              <div className="form-group">
                <label>Day of Week</label>
                <select value={avForm.dayOfWeek} onChange={e => setAvForm(f => ({ ...f, dayOfWeek: e.target.value }))}>
                  {DAYS.map(d => <option key={d} value={d}>{d}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Slot Duration (minutes)</label>
                <select value={avForm.slotDurationMinutes} onChange={e => setAvForm(f => ({ ...f, slotDurationMinutes: e.target.value }))}>
                  <option value="10">10 min</option>
                  <option value="15">15 min</option>
                  <option value="20">20 min</option>
                  <option value="30">30 min</option>
                </select>
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Start Time</label>
                <input type="time" value={avForm.startTime}
                  onChange={e => setAvForm(f => ({ ...f, startTime: e.target.value }))} />
              </div>
              <div className="form-group">
                <label>End Time</label>
                <input type="time" value={avForm.endTime}
                  onChange={e => setAvForm(f => ({ ...f, endTime: e.target.value }))} />
              </div>
            </div>
            <div className="form-group">
              <label>Max Patients Per Day</label>
              <input type="number" min="1" max="200" value={avForm.maxPatientsPerDay}
                onChange={e => setAvForm(f => ({ ...f, maxPatientsPerDay: e.target.value }))} />
            </div>
            <button className="btn btn-primary">Save Availability</button>
          </form>

          {availability.length > 0 && (
            <div style={{ marginTop: 24 }}>
              <div style={{ fontWeight: 700, marginBottom: 10 }}>Current Schedule</div>
              {availability.map(a => (
                <div key={a.id} style={{
                  display: 'flex', justifyContent: 'space-between',
                  padding: '10px 14px', background: '#f8fafc', borderRadius: 8, marginBottom: 6
                }}>
                  <span style={{ fontWeight: 600 }}>{a.dayOfWeek}</span>
                  <span>{a.startTime} – {a.endTime}</span>
                  <span style={{ color: '#1a56db' }}>Every {a.slotDurationMinutes} min</span>
                  <span style={{ color: '#6b7280' }}>Max {a.maxPatientsPerDay} patients</span>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Leave Tab */}
      {tab === 'leave' && (
        <div className="card">
          <div className="card-title">Mark Leave / Holiday</div>
          {msg && <div className={`alert ${msg.startsWith('✅') ? 'alert-success' : 'alert-error'}`}>{msg}</div>}
          <form onSubmit={markLeave}>
            <div className="form-row">
              <div className="form-group">
                <label>Leave Date</label>
                <input type="date" required value={leaveDate}
                  onChange={e => setLeaveDate(e.target.value)} />
              </div>
              <div className="form-group">
                <label>Reason (optional)</label>
                <input placeholder="Holiday, conference..."
                  value={leaveReason} onChange={e => setLeaveReason(e.target.value)} />
              </div>
            </div>
            <button className="btn btn-warning">Mark Leave</button>
          </form>

          {leaves.length > 0 && (
            <div style={{ marginTop: 24 }}>
              <div style={{ fontWeight: 700, marginBottom: 10 }}>Upcoming Leaves</div>
              {leaves.map(l => (
                <div key={l.id} style={{
                  display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                  padding: '10px 14px', background: '#fff7ed', borderRadius: 8, marginBottom: 6,
                  borderLeft: '4px solid #d97706'
                }}>
                  <div>
                    <span style={{ fontWeight: 700 }}>{l.leaveDate}</span>
                    {l.reason && <span style={{ color: '#6b7280', marginLeft: 8 }}>— {l.reason}</span>}
                  </div>
                  <button className="btn btn-danger btn-sm"
                    onClick={() => cancelLeave(l.leaveDate)}>Remove</button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
