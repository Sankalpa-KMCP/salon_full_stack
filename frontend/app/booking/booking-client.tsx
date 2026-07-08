"use client";

import { useEffect, useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
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
      <div className="flex flex-col flex-1 bg-black font-sans items-center justify-center p-8 text-center relative">
        <div className="absolute inset-0 z-0">
          <Image src="/media/hero-ambience.webp" alt="Background" fill className="object-cover opacity-30 mix-blend-luminosity" />
          <div className="absolute inset-0 bg-gradient-to-b from-background/80 to-background" />
        </div>
        <div className="relative z-10 p-10 bg-white/5 border border-white/10 rounded-sm max-w-lg backdrop-blur-sm">
          <h2 className="text-2xl font-display text-accent mb-4 uppercase tracking-widest text-red-400">System Unavailable</h2>
          <p className="text-foreground/70 font-light text-sm">{configError}</p>
        </div>
      </div>
    );
  }

  if (successData) {
    return (
      <div className="flex flex-col flex-1 bg-black font-sans items-center justify-center py-20 px-4 relative min-h-screen">
        <div className="absolute inset-0 z-0">
          <Image src="/media/service-spa-finish.webp" alt="Background" fill className="object-cover opacity-20 mix-blend-luminosity" />
          <div className="absolute inset-0 bg-gradient-to-t from-background via-background/90 to-transparent" />
        </div>

        <div className="relative z-10 w-full max-w-2xl bg-white/5 border border-white/10 rounded-sm p-10 sm:p-14 text-center flex flex-col items-center gap-8 backdrop-blur-md shadow-2xl">
          <div className="flex flex-col items-center">
            <span className="text-accent/60 font-light tracking-[0.3em] uppercase text-xs mb-4">Reservation Confirmed</span>
            <h2 className="text-4xl sm:text-5xl font-display text-foreground mb-2">Thank You, {customerName || 'Guest'}.</h2>
            <div className="w-12 h-[1px] bg-accent/40 my-4" />
            <p className="text-foreground/60 font-light text-sm sm:text-base">Your appointment at Velvet Salon has been secured.</p>
          </div>

          <div className="w-full bg-black/40 border border-white/5 p-8 flex flex-col gap-6 relative overflow-hidden">
            <div className="absolute top-0 left-0 w-full h-[1px] bg-gradient-to-r from-transparent via-accent/30 to-transparent" />
            <div className="grid grid-cols-2 gap-y-6 text-sm font-light">
              <div className="text-foreground/40 uppercase tracking-widest text-xs text-left">Service</div>
              <div className="text-foreground text-right">{successData.serviceName}</div>

              <div className="text-foreground/40 uppercase tracking-widest text-xs text-left">Curator</div>
              <div className="text-foreground text-right">{successData.booking.staffName}</div>

              <div className="text-foreground/40 uppercase tracking-widest text-xs text-left">Reserved Time</div>
              <div className="text-foreground text-right">{formatTime(successData.booking.startTime)}</div>
            </div>
            <div className="absolute bottom-0 left-0 w-full h-[1px] bg-gradient-to-r from-transparent via-accent/30 to-transparent" />
          </div>

          <div className="w-full bg-accent/5 border border-accent/20 p-6 flex flex-col gap-4 relative">
            <h3 className="text-accent font-display text-lg">Cancellation Token</h3>
            <p className="text-xs text-foreground/50 font-light">
              Please retain this secure token should you need to amend your reservation.
            </p>
            <div className="flex items-center gap-2 mt-2">
              <code className="flex-1 bg-black/60 text-accent/80 p-4 text-xs tracking-widest truncate text-left border border-white/5">
                {successData.booking.cancellationToken}
              </code>
              <button
                onClick={copyToken}
                className="px-6 py-4 bg-accent text-accent-foreground text-xs uppercase tracking-widest font-medium transition-all hover:bg-white focus:outline-none"
              >
                {copied ? 'Copied' : 'Copy'}
              </button>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row gap-4 w-full mt-4">
            <button
              onClick={() => window.location.reload()}
              className="flex-1 px-6 py-4 bg-white/5 hover:bg-white/10 border border-white/10 text-xs uppercase tracking-widest transition-colors font-medium"
            >
              New Reservation
            </button>
            <Link
              href={`/booking/manage?token=${encodeURIComponent(successData.booking.cancellationToken)}`}
              className="flex-1 px-6 py-4 bg-white/5 hover:bg-white/10 border border-white/10 text-xs uppercase tracking-widest transition-colors font-medium text-center"
            >
              Manage Booking
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col flex-1 bg-black font-sans relative min-h-screen">
      {/* Cinematic Background */}
      <div className="fixed inset-0 z-0 pointer-events-none">
        <Image src="/media/hero-ambience.webp" alt="Velvet Salon Ambience" fill className="object-cover opacity-30 mix-blend-luminosity" priority />
        <div className="absolute inset-0 bg-gradient-to-r from-background via-background/95 to-background/80" />
        <div className="absolute inset-0 bg-gradient-to-b from-background/50 via-transparent to-background" />
      </div>

      <main className="relative z-10 flex-1 flex flex-col lg:flex-row w-full max-w-[1400px] mx-auto">
        {/* Left Panel: Atelier Summary (Sticky on Desktop) */}
        <div className="w-full lg:w-1/3 lg:sticky lg:top-0 lg:h-screen flex flex-col justify-center p-8 lg:p-16 border-b lg:border-b-0 lg:border-r border-white/5 bg-background/40 backdrop-blur-md">
          <div className="flex flex-col gap-6 max-w-sm">
            <span className="text-accent/60 font-light tracking-[0.3em] uppercase text-xs">The Atelier</span>
            <h1 className="text-4xl sm:text-5xl font-display text-foreground leading-tight">
              Reserve<br />Your Time.
            </h1>
            <p className="text-foreground/60 font-light text-sm leading-relaxed mb-8">
              Curate your experience. Select your preferred treatments and our artisans will ensure a transcendent visit.
            </p>

            {/* Dynamic Summary */}
            <div className="flex flex-col gap-4 border-l border-accent/30 pl-6 py-2">
              <div className="flex flex-col">
                <span className="text-[10px] uppercase tracking-widest text-foreground/40 mb-1">Service</span>
                <span className="text-sm text-foreground/90 font-light">
                  {serviceSlug ? services.find(s => s.slug === serviceSlug)?.name : 'Not selected'}
                </span>
              </div>
              <div className="flex flex-col">
                <span className="text-[10px] uppercase tracking-widest text-foreground/40 mb-1">Curator</span>
                <span className="text-sm text-foreground/90 font-light">
                  {staffSlug ? (staffSlug === 'any' ? 'Any Available Artisan' : staff.find(s => s.slug === staffSlug)?.name) : 'Not selected'}
                </span>
              </div>
              <div className="flex flex-col">
                <span className="text-[10px] uppercase tracking-widest text-foreground/40 mb-1">Date & Time</span>
                <span className="text-sm text-foreground/90 font-light">
                  {date && selectedSlot ? `${new Date(date).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric'})} at ${formatTime(selectedSlot)}` : 'Not selected'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Right Panel: The Form */}
        <div className="w-full lg:w-2/3 p-6 sm:p-12 lg:p-24 flex flex-col justify-center min-h-[80vh]">
          <form className="w-full max-w-2xl mx-auto flex flex-col gap-16" onSubmit={handleSubmit}>

            {submitError && (
              <div className="bg-red-500/5 border-l-2 border-red-500/50 text-red-400 p-6 text-sm font-light backdrop-blur-sm">
                {submitError}
              </div>
            )}

            {/* Section 1: Curation */}
            <div className="flex flex-col gap-8">
              <div className="flex items-center gap-4">
                <span className="text-accent/40 font-display italic text-xl">01</span>
                <h3 className="text-sm uppercase tracking-[0.2em] text-foreground/80 font-light">Curation</h3>
                <div className="flex-1 h-[1px] bg-white/5" />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="flex flex-col gap-3 group">
                  <label htmlFor="service" className="text-xs uppercase tracking-widest text-foreground/50 group-focus-within:text-accent transition-colors">Service</label>
                  <div className="relative">
                    <select
                      id="service"
                      value={serviceSlug}
                      onChange={(e) => handleServiceChange(e.target.value)}
                      disabled={loadingServices}
                      className="w-full h-14 bg-transparent border-b border-white/20 px-0 py-2 text-base text-foreground font-light focus:outline-none focus:border-accent disabled:opacity-50 appearance-none rounded-none cursor-pointer transition-colors"
                      required
                    >
                      <option value="" className="bg-background text-foreground/50">
                        {loadingServices ? 'Loading services...' : 'Select a service...'}
                      </option>
                      {services.map(service => (
                        <option key={service.slug} value={service.slug} className="bg-background text-foreground">
                          {service.name} &mdash; {formatPrice(service.price)}
                        </option>
                      ))}
                    </select>
                    <div className="absolute right-0 top-1/2 -translate-y-1/2 pointer-events-none text-accent/50 group-focus-within:text-accent transition-colors">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M6 9l6 6 6-6"/></svg>
                    </div>
                  </div>
                  {servicesError && <span className="text-[10px] text-red-400 tracking-wider uppercase">{servicesError}</span>}
                </div>

                <div className="flex flex-col gap-3 group">
                  <label htmlFor="stylist" className="text-xs uppercase tracking-widest text-foreground/50 group-focus-within:text-accent transition-colors">Artisan</label>
                  <div className="relative">
                    <select
                      id="stylist"
                      value={staffSlug}
                      onChange={(e) => handleStaffChange(e.target.value)}
                      disabled={!serviceSlug || loadingStaff}
                      className="w-full h-14 bg-transparent border-b border-white/20 px-0 py-2 text-base text-foreground font-light focus:outline-none focus:border-accent disabled:opacity-50 appearance-none rounded-none cursor-pointer transition-colors"
                      required
                    >
                      <option value="" className="bg-background text-foreground/50">
                        {!serviceSlug ? 'Select a service first' : loadingStaff ? 'Loading artisans...' : 'Select a curator...'}
                      </option>
                      {serviceSlug && !loadingStaff && <option value="any" className="bg-background text-foreground">Any Available Artisan</option>}
                      {staff.map(member => (
                        <option key={member.slug} value={member.slug} className="bg-background text-foreground">
                          {member.name}
                        </option>
                      ))}
                    </select>
                    <div className="absolute right-0 top-1/2 -translate-y-1/2 pointer-events-none text-accent/50 group-focus-within:text-accent transition-colors">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M6 9l6 6 6-6"/></svg>
                    </div>
                  </div>
                  {staffError && <span className="text-[10px] text-red-400 tracking-wider uppercase">{staffError}</span>}
                </div>
              </div>
            </div>

            {/* Section 2: Horizon */}
            <div className="flex flex-col gap-8">
              <div className="flex items-center gap-4">
                <span className="text-accent/40 font-display italic text-xl">02</span>
                <h3 className="text-sm uppercase tracking-[0.2em] text-foreground/80 font-light">Horizon</h3>
                <div className="flex-1 h-[1px] bg-white/5" />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="flex flex-col gap-3 group">
                  <label htmlFor="date" className="text-xs uppercase tracking-widest text-foreground/50 group-focus-within:text-accent transition-colors">Date</label>
                  <input
                    type="date"
                    id="date"
                    min={min}
                    max={max}
                    value={date}
                    onChange={(e) => handleDateChange(e.target.value)}
                    className="w-full h-14 bg-transparent border-b border-white/20 px-0 py-2 text-base text-foreground font-light focus:outline-none focus:border-accent [color-scheme:dark] rounded-none transition-colors"
                    required
                    suppressHydrationWarning
                  />
                </div>

                <div className="flex flex-col gap-3">
                  <label className="text-xs uppercase tracking-widest text-foreground/50">Time Slot</label>
                  {!serviceSlug || !staffSlug || !date ? (
                    <div className="h-14 flex items-center border-b border-white/5 text-sm font-light text-foreground/30 italic">
                      Pending curation...
                    </div>
                  ) : loadingSlots ? (
                    <div className="h-14 flex items-center border-b border-white/5 text-sm font-light text-foreground/50 animate-pulse">
                      Consulting agenda...
                    </div>
                  ) : slotsError ? (
                    <div className="h-14 flex items-center border-b border-red-500/20 text-sm font-light text-red-400">
                      {slotsError}
                    </div>
                  ) : slots.length === 0 ? (
                    <div className="h-14 flex items-center border-b border-white/5 text-sm font-light text-foreground/50">
                      No availability on this date.
                    </div>
                  ) : (
                    <div className="grid grid-cols-3 gap-3 max-h-56 overflow-y-auto pr-2 custom-scrollbar mt-2">
                      {slots.map(slot => (
                        <button
                          key={slot.startTime}
                          type="button"
                          onClick={() => setSelectedSlot(slot.startTime)}
                          className={`py-3 px-2 text-xs font-light tracking-wider rounded-sm border transition-all duration-300 ${
                            selectedSlot === slot.startTime
                              ? 'bg-accent/10 border-accent text-accent shadow-[0_0_15px_rgba(212,175,55,0.1)]'
                              : 'border-white/10 bg-white/5 hover:border-white/30 text-foreground/80 hover:text-foreground'
                          }`}
                        >
                          {formatTime(slot.startTime)}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Section 3: Identity */}
            <div className="flex flex-col gap-8">
              <div className="flex items-center gap-4">
                <span className="text-accent/40 font-display italic text-xl">03</span>
                <h3 className="text-sm uppercase tracking-[0.2em] text-foreground/80 font-light">Identity</h3>
                <div className="flex-1 h-[1px] bg-white/5" />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="flex flex-col gap-3 group">
                  <label htmlFor="name" className="text-xs uppercase tracking-widest text-foreground/50 group-focus-within:text-accent transition-colors">Guest Name</label>
                  <input
                    type="text"
                    id="name"
                    value={customerName}
                    onChange={(e) => setCustomerName(e.target.value)}
                    required
                    disabled={submitting}
                    className="w-full h-14 bg-transparent border-b border-white/20 px-0 py-2 text-base text-foreground font-light focus:outline-none focus:border-accent disabled:opacity-50 placeholder:text-foreground/20 rounded-none transition-colors"
                    placeholder="Enter your full name"
                  />
                </div>

                <div className="flex flex-col gap-3 group">
                  <label htmlFor="phone" className="text-xs uppercase tracking-widest text-foreground/50 group-focus-within:text-accent transition-colors">Contact Number</label>
                  <input
                    type="tel"
                    id="phone"
                    value={customerPhone}
                    onChange={(e) => setCustomerPhone(e.target.value)}
                    required
                    disabled={submitting}
                    className="w-full h-14 bg-transparent border-b border-white/20 px-0 py-2 text-base text-foreground font-light focus:outline-none focus:border-accent disabled:opacity-50 placeholder:text-foreground/20 rounded-none transition-colors"
                    placeholder="+1 (555) 000-0000"
                  />
                </div>
              </div>

              <div className="flex flex-col gap-3 group">
                <label htmlFor="email" className="text-xs uppercase tracking-widest text-foreground/50 group-focus-within:text-accent transition-colors">Email Address</label>
                <input
                  type="email"
                  id="email"
                  value={customerEmail}
                  onChange={(e) => setCustomerEmail(e.target.value)}
                  required
                  disabled={submitting}
                  className="w-full h-14 bg-transparent border-b border-white/20 px-0 py-2 text-base text-foreground font-light focus:outline-none focus:border-accent disabled:opacity-50 placeholder:text-foreground/20 rounded-none transition-colors"
                  placeholder="For reservation confirmation"
                />
              </div>

              <div className="flex flex-col gap-3 group">
                <label htmlFor="notes" className="text-xs uppercase tracking-widest text-foreground/50 group-focus-within:text-accent transition-colors">Special Requests (Optional)</label>
                <textarea
                  id="notes"
                  rows={1}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  disabled={submitting}
                  className="w-full bg-transparent border-b border-white/20 px-0 py-2 text-base text-foreground font-light focus:outline-none focus:border-accent resize-none disabled:opacity-50 placeholder:text-foreground/20 rounded-none transition-colors min-h-[56px]"
                  placeholder="Allergies, sensitivities, or preferences..."
                />
              </div>
            </div>

            {/* Action */}
            <div className="pt-8">
              <button
                type="submit"
                disabled={!serviceSlug || !staffSlug || !date || !selectedSlot || !customerName || !customerPhone || !customerEmail || submitting}
                className="w-full sm:w-auto px-12 py-5 bg-accent text-accent-foreground text-xs font-medium uppercase tracking-[0.2em] hover:bg-white transition-all duration-500 disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:bg-accent flex items-center justify-center gap-4"
              >
                {submitting ? (
                  <>
                    <span className="w-4 h-4 border-2 border-background/20 border-t-background rounded-full animate-spin" />
                    Finalizing
                  </>
                ) : 'Secure Reservation'}
              </button>
            </div>

          </form>
        </div>
      </main>
    </div>
  );
}
