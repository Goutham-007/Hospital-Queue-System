import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import MockPayment from './pages/patient/MockPayment'
import Login          from './pages/auth/Login'
import PatientDashboard from './pages/patient/PatientDashboard'
import SearchDoctor   from './pages/patient/SearchDoctor'
import DoctorSlots    from './pages/patient/DoctorSlots'
import DoctorDashboard from './pages/doctor/DoctorDashboard'
import AdminDashboard from './pages/admin/AdminDahsboard'

function RootRedirect() {
  const { user, loading } = useAuth()
  if (loading) return <div className="loading">Loading...</div>
  if (!user) return <Navigate to="/login" replace />
  if (user.role === 'PATIENT') return <Navigate to="/patient" replace />
  if (user.role === 'DOCTOR')  return <Navigate to="/doctor" replace />
  if (user.role === 'ADMIN')   return <Navigate to="/admin" replace />
  return <Navigate to="/login" replace />
}

function AppLayout({ children }) {
  return (
    <>
      <Navbar />
      {children}
    </>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<RootRedirect />} />
          <Route path="/login" element={<Login />} />

          {/* Patient Routes */}
          <Route path="/patient" element={
            <ProtectedRoute role="PATIENT">
              <AppLayout><PatientDashboard /></AppLayout>
            </ProtectedRoute>
          } />
          <Route path="/patient/search" element={
            <ProtectedRoute role="PATIENT">
              <AppLayout><SearchDoctor /></AppLayout>
            </ProtectedRoute>
          } />
          <Route path="/patient/slots/:doctorId" element={
            <ProtectedRoute role="PATIENT">
              <AppLayout><DoctorSlots /></AppLayout>
            </ProtectedRoute>
          } />
          <Route path="/patient/payment" element={
  <ProtectedRoute role="PATIENT">
    <MockPayment />
  </ProtectedRoute>
} />

          {/* Doctor Routes */}
          <Route path="/doctor" element={
            <ProtectedRoute role="DOCTOR">
              <AppLayout><DoctorDashboard /></AppLayout>
            </ProtectedRoute>
          } />

          {/* Admin Routes */}
          <Route path="/admin" element={
            <ProtectedRoute role="ADMIN">
              <AppLayout><AdminDashboard /></AppLayout>
            </ProtectedRoute>
          } />

          {/* Catch all */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
