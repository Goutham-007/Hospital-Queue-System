import { useState, useEffect } from 'react'
import { adminApi, publicApi } from '../../api'

export default function AdminDashboard() {
  const [analytics, setAnalytics]   = useState(null)
  const [doctors, setDoctors]       = useState([])
  const [specs, setSpecs]           = useState([])
  const [newSpec, setNewSpec]       = useState('')
  const [tab, setTab]               = useState('analytics')
  const [msg, setMsg]               = useState('')
  const [loading, setLoading]       = useState(true)

  // Create doctor form
  const [docForm, setDocForm] = useState({
    name: '', email: '', password: '', phone: '',
    specializationId: '', qualification: '',
    experienceYears: '', consultationFee: '', bio: ''
  })
  const setDoc = (k, v) => setDocForm(f => ({ ...f, [k]: v }))

  useEffect(() => { loadAll() }, [])

  const loadAll = async () => {
    try {
      const [aRes, dRes, sRes] = await Promise.all([
        adminApi.getAnalytics(),
        adminApi.getDoctors(),
        adminApi.getSpecializations()
      ])
      setAnalytics(aRes.data)
      setDoctors(dRes.data)
      setSpecs(sRes.data)
    } catch (err) { console.error(err) }
    finally { setLoading(false) }
  }

  const addSpec = async (e) => {
    e.preventDefault()
    if (!newSpec.trim()) return
    try {
      await adminApi.addSpecialization(newSpec.trim())
      setMsg('✅ Specialization added!')
      setNewSpec('')
      loadAll()
    } catch (err) {
      setMsg('❌ ' + (err.response?.data?.message || 'Error'))
    }
  }

  const createDoctor = async (e) => {
    e.preventDefault()
    setMsg('')
    try {
      await adminApi.createDoctor({
        ...docForm,
        role: 'DOCTOR',
        specializationId: parseInt(docForm.specializationId),
        experienceYears:  parseInt(docForm.experienceYears),
        consultationFee:  parseFloat(docForm.consultationFee)
      })
      setMsg('✅ Doctor account created!')
      setDocForm({ name:'', email:'', password:'', phone:'', specializationId:'', qualification:'', experienceYears:'', consultationFee:'', bio:'' })
      loadAll()
    } catch (err) {
      setMsg('❌ ' + (err.response?.data?.message || 'Error creating doctor'))
    }
  }

  const toggleDoctor = async (doctorId) => {
    try {
      await adminApi.toggleDoctor(doctorId)
      loadAll()
    } catch (err) { alert('Error') }
  }

  if (loading) return <div className="loading">Loading admin panel...</div>

  return (
    <div className="container">
      <h2 className="page-title">Admin Dashboard</h2>

      {analytics && (
        <div className="stats-row">
          <div className="stat-card">
            <div className="stat-value">{analytics.totalDoctors}</div>
            <div className="stat-label">Total Doctors</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{analytics.totalPatients}</div>
            <div className="stat-label">Total Patients</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{analytics.totalBookings}</div>
            <div className="stat-label">Total Bookings</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{analytics.totalSpecializations}</div>
            <div className="stat-label">Specializations</div>
          </div>
        </div>
      )}

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 4, marginBottom: 20, background: 'white', padding: 6, borderRadius: 10, boxShadow: '0 1px 4px rgba(0,0,0,0.06)', flexWrap: 'wrap' }}>
        {[['analytics','📊 Analytics'],['create-doctor','➕ Add Doctor'],['doctors','👨‍⚕️ Doctors'],['specs','🏷 Specializations']].map(([key,label]) => (
          <button key={key} className={`btn ${tab === key ? 'btn-primary' : 'btn-secondary'}`}
            style={{ flex: 1 }} onClick={() => { setTab(key); setMsg('') }}>{label}</button>
        ))}
      </div>

      {msg && <div className={`alert ${msg.startsWith('✅') ? 'alert-success' : 'alert-error'}`}>{msg}</div>}

      {tab === 'analytics' && analytics && (
        <div className="card">
          <div className="card-title">System Overview</div>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <tbody>
              {Object.entries(analytics).map(([k, v]) => (
                <tr key={k} style={{ borderBottom: '1px solid #f1f5f9' }}>
                  <td style={{ padding: '12px 8px', color: '#6b7280', textTransform: 'capitalize' }}>
                    {k.replace(/([A-Z])/g, ' $1')}
                  </td>
                  <td style={{ padding: '12px 8px', fontWeight: 700, fontSize: '1.1rem' }}>{v}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'create-doctor' && (
        <div className="card">
          <div className="card-title">Create Doctor Account</div>
          <form onSubmit={createDoctor}>
            <div className="form-row">
              <div className="form-group">
                <label>Full Name</label>
                <input required placeholder="Dr. Arjun Sharma"
                  value={docForm.name} onChange={e => setDoc('name', e.target.value)} />
              </div>
              <div className="form-group">
                <label>Phone</label>
                <input required placeholder="9876543210"
                  value={docForm.phone} onChange={e => setDoc('phone', e.target.value)} />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Email</label>
                <input type="email" required placeholder="doctor@hospital.com"
                  value={docForm.email} onChange={e => setDoc('email', e.target.value)} />
              </div>
              <div className="form-group">
                <label>Password</label>
                <input type="password" required minLength={6} placeholder="••••••••"
                  value={docForm.password} onChange={e => setDoc('password', e.target.value)} />
              </div>
            </div>
            <div className="form-group">
              <label>Specialization</label>
              <select required value={docForm.specializationId}
                onChange={e => setDoc('specializationId', e.target.value)}>
                <option value="">-- Select Specialization --</option>
                {specs.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Qualification</label>
                <input required placeholder="MBBS, MD Cardiology"
                  value={docForm.qualification} onChange={e => setDoc('qualification', e.target.value)} />
              </div>
              <div className="form-group">
                <label>Experience (years)</label>
                <input type="number" required placeholder="5"
                  value={docForm.experienceYears} onChange={e => setDoc('experienceYears', e.target.value)} />
              </div>
            </div>
            <div className="form-group">
              <label>Consultation Fee (₹)</label>
              <input type="number" required placeholder="500"
                value={docForm.consultationFee} onChange={e => setDoc('consultationFee', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Bio (optional)</label>
              <textarea rows={2} placeholder="Brief description about the doctor..."
                value={docForm.bio} onChange={e => setDoc('bio', e.target.value)} />
            </div>
            <button className="btn btn-primary">Create Doctor Account</button>
          </form>
        </div>
      )}

      {tab === 'doctors' && (
        <div className="card">
          <div className="card-title">All Doctors ({doctors.length})</div>
          {doctors.length === 0 ? (
            <div className="empty">
              <div className="empty-icon">👨‍⚕️</div>
              <p>No doctors yet. Use "Add Doctor" tab to create one.</p>
            </div>
          ) : doctors.map(d => (
            <div key={d.id} style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              padding: '14px 0', borderBottom: '1px solid #f1f5f9'
            }}>
              <div>
                <div style={{ fontWeight: 700 }}>Dr. {d.user?.name}</div>
                <div style={{ fontSize: '0.82rem', color: '#6b7280' }}>
                  {d.specialization?.name} · {d.user?.email}
                </div>
                <div style={{ fontSize: '0.82rem', color: '#6b7280' }}>
                  ₹{d.consultationFee} · {d.experienceYears} yrs exp
                </div>
              </div>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <span className={`badge ${d.isActive ? 'badge-confirmed' : 'badge-cancelled'}`}>
                  {d.isActive ? 'Active' : 'Inactive'}
                </span>
                <button className={`btn btn-sm ${d.isActive ? 'btn-danger' : 'btn-success'}`}
                  onClick={() => toggleDoctor(d.id)}>
                  {d.isActive ? 'Deactivate' : 'Activate'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {tab === 'specs' && (
        <div className="card">
          <div className="card-title">Manage Specializations</div>
          <form onSubmit={addSpec} style={{ display: 'flex', gap: 10, marginBottom: 20 }}>
            <input style={{ flex: 1, padding: '10px 14px', border: '1.5px solid #d1d5db', borderRadius: 8, fontSize: '0.95rem' }}
              placeholder="e.g. Neurology" value={newSpec}
              onChange={e => setNewSpec(e.target.value)} />
            <button className="btn btn-primary">Add</button>
          </form>
          <div>
            {specs.map(s => (
              <div key={s.id} style={{
                padding: '10px 14px', background: '#f8fafc',
                borderRadius: 8, marginBottom: 6,
                display: 'flex', justifyContent: 'space-between'
              }}>
                <span style={{ fontWeight: 600 }}>🏥 {s.name}</span>
                <span style={{ color: '#9ca3af', fontSize: '0.8rem' }}>ID: {s.id}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
