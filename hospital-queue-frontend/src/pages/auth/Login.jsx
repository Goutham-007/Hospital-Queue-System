import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { authApi } from '../../api'

export default function Login() {
  const [tab, setTab]         = useState('login')
  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)
  const { login }             = useAuth()
  const navigate              = useNavigate()

  const [form, setForm] = useState({
    name: '', email: '', password: '', phone: '',
    age: '', gender: 'MALE'
  })

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const handleLogin = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await authApi.login({ email: form.email, password: form.password })
      login(res.data)
      const role = res.data.role
      if (role === 'PATIENT') navigate('/patient')
      else if (role === 'DOCTOR') navigate('/doctor')
      else navigate('/admin')
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password')
    } finally { setLoading(false) }
  }

  const handleRegister = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      // Role is always PATIENT — doctors/admins are created by admin only
      const payload = {
        name:     form.name,
        email:    form.email,
        password: form.password,
        phone:    form.phone,
        role:     'PATIENT',
        age:      parseInt(form.age) || null,
        gender:   form.gender
      }
      const res = await authApi.register(payload)
      login(res.data)
      navigate('/patient')
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed')
    } finally { setLoading(false) }
  }

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-logo">
          <h2>🏥 Hospital Queue</h2>
          <p>Smart queue management system</p>
        </div>

        <div className="auth-tabs">
          <div className={`auth-tab ${tab === 'login' ? 'active' : ''}`}
            onClick={() => { setTab('login'); setError('') }}>
            Login
          </div>
          <div className={`auth-tab ${tab === 'register' ? 'active' : ''}`}
            onClick={() => { setTab('register'); setError('') }}>
            Register as Patient
          </div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {tab === 'login' ? (
          <form onSubmit={handleLogin}>
            <div className="form-group">
              <label>Email</label>
              <input type="email" required placeholder="you@email.com"
                value={form.email} onChange={e => set('email', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input type="password" required placeholder="••••••••"
                value={form.password} onChange={e => set('password', e.target.value)} />
            </div>
            <button className="btn btn-primary btn-full" disabled={loading}>
              {loading ? 'Logging in...' : 'Login'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleRegister}>
            <div className="form-group">
              <label>Full Name</label>
              <input required placeholder="Your full name"
                value={form.name} onChange={e => set('name', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input type="email" required placeholder="you@email.com"
                value={form.email} onChange={e => set('email', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Phone</label>
              <input required placeholder="9876543210"
                value={form.phone} onChange={e => set('phone', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Password (min 6 chars)</label>
              <input type="password" required minLength={6} placeholder="••••••••"
                value={form.password} onChange={e => set('password', e.target.value)} />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Age</label>
                <input type="number" placeholder="28"
                  value={form.age} onChange={e => set('age', e.target.value)} />
              </div>
              <div className="form-group">
                <label>Gender</label>
                <select value={form.gender} onChange={e => set('gender', e.target.value)}>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
            </div>
            <button className="btn btn-primary btn-full" disabled={loading}>
              {loading ? 'Creating account...' : 'Create Patient Account'}
            </button>
            <p style={{ textAlign: 'center', fontSize: '0.8rem', color: '#9ca3af', marginTop: 14 }}>
              Doctors and Admins are registered by the hospital admin only.
            </p>
          </form>
        )}
      </div>
    </div>
  )
}
