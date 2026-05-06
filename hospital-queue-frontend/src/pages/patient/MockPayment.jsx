import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { bookingApi } from '../../api'
import api from '../../api/axiosInstance'

export default function MockPayment() {
  const navigate  = useNavigate()
  const { state } = useLocation()
  const [step, setStep]       = useState('form') // form | processing | success | failed
  const [cardNum, setCardNum] = useState('')
  const [expiry, setExpiry]   = useState('')
  const [cvv, setCvv]         = useState('')
  const [name, setName]       = useState('')

  if (!state) {
    navigate('/patient')
    return null
  }

  const { bookingId, razorpayOrderId, amount, doctorName } = state

  const handlePay = async (e) => {
    e.preventDefault()
    setStep('processing')

    // Simulate payment processing delay
    await new Promise(r => setTimeout(r, 2000))

    try {
      // Call our own backend mock webhook endpoint
      await api.post('/api/payments/mock-confirm', {
        bookingId,
        razorpayOrderId
      })
      setStep('success')
    } catch (err) {
      setStep('failed')
    }
  }

  const handleFailure = async () => {
    setStep('processing')
    await new Promise(r => setTimeout(r, 1500))
    setStep('failed')
  }

  if (step === 'processing') return (
    <div className="auth-wrapper">
      <div className="auth-card" style={{ textAlign: 'center' }}>
        <div style={{ fontSize: '3rem', marginBottom: 16 }}>⏳</div>
        <h3>Processing Payment...</h3>
        <p style={{ color: '#6b7280', marginTop: 8 }}>Please wait</p>
      </div>
    </div>
  )

  if (step === 'success') return (
    <div className="auth-wrapper">
      <div className="auth-card" style={{ textAlign: 'center' }}>
        <div style={{ fontSize: '4rem', marginBottom: 16 }}>✅</div>
        <h3 style={{ color: '#059669', fontSize: '1.4rem' }}>Payment Successful!</h3>
        <p style={{ color: '#6b7280', margin: '12px 0' }}>
          Your token has been assigned.<br />
          Check your dashboard for token number.
        </p>
        <p style={{ fontWeight: 700, color: '#1a56db', fontSize: '1.1rem', margin: '12px 0' }}>
          ₹{amount} paid for Dr. {doctorName}
        </p>
        <button className="btn btn-primary btn-full" style={{ marginTop: 16 }}
          onClick={() => navigate('/patient')}>
          Go to My Dashboard →
        </button>
      </div>
    </div>
  )

  if (step === 'failed') return (
    <div className="auth-wrapper">
      <div className="auth-card" style={{ textAlign: 'center' }}>
        <div style={{ fontSize: '4rem', marginBottom: 16 }}>❌</div>
        <h3 style={{ color: '#dc2626', fontSize: '1.4rem' }}>Payment Failed</h3>
        <p style={{ color: '#6b7280', margin: '12px 0' }}>
          Your slot is held for 15 minutes.<br />
          You can try again.
        </p>
        <div style={{ display: 'flex', gap: 10, marginTop: 16 }}>
          <button className="btn btn-primary" style={{ flex: 1 }}
            onClick={() => setStep('form')}>Try Again</button>
          <button className="btn btn-secondary" style={{ flex: 1 }}
            onClick={() => navigate('/patient')}>Cancel</button>
        </div>
      </div>
    </div>
  )

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-logo">
          <div style={{ fontSize: '2rem' }}>💳</div>
          <h2>Secure Payment</h2>
          <p>Complete your booking with Dr. {doctorName}</p>
        </div>

        {/* Amount banner */}
        <div style={{
          background: '#eff6ff', border: '2px solid #1a56db',
          borderRadius: 10, padding: '14px 18px', marginBottom: 20,
          display: 'flex', justifyContent: 'space-between', alignItems: 'center'
        }}>
          <span style={{ color: '#374151', fontWeight: 600 }}>Consultation Fee</span>
          <span style={{ color: '#1a56db', fontSize: '1.4rem', fontWeight: 900 }}>₹{amount}</span>
        </div>

        <form onSubmit={handlePay}>
          <div className="form-group">
            <label>Cardholder Name</label>
            <input required placeholder="Name on card"
              value={name} onChange={e => setName(e.target.value)} />
          </div>
          <div className="form-group">
            <label>Card Number</label>
            <input required placeholder="4111 1111 1111 1111" maxLength={19}
              value={cardNum}
              onChange={e => setCardNum(e.target.value.replace(/\D/g,'').replace(/(.{4})/g,'$1 ').trim())} />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Expiry (MM/YY)</label>
              <input required placeholder="12/27" maxLength={5}
                value={expiry} onChange={e => setExpiry(e.target.value)} />
            </div>
            <div className="form-group">
              <label>CVV</label>
              <input required placeholder="123" maxLength={3} type="password"
                value={cvv} onChange={e => setCvv(e.target.value)} />
            </div>
          </div>

          {/* Test card hint */}
          <div style={{
            background: '#fef3c7', borderRadius: 8, padding: '10px 14px',
            fontSize: '0.82rem', color: '#92400e', marginBottom: 16
          }}>
            🧪 <strong>Test Mode:</strong> Use any card details. Payment is simulated.
          </div>

          <button type="submit" className="btn btn-success btn-full">
            Pay ₹{amount} →
          </button>
          <button type="button" className="btn btn-secondary btn-full"
            style={{ marginTop: 8 }} onClick={handleFailure}>
            Simulate Payment Failure
          </button>
        </form>
      </div>
    </div>
  )
}