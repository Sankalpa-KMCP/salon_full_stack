"use client";

import { useState, useEffect, FormEvent } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import { apiClient, BookingResponseDto, ServiceDto, ApiError, API_BASE_URL } from '@/lib/api-client';

export default function ManageClient() {
  const searchParams = useSearchParams();
  const initialToken = searchParams.get('token') || '';

  const configError = API_BASE_URL ? null : "The application is missing a required configuration (NEXT_PUBLIC_API_BASE_URL). Please check the environment variables.";
  const [tokenInput, setTokenInput] = useState(initialToken);

  const [loading, setLoading] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [services, setServices] = useState<ServiceDto[]>([]);
  const [appointment, setAppointment] = useState<BookingResponseDto | null>(null);

  const handleLookup = async (tokenToLookup: string) => {
    const cleanToken = tokenToLookup.trim();
    if (!cleanToken) return;

    setLoading(true);
    setError(null);
    setAppointment(null);

    try {
      const data = await apiClient.getBooking(cleanToken);
      setAppointment(data);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        if (err.status === 404 || err.status === 400) {
          setError("Appointment not found or invalid token.");
        } else {
          setError(err.message);
        }
      } else {
        setError("A network error occurred while looking up your appointment.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (configError) return;

    // Fetch services to resolve names
    apiClient.getServices()
      .then(setServices)
      .catch(err => console.error('Failed to load services:', err));

    if (initialToken) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      handleLookup(initialToken);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);


  const onSubmit = (e: FormEvent) => {
    e.preventDefault();
    handleLookup(tokenInput);
  };

  const handleCancel = async () => {
    if (!appointment) return;

    if (!window.confirm("Are you sure you want to cancel this appointment?")) {
      return;
    }

    setCancelling(true);
    setError(null);

    try {
      await apiClient.cancelBooking(appointment.cancellationToken);
      // Update local status
      setAppointment({ ...appointment, status: 'CANCELLED' });
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        if (err.status === 400) {
          // Backend business error (e.g., within 24 hours)
          setError(err.message || "This appointment can no longer be cancelled online.");
        } else if (err.status === 404) {
          setError("Appointment not found or invalid token.");
        } else {
          setError(err.message);
        }
      } else {
        setError("A network error occurred while cancelling your appointment.");
      }
    } finally {
      setCancelling(false);
    }
  };

  const formatTime = (isoString: string) => {
    return new Intl.DateTimeFormat('en-US', {
      timeZone: 'Asia/Colombo',
      hour: 'numeric',
      minute: '2-digit'
    }).format(new Date(isoString));
  };

  const formatDate = (isoString: string) => {
    return new Intl.DateTimeFormat('en-US', {
      timeZone: 'Asia/Colombo',
      weekday: 'long',
      month: 'long',
      day: 'numeric',
      year: 'numeric'
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

  // Resolve service name safely
  const resolvedServiceName = appointment
    ? (services.find(s => s.slug === appointment.serviceSlug)?.name || appointment.serviceSlug)
    : '';

  return (
    <div className="flex flex-col flex-1 bg-black font-sans relative min-h-screen">
      {/* Cinematic Background */}
      <div className="fixed inset-0 z-0 pointer-events-none">
        <Image src="/media/hero-ambience.webp" alt="Velvet Salon Ambience" fill className="object-cover opacity-25 mix-blend-luminosity" priority />
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/90 to-background/50" />
      </div>

      <main className="relative z-10 flex-1 flex flex-col items-center justify-center py-20 px-4 sm:px-6 lg:px-8 w-full max-w-4xl mx-auto">
        <div className="w-full flex flex-col gap-12 items-center">

          <div className="flex flex-col items-center gap-6 text-center max-w-xl">
            <span className="text-accent/60 font-light tracking-[0.3em] uppercase text-xs">Client Portal</span>
            <h1 className="text-4xl sm:text-5xl font-display text-foreground leading-tight">
              Manage Your<br />Reservation.
            </h1>
            <p className="text-foreground/60 font-light text-sm leading-relaxed">
              Enter your secure cancellation token below to retrieve your itinerary or amend your appointment.
            </p>
          </div>

          <form onSubmit={onSubmit} className="flex flex-col sm:flex-row gap-4 w-full max-w-lg mx-auto relative group">
            <div className="relative flex-1">
              <input
                type="text"
                value={tokenInput}
                onChange={(e) => setTokenInput(e.target.value)}
                placeholder="Cancellation Token"
                className="w-full h-14 bg-white/5 border border-white/20 px-6 py-2 text-sm tracking-widest text-foreground font-light focus:outline-none focus:border-accent disabled:opacity-50 transition-colors backdrop-blur-sm placeholder:text-foreground/30 text-center sm:text-left"
                required
              />
            </div>
            <button
              type="submit"
              disabled={loading || !tokenInput.trim()}
              className="h-14 px-8 bg-accent text-accent-foreground text-xs uppercase tracking-widest font-medium hover:bg-white transition-all duration-300 disabled:opacity-30 disabled:cursor-not-allowed flex items-center justify-center min-w-[140px]"
            >
              {loading ? (
                <span className="w-4 h-4 border-2 border-background/20 border-t-background rounded-full animate-spin" />
              ) : 'Retrieve'}
            </button>
          </form>

          {error && (
            <div className="w-full max-w-lg p-6 bg-red-500/5 border-l-2 border-red-500/50 text-red-400 text-sm font-light text-center backdrop-blur-sm">
              {error}
            </div>
          )}

          {appointment && !loading && (
            <div className="w-full max-w-2xl bg-white/5 border border-white/10 p-8 sm:p-12 flex flex-col items-center text-center gap-8 backdrop-blur-md shadow-2xl relative overflow-hidden mt-4">
              {/* Receipt styling overlay */}
              <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-accent/50 to-transparent opacity-50" />

              <div className="flex flex-col items-center gap-4 w-full">
                <div className={`px-4 py-1.5 text-[10px] uppercase tracking-widest font-medium border ${
                  appointment.status === 'CANCELLED'
                    ? 'bg-red-500/10 text-red-400 border-red-500/20'
                    : 'bg-green-500/10 text-green-400 border-green-500/20'
                }`}>
                  {appointment.status}
                </div>

                <h2 className="text-3xl font-display text-foreground mt-4">
                  {formatDate(appointment.startTime)}
                </h2>
                <div className="w-12 h-[1px] bg-accent/40 my-2" />
              </div>

              <div className="w-full bg-black/40 border border-white/5 p-8 flex flex-col gap-6 relative">
                <div className="grid grid-cols-2 gap-y-6 text-sm font-light">
                  <div className="text-foreground/40 uppercase tracking-widest text-xs text-left">Service</div>
                  <div className="text-foreground text-right">{resolvedServiceName}</div>

                  <div className="text-foreground/40 uppercase tracking-widest text-xs text-left">Curator</div>
                  <div className="text-foreground text-right">{appointment.staffName}</div>

                  <div className="text-foreground/40 uppercase tracking-widest text-xs text-left">Duration</div>
                  <div className="text-foreground text-right">
                    {formatTime(appointment.startTime)} &mdash; {formatTime(appointment.endTime)}
                  </div>
                </div>
              </div>

              {appointment.status !== 'CANCELLED' ? (
                <div className="w-full flex flex-col gap-6 mt-4">
                  <p className="text-xs text-foreground/50 font-light italic">
                    Amendments or cancellations must be finalized at least 24 hours prior to your scheduled arrival.
                  </p>
                  <button
                    onClick={handleCancel}
                    disabled={cancelling}
                    className="w-full py-5 bg-red-950/30 hover:bg-red-900/40 text-red-400 border border-red-900/30 text-xs uppercase tracking-widest font-medium transition-all duration-300 disabled:opacity-50 flex items-center justify-center gap-3"
                  >
                    {cancelling ? (
                      <>
                        <span className="w-4 h-4 border-2 border-red-400/20 border-t-red-400 rounded-full animate-spin" />
                        Processing...
                      </>
                    ) : 'Cancel Reservation'}
                  </button>
                </div>
              ) : (
                <div className="w-full p-6 bg-background/40 border border-white/5 text-foreground/60 text-sm font-light mt-4">
                  This reservation has been successfully annulled.
                </div>
              )}
            </div>
          )}

          <div className="flex justify-center pt-12">
            <Link
              href="/booking"
              className="text-xs uppercase tracking-widest text-accent/70 hover:text-accent transition-colors flex items-center gap-2"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
              Return to Booking
            </Link>
          </div>

        </div>
      </main>
    </div>
  );
}
