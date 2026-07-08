"use client";

import { useEffect, useState } from 'react';
import { apiClient, ServiceDto, StaffDto, SlotDto, BookingResponseDto, ApiError, API_BASE_URL } from '@/lib/api-client';

export default function BookingClient() {
  const [configError, setConfigError] = useState<string | null>(null);

  // Data state
  const [services, setServices] = useState<ServiceDto[]>([]);
  const [staff, setStaff] = useState<StaffDto[]>([]);
  const [slots, setSlots] = useState<SlotDto[]>([]);

  // Loading and Error states
  const [loadingServices, setLoadingServices] = useState(true);
  const [servicesError, setServicesError] = useState<string | null>(null);
  const [loadingStaff, setLoadingStaff] = useState(false);
  const [staffError, setStaffError] = useState<string | null>(null);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [slotsError, setSlotsError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // Form selections
  const [serviceSlug, setServiceSlug] = useState<string>('');
  const [staffSlug, setStaffSlug] = useState<string>('');

  const getDates = () => {
    const today = new Date();
    const max = new Date();
    max.setDate(today.getDate() + 30);
    return {
      min: today.toISOString().split('T')[0],
      max: max.toISOString().split('T')[0]
    };
  };
  const { min, max } = getDates();

  const [date, setDate] = useState<string>(min);
  const [selectedSlot, setSelectedSlot] = useState<string>(''); // startTime of the slot

  // Customer details
  const [customerName, setCustomerName] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');
  const [customerEmail, setCustomerEmail] = useState('');
  const [notes, setNotes] = useState('');

  // Success state
  const [successData, setSuccessData] = useState<{ booking: BookingResponseDto; serviceName: string } | null>(null);
  const [copied, setCopied] = useState(false);

  // Initialize
  useEffect(() => {
    if (!API_BASE_URL) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setConfigError('Configuration Error: NEXT_PUBLIC_API_BASE_URL is not set.');
      setLoadingServices(false);
      return;
    }

    apiClient.getServices()
      .then(setServices)
      .catch(() => {
        setServicesError('Failed to load services. Please try again later.');
      })
      .finally(() => setLoadingServices(false));
  }, []);

  // Handle service change
  const handleServiceChange = (newServiceSlug: string) => {
    setServiceSlug(newServiceSlug);
    setStaffSlug('');
    setSelectedSlot('');
    setSlots([]);
    setStaff([]);
    setStaffError(null);

    if (newServiceSlug) {
      setLoadingStaff(true);
      apiClient.getStaff(newServiceSlug)
        .then(setStaff)
        .catch((err) => {
          if (err instanceof ApiError && err.status === 404) {
            setStaffError('Selected service is no longer available.');
          } else {
            setStaffError('Failed to load staff for this service.');
          }
        })
        .finally(() => setLoadingStaff(false));
    }
  };

  const handleStaffChange = (newStaffSlug: string) => {
    setStaffSlug(newStaffSlug);
    setSelectedSlot('');
    setSlots([]);
  };

  const handleDateChange = (newDate: string) => {
    setDate(newDate);
    setSelectedSlot('');
    setSlots([]);
  };

  const fetchSlots = () => {
    if (!serviceSlug || !staffSlug || !date) return;

    setLoadingSlots(true);
    setSlotsError(null);
    setSelectedSlot('');

    apiClient.getAvailability(serviceSlug, staffSlug, date)
      .then(res => setSlots(res.slots))
      .catch(() => {
        setSlotsError('Failed to load available times. Please try again.');
      })
      .finally(() => setLoadingSlots(false));
  };

  // Fetch slots when service, staff, or date change
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchSlots();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serviceSlug, staffSlug, date]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!serviceSlug || !staffSlug || !date || !selectedSlot || !customerName || !customerPhone || !customerEmail) {
      return;
    }

    setSubmitting(true);
    setSubmitError(null);

    try {
      const response = await apiClient.createBooking({
        serviceSlug,
        staffSlug,
        startTime: selectedSlot,
        customerName,
        customerEmail,
        customerPhone,
        notes: notes || undefined
      });

      const serviceName = services.find(s => s.slug === serviceSlug)?.name || 'Service';
      setSuccessData({ booking: response, serviceName });
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          setSubmitError('This time slot was just booked by someone else. Please choose another time.');
          fetchSlots(); // Refresh slots
        } else if (err.status === 404) {
          setSubmitError('The selected service or staff is no longer available. Please reselect.');
        } else if (err.status === 400) {
          setSubmitError(err.message || 'Please check your details and try again.');
        } else {
          setSubmitError(err.message || 'An unexpected error occurred.');
        }
      } else {
        setSubmitError('Unable to connect. Please check your network and try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const copyToken = () => {
    if (successData?.booking.cancellationToken) {
      navigator.clipboard.writeText(successData.booking.cancellationToken);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('en-LK', { style: 'currency', currency: 'LKR' }).format(price);
  };

  const formatTime = (isoString: string) => {
    return new Intl.DateTimeFormat('en-US', {
      timeZone: 'Asia/Colombo',
      hour: 'numeric',
      minute: '2-digit'
    }).format(new Date(isoString));
  };

  if (configError) {
    return (
      <div className="flex flex-col flex-1 bg-background font-sans items-center justify-center p-8 text-center">
        <div className="p-6 bg-red-500/10 border border-red-500/20 rounded-xl max-w-lg">
          <h2 className="text-xl font-bold text-red-500 mb-2">Configuration Error</h2>
          <p className="text-red-400">{configError}</p>
        </div>
      </div>
    );
  }

  if (successData) {
    return (
      <div className="flex flex-col flex-1 bg-background font-sans items-center py-20 px-4">
        <div className="w-full max-w-2xl bg-white/5 border border-white/10 rounded-2xl p-8 sm:p-12 text-center flex flex-col items-center gap-6">
          <div className="w-16 h-16 rounded-full bg-green-500/20 text-green-500 flex items-center justify-center mb-2">
            <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path></svg>
          </div>
          <h2 className="text-3xl font-bold text-foreground">Booking Confirmed!</h2>
          <p className="text-foreground/70">
            Thank you, {customerName || 'customer'}. Your appointment is set.
          </p>

          <div className="w-full bg-black/20 rounded-xl p-6 text-left border border-white/5 my-4">
            <div className="grid grid-cols-2 gap-y-4 text-sm">
              <div className="text-foreground/50">Service</div>
              <div className="font-medium text-foreground text-right">{successData.serviceName}</div>

              <div className="text-foreground/50">Stylist</div>
              <div className="font-medium text-foreground text-right">{successData.booking.staffName}</div>

              <div className="text-foreground/50">Time</div>
              <div className="font-medium text-foreground text-right">{formatTime(successData.booking.startTime)}</div>
            </div>
          </div>

          <div className="w-full bg-amber-500/10 border border-amber-500/20 rounded-xl p-6 flex flex-col gap-4">
            <h3 className="text-amber-500 font-semibold text-left">Save Your Cancellation Token</h3>
            <p className="text-sm text-amber-500/80 text-left">
              You will need this token if you wish to cancel your appointment later. Please copy and save it now.
            </p>
            <div className="flex items-center gap-2">
              <code className="flex-1 bg-black/40 text-amber-400 p-3 rounded-lg text-sm truncate">
                {successData.booking.cancellationToken}
              </code>
              <button
                onClick={copyToken}
                className="px-4 py-3 bg-amber-500/20 hover:bg-amber-500/30 text-amber-500 rounded-lg text-sm font-medium transition-colors"
              >
                {copied ? 'Copied!' : 'Copy'}
              </button>
            </div>
          </div>

          <button
            onClick={() => window.location.reload()}
            className="mt-4 px-6 py-3 bg-white/10 hover:bg-white/20 rounded-md text-sm font-medium transition-colors"
          >
            Book Another Appointment
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col flex-1 bg-background font-sans">
      <main className="flex-1 flex flex-col items-center py-20 px-4 sm:px-6 lg:px-8">
        <div className="w-full max-w-3xl mx-auto flex flex-col gap-10">
          
          <div className="flex flex-col items-center gap-4 text-center">
            <h1 className="text-4xl sm:text-5xl font-bold tracking-tight text-foreground">
              Book Your Appointment
            </h1>
            <p className="max-w-2xl text-lg text-foreground/70">
              Select your service, stylist, and preferred time to reserve your Velvet Salon experience.
            </p>
          </div>

          <form className="flex flex-col gap-8 bg-white/5 border border-white/10 rounded-2xl p-6 sm:p-10" onSubmit={handleSubmit}>
            
            {submitError && (
              <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-lg text-sm">
                {submitError}
              </div>
            )}

            {/* Service & Stylist */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              <div className="flex flex-col gap-2">
                <label htmlFor="service" className="text-sm font-medium text-foreground">Service</label>
                <select 
                  id="service" 
                  value={serviceSlug}
                  onChange={(e) => handleServiceChange(e.target.value)}
                  disabled={loadingServices}
                  className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
                  required
                >
                  <option value="">
                    {loadingServices ? 'Loading services...' : 'Select a service...'}
                  </option>
                  {services.map(service => (
                    <option key={service.slug} value={service.slug}>
                      {service.name} ({service.durationMinutes} min, {formatPrice(service.price)})
                    </option>
                  ))}
                </select>
                {servicesError && <span className="text-xs text-red-400">{servicesError}</span>}
              </div>

              <div className="flex flex-col gap-2">
                <label htmlFor="stylist" className="text-sm font-medium text-foreground">Stylist</label>
                <select 
                  id="stylist"
                  value={staffSlug}
                  onChange={(e) => handleStaffChange(e.target.value)}
                  disabled={!serviceSlug || loadingStaff}
                  className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
                  required
                >
                  <option value="">
                    {!serviceSlug ? 'Select a service first' : loadingStaff ? 'Loading staff...' : 'Select a stylist...'}
                  </option>
                  {serviceSlug && !loadingStaff && <option value="any">Any stylist</option>}
                  {staff.map(member => (
                    <option key={member.slug} value={member.slug}>
                      {member.name} - {member.role}
                    </option>
                  ))}
                </select>
                {staffError && <span className="text-xs text-red-400">{staffError}</span>}
              </div>
            </div>

            {/* Date & Time */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              <div className="flex flex-col gap-2">
                <label htmlFor="date" className="text-sm font-medium text-foreground">Date (Up to 30 days ahead)</label>
                <input 
                  type="date" 
                  id="date" 
                  min={min}
                  max={max}
                  value={date}
                  onChange={(e) => handleDateChange(e.target.value)}
                  className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring [color-scheme:dark]"
                  required
                  suppressHydrationWarning
                />
              </div>

              <div className="flex flex-col gap-2">
                <label className="text-sm font-medium text-foreground">Time</label>
                {!serviceSlug || !staffSlug || !date ? (
                  <div className="h-11 flex items-center px-3 rounded-md border border-dashed border-white/20 bg-white/5 text-sm text-foreground/50">
                    Select service, stylist, and date first
                  </div>
                ) : loadingSlots ? (
                  <div className="h-11 flex items-center px-3 rounded-md border border-white/20 bg-white/5 text-sm text-foreground/70 animate-pulse">
                    Checking availability...
                  </div>
                ) : slotsError ? (
                  <div className="h-11 flex items-center px-3 rounded-md border border-red-500/20 bg-red-500/5 text-sm text-red-400">
                    {slotsError}
                  </div>
                ) : slots.length === 0 ? (
                  <div className="h-11 flex items-center px-3 rounded-md border border-white/20 bg-white/5 text-sm text-foreground/70">
                    No available times for this selection.
                  </div>
                ) : (
                  <div className="grid grid-cols-3 gap-2 max-h-48 overflow-y-auto pr-2 custom-scrollbar">
                    {slots.map(slot => (
                      <button
                        key={slot.startTime}
                        type="button"
                        onClick={() => setSelectedSlot(slot.startTime)}
                        className={`py-2 px-1 text-sm rounded-md border transition-colors ${
                          selectedSlot === slot.startTime
                            ? 'bg-accent text-accent-foreground border-accent'
                            : 'border-white/20 bg-white/5 hover:bg-white/10 text-foreground'
                        }`}
                      >
                        {formatTime(slot.startTime)}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Customer Details */}
            <div className="border-t border-white/10 pt-8 flex flex-col gap-6">
              <h3 className="text-lg font-semibold text-foreground">Your Details</h3>
              
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                <div className="flex flex-col gap-2">
                  <label htmlFor="name" className="text-sm font-medium text-foreground">Full Name</label>
                  <input 
                    type="text" 
                    id="name" 
                    value={customerName}
                    onChange={(e) => setCustomerName(e.target.value)}
                    required
                    disabled={submitting}
                    className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
                    placeholder="Jane Doe"
                  />
                </div>
                
                <div className="flex flex-col gap-2">
                  <label htmlFor="phone" className="text-sm font-medium text-foreground">Phone Number</label>
                  <input 
                    type="tel" 
                    id="phone" 
                    value={customerPhone}
                    onChange={(e) => setCustomerPhone(e.target.value)}
                    required
                    disabled={submitting}
                    className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
                    placeholder="+94 77 123 4567"
                  />
                </div>
              </div>

              <div className="flex flex-col gap-2">
                <label htmlFor="email" className="text-sm font-medium text-foreground">Email Address</label>
                <input 
                  type="email" 
                  id="email"
                  value={customerEmail}
                  onChange={(e) => setCustomerEmail(e.target.value)}
                  required
                  disabled={submitting}
                  className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
                  placeholder="jane@example.com"
                />
              </div>

              <div className="flex flex-col gap-2">
                <label htmlFor="notes" className="text-sm font-medium text-foreground">Optional Notes</label>
                <textarea 
                  id="notes" 
                  rows={3}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  disabled={submitting}
                  className="rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring resize-none disabled:opacity-50"
                  placeholder="Any special requests or allergies?"
                />
              </div>
            </div>

            {/* Actions */}
            <div className="pt-4 flex flex-col gap-4 items-center">
              <button 
                type="submit"
                disabled={!serviceSlug || !staffSlug || !date || !selectedSlot || !customerName || !customerPhone || !customerEmail || submitting}
                className="h-12 w-full sm:w-auto px-8 rounded-md bg-accent text-accent-foreground font-medium hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {submitting ? 'Confirming...' : 'Complete Booking'}
              </button>
            </div>

          </form>

        </div>
      </main>
    </div>
  );
}
