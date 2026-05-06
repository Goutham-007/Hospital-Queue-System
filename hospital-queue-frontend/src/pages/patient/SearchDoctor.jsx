import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { publicApi } from '../../api'

export default function SearchDoctor() {
  const [specs, setSpecs]         = useState([])
  const [doctors, setDoctors]     = useState([])
  const [selectedSpec, setSelectedSpec] = useState('')
  const [loading, setLoading]     = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    publicApi.getSpecializations().then(r => setSpecs(r.data))
    loadDoctors('')
  }, [])

  const loadDoctors = async (specId) => {
    setLoading(true)
    try {
      const res = await publicApi.getDoctors(specId || null)
      setDoctors(res.data)
    } finally { setLoading(false) }
  }

  const handleSpecChange = (specId) => {
    setSelectedSpec(specId)
    loadDoctors(specId)
  }

  const handleSelectDoctor = (doctorId) => {
    navigate(`/patient/slots/${doctorId}`)
  }

  return (
    <div className="container">
      <h2 className="page-title">Find a Doctor</h2>

      <div className="card">
        <div className="form-group" style={{ marginBottom: 0 }}>
          <label>Filter by Specialization</label>
          <select value={selectedSpec} onChange={e => handleSpecChange(e.target.value)}>
            <option value="">All Specializations</option>
            {specs.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
        </div>
      </div>

      {loading ? (
        <div className="loading">Finding doctors...</div>
      ) : doctors.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">👨‍⚕️</div>
          <p>No doctors found for this specialization</p>
        </div>
      ) : (
        <div className="doctor-grid">
          {doctors.map(d => (
            <div key={d.doctorId} className="doctor-card" onClick={() => handleSelectDoctor(d.doctorId)}>
              <div className="doctor-avatar">{d.name.charAt(0)}</div>
              <div className="doctor-name">Dr. {d.name}</div>
              <div className="doctor-spec">{d.specialization}</div>
              <div className="doctor-info">🎓 {d.qualification}</div>
              <div className="doctor-info">⏱ {d.experienceYears} years experience</div>
              {d.bio && <div className="doctor-info" style={{ marginTop: 8, fontStyle: 'italic' }}>"{d.bio}"</div>}
              <div className="doctor-fee">₹{d.consultationFee}</div>
              <button className="btn btn-primary btn-sm" style={{ marginTop: 12, width: '100%' }}>
                Book Appointment →
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
