"use client";

import { useState, useEffect, FormEvent } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
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
      <div className="flex flex-col items-center justify-center flex-1 bg-background p-4 font-sans">
        <div className="max-w-md p-6 bg-red-500/10 border border-red-500/20 rounded-xl text-center">
          <h2 className="text-lg font-semibold text-red-500 mb-2">Configuration Error</h2>
          <p className="text-sm text-red-400">{configError}</p>
        </div>
      </div>
    );
  }

  // Resolve service name safely
  const resolvedServiceName = appointment
    ? (services.find(s => s.slug === appointment.serviceSlug)?.name || appointment.serviceSlug)
    : '';

  return (
    <div className="flex flex-col flex-1 bg-background font-sans">
      <main className="flex-1 flex flex-col items-center py-20 px-4 sm:px-6 lg:px-8">
        <div className="w-full max-w-2xl mx-auto flex flex-col gap-10">

          <div className="flex flex-col items-center gap-4 text-center">
            <h1 className="text-4xl sm:text-5xl font-bold tracking-tight text-foreground">
              Manage Appointment
            </h1>
            <p className="text-lg text-foreground/60 max-w-xl">
              Enter your cancellation token below to retrieve or cancel your appointment.
            </p>
          </div>

          <form onSubmit={onSubmit} className="flex gap-2 w-full max-w-md mx-auto">
            <input
              type="text"
              value={tokenInput}
              onChange={(e) => setTokenInput(e.target.value)}
              placeholder="Cancellation Token"
              className="flex-1 h-12 rounded-md border border-white/20 bg-background px-4 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              required
            />
            <button
              type="submit"
              disabled={loading || !tokenInput.trim()}
              className="h-12 px-6 rounded-md bg-accent text-accent-foreground font-medium hover:opacity-90 transition-opacity disabled:opacity-50"
            >
              {loading ? 'Looking up...' : 'Lookup'}
            </button>
          </form>

          {error && (
            <div className="w-full p-4 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-sm text-center">
              {error}
            </div>
          )}

          {appointment && !loading && (
            <div className="w-full bg-white/5 border border-white/10 rounded-2xl p-8 flex flex-col items-center text-center gap-6 mt-4">
              <div className="flex flex-col items-center gap-2">
                <div className={`px-4 py-1 rounded-full text-xs font-semibold ${
                  appointment.status === 'CANCELLED'
                    ? 'bg-red-500/20 text-red-400 border border-red-500/30'
                    : 'bg-green-500/20 text-green-400 border border-green-500/30'
                }`}>
                  {appointment.status}
                </div>
                <h2 className="text-2xl font-semibold text-foreground mt-2">
                  {formatDate(appointment.startTime)}
                </h2>
              </div>

              <div className="w-full bg-black/20 rounded-xl p-6 text-left border border-white/5">
                <div className="grid grid-cols-2 gap-y-4 text-sm">
                  <div className="text-foreground/50">Service</div>
                  <div className="font-medium text-foreground text-right">{resolvedServiceName}</div>

                  <div className="text-foreground/50">Stylist</div>
                  <div className="font-medium text-foreground text-right">{appointment.staffName}</div>

                  <div className="text-foreground/50">Time</div>
                  <div className="font-medium text-foreground text-right">
                    {formatTime(appointment.startTime)} - {formatTime(appointment.endTime)}
                  </div>
                </div>
              </div>

              {appointment.status !== 'CANCELLED' ? (
                <div className="w-full flex flex-col gap-4 mt-2">
                  <button
                    onClick={handleCancel}
                    disabled={cancelling}
                    className="w-full py-4 bg-red-500/20 hover:bg-red-500/30 text-red-500 rounded-xl font-medium transition-colors border border-red-500/20 disabled:opacity-50"
                  >
                    {cancelling ? 'Cancelling...' : 'Cancel Appointment'}
                  </button>
                  <p className="text-xs text-foreground/50">
                    Cancellations must be made at least 24 hours in advance.
                  </p>
                </div>
              ) : (
                <div className="w-full p-4 bg-green-500/10 border border-green-500/20 rounded-lg text-green-400 text-sm">
                  Your appointment has been successfully cancelled.
                </div>
              )}
            </div>
          )}

          <div className="flex justify-center pt-8">
            <Link
              href="/booking"
              className="text-sm text-foreground/60 hover:text-foreground underline underline-offset-4"
            >
              Return to Booking
            </Link>
          </div>

        </div>
      </main>
    </div>
  );
}
