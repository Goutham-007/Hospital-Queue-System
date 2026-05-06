import { useAuth } from '../context/AuthContext'
import { useNavigate, Link } from 'react-router-dom'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const roleLabel = {
    PATIENT: '🧑‍⚕️ Patient',
    DOCTOR:  '👨‍⚕️ Doctor',
    ADMIN:   '🔧 Admin'
  }

  return (
    <div className="navbar">
      <h1>🏥 Hospital Queue System</h1>
      {user && (
        <div className="navbar-right">
          {user.role === 'PATIENT' && (
            <>
              <Link to="/patient" style={{ color: 'white', fontSize: '0.88rem', textDecoration: 'none', opacity: 0.9 }}>My Bookings</Link>
              <Link to="/patient/search" style={{ color: 'white', fontSize: '0.88rem', textDecoration: 'none', opacity: 0.9 }}>Find Doctor</Link>
            </>
          )}
          <span style={{ opacity: 0.7 }}>|</span>
          <span>{roleLabel[user.role]} — {user.name}</span>
          <button onClick={handleLogout}>Logout</button>
        </div>
      )}
    </div>
  )
}
