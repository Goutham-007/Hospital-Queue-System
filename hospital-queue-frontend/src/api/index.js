import api from './axiosInstance'

export const authApi = {
  register: (data)          => api.post('/api/auth/register', data),
  login:    (data)          => api.post('/api/auth/login', data),
  updateFcm:(token)         => api.post('/api/auth/fcm-token', { fcmToken: token }),
}

export const publicApi = {
  getSpecializations: ()              => api.get('/api/public/specializations'),
  getDoctors:        (specId)         => api.get('/api/public/doctors', { params: specId ? { specializationId: specId } : {} }),
  getSlots:          (doctorId, date) => api.get(`/api/public/doctors/${doctorId}/slots`, { params: { date } }),
}

export const bookingApi = {
  createBooking:  (data)      => api.post('/api/bookings', data),
  getMyBookings:  ()          => api.get('/api/patient/bookings'),
  getBooking:     (id)        => api.get(`/api/patient/bookings/${id}`),
  cancelBooking:  (id)        => api.delete(`/api/patient/bookings/${id}`),
  getQueue:       (doctorId)  => api.get(`/api/patient/queue/${doctorId}`),
}

export const doctorApi = {
  setAvailability:  (data)      => api.post('/api/doctor/availability', data),
  getAvailability:  ()          => api.get('/api/doctor/availability'),
  markLeave:        (data)      => api.post('/api/doctor/leave', data),
  cancelLeave:      (date)      => api.delete(`/api/doctor/leave/${date}`),
  getLeaves:        ()          => api.get('/api/doctor/leave'),
  getLiveQueue:     ()          => api.get('/api/doctor/queue'),
  markTokenDone:    (bookingId) => api.post(`/api/doctor/queue/done/${bookingId}`),
  getDashboard:     ()          => api.get('/api/doctor/dashboard'),
}

export const adminApi = {
  getAnalytics:       ()      => api.get('/api/admin/analytics'),
  addSpecialization:  (name)  => api.post('/api/admin/specializations', { name }),
  getSpecializations: ()      => api.get('/api/admin/specializations'),
  getDoctors:         ()      => api.get('/api/admin/doctors'),
  toggleDoctor:       (id)    => api.put(`/api/admin/doctors/${id}/toggle`),
  createDoctor:       (data)  => api.post('/api/admin/doctors/create', data),
}
