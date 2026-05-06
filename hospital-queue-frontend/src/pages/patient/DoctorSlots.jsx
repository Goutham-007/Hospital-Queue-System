import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { publicApi, bookingApi } from '../../api'

export default function DoctorSlots() {
  const { doctorId } = useParams()
  const navigate = useNavigate()

  const [doctor, setDoctor]       = useState(null)
  const [slots, setSlots]         = useState([])
  const [selectedDate, setSelectedDate] = useState('')
  const [selectedSlot, setSelectedSlot] = useState('')
  const [loading, setLoading]     = useState(false)
  const [slotsLoading, setSlotsLoading] = useState(false)
  const [error, setError]         = useState('')
  const [success, setSuccess]     = useState('')

  // Get today's date as min date for booking
  const today = new Date().toISOString().split('T')[0]

  useEffect(() => {
    // Load doctor info
    publicApi.getDoctors().then(res => {
      const doc = res.data.find(d => d.doctorId === parseInt(doctorId))
      setDoctor(doc)
    })
  }, [doctorId])

  const loadSlots = async (date) => {
    if (!date) return
    setSlotsLoading(true)
    setSlots([])
    setSelectedSlot('')
    setError('')
    try {
      const res = await publicApi.getSlots(doctorId, date)
      setSlots(res.data)
      if (res.data.length === 0) setError('Doctor is not available on this date.')
    } catch (err) {
      setError('Could not load slots.')
    } finally { setSlotsLoading(false) }
  }

  const handleDateChange = (date) => {
    setSelectedDate(date)
    loadSlots(date)
  }

  // const openRazorpay = (orderData) => {
  //   const options = {
  //     key: orderData.keyId,
  //     amount: orderData.amount * 100,
  //     currency: 'INR',
  //     name: 'Hospital Queue System',
  //     description: `Consultation with Dr. ${doctor?.name}`,
  //     order_id: orderData.razorpayOrderId,
  //     handler: function (response) {
  //       // Payment done on Razorpay side
  //       // Backend webhook will confirm and assign token
  //       setSuccess(`✅ Payment successful! Your booking is confirmed. Token will be assigned shortly. Check your dashboard.`)
  //       setTimeout(() => navigate('/patient'), 3000)
  //     },
  //     prefill: {
  //       name: orderData.customerName,
  //       email: orderData.customerEmail,
  //       contact: orderData.customerPhone
  //     },
  //     theme: { color: '#1a56db' },
  //     modal: {
  //       ondismiss: function () {
  //         setError('Payment cancelled. Your booking slot has been held for 15 minutes.')
  //       }
  //     }
  //   }
  //   const rzp = new window.Razorpay(options)
  //   rzp.open()
  // }

 const handleBook = async () => {
  if (!selectedSlot || !selectedDate) {
    setError('Please select a date and time slot')
    return
  }
  setLoading(true)
  setError('')
  try {
    const res = await bookingApi.createBooking({
      doctorId: parseInt(doctorId),
      bookingDate: selectedDate,
      slotTime: selectedSlot
    })
    // Store booking info and go to mock payment page
    navigate(`/patient/payment`, {
      state: {
        bookingId: res.data.bookingId,
        razorpayOrderId: res.data.razorpayOrderId,
        amount: res.data.amount,
        doctorName: doctor?.name
      }
    })
  } catch (err) {
    setError(err.response?.data?.message || 'Slot already taken. Choose another.')
  } finally { setLoading(false) }
}
  const availableSlots = slots.filter(s => s.available)
  const bookedSlots    = slots.filter(s => !s.available)

  return (
    <div className="container">
      <button className="btn btn-secondary btn-sm" style={{ marginBottom: 16 }}
        onClick={() => navigate('/patient/search')}>← Back to Doctors</button>

      {doctor && (
        <div className="card" style={{ display: 'flex', gap: 20, alignItems: 'flex-start' }}>
          <div className="doctor-avatar" style={{ width: 64, height: 64, fontSize: '1.8rem', flexShrink: 0 }}>
            {doctor.name.charAt(0)}
          </div>
          <div>
            <div className="doctor-name" style={{ fontSize: '1.2rem' }}>Dr. {doctor.name}</div>
            <div className="doctor-spec">{doctor.specialization}</div>
            <div className="doctor-info">🎓 {doctor.qualification} · ⏱ {doctor.experienceYears} yrs</div>
            <div className="doctor-fee" style={{ marginTop: 8 }}>Consultation Fee: ₹{doctor.consultationFee}</div>
          </div>
        </div>
      )}

      <div className="card">
        <div className="card-title">Select Date & Time Slot</div>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <div className="form-group">
          <label>Select Date</label>
          <input type="date" min={today}
            value={selectedDate}
            onChange={e => handleDateChange(e.target.value)} />
        </div>

        {slotsLoading && <div className="loading">Loading available slots...</div>}

        {!slotsLoading && slots.length > 0 && (
          <>
            <div style={{ marginBottom: 8 }}>
              <span style={{ fontSize: '0.88rem', color: '#6b7280' }}>
                {availableSlots.length} slots available · {bookedSlots.length} booked
              </span>
            </div>
            <div className="slots-grid">
              {slots.map(slot => (
                <div key={slot.time}
                  className={`slot-btn ${!slot.available ? 'booked' : selectedSlot === slot.time ? 'selected' : 'available'}`}
                  onClick={() => slot.available && setSelectedSlot(slot.time)}>
                  {slot.time}
                  {!slot.available && <div style={{ fontSize: '0.7rem' }}>Booked</div>}
                </div>
              ))}
            </div>

            {selectedSlot && (
              <div style={{ marginTop: 20, padding: 16, background: '#eff6ff', borderRadius: 10 }}>
                <div style={{ fontWeight: 700, marginBottom: 8 }}>Booking Summary</div>
                <div style={{ fontSize: '0.9rem', color: '#374151' }}>
                  📅 {selectedDate} &nbsp;·&nbsp; 🕐 {selectedSlot}<br />
                  👨‍⚕️ Dr. {doctor?.name} &nbsp;·&nbsp; 💰 ₹{doctor?.consultationFee}
                </div>
                <button className="btn btn-primary" style={{ marginTop: 14, width: '100%' }}
                  onClick={handleBook} disabled={loading}>
                  {loading ? 'Processing...' : `Pay ₹${doctor?.consultationFee} & Confirm`}
                </button>
              </div>
            )}
          </>
        )}

        {!slotsLoading && selectedDate && slots.length === 0 && !error && (
          <div className="alert alert-info">No slots available on this date. Try another date.</div>
        )}
      </div>
    </div>
  )
}
