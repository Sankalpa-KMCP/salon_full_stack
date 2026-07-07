"use client";

import { featuredServices } from '@/lib/services';
import { staffMembers } from '@/lib/staff';

export default function BookingClient() {
  const getDates = () => {
    // Return empty during SSR to avoid hydration mismatch, or just compute it.
    // For a mock form, computing synchronously is perfectly fine.
    const today = new Date();
    const max = new Date();
    max.setDate(today.getDate() + 30);
    return {
      min: today.toISOString().split('T')[0],
      max: max.toISOString().split('T')[0]
    };
  };

  const { min, max } = getDates();

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

          <form className="flex flex-col gap-8 bg-white/5 border border-white/10 rounded-2xl p-6 sm:p-10" onSubmit={(e) => e.preventDefault()}>
            
            {/* Service & Stylist */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              <div className="flex flex-col gap-2">
                <label htmlFor="service" className="text-sm font-medium text-foreground">Service</label>
                <select 
                  id="service" 
                  className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  required
                >
                  <option value="">Select a service...</option>
                  {featuredServices.map(service => (
                    <option key={service.id} value={service.id}>
                      {service.name} ({service.durationMinutes} min, {service.priceFormatted})
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex flex-col gap-2">
                <label htmlFor="stylist" className="text-sm font-medium text-foreground">Stylist</label>
                <select 
                  id="stylist" 
                  className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  required
                >
                  <option value="any">Any stylist</option>
                  {staffMembers.map(staff => (
                    <option key={staff.id} value={staff.id}>
                      {staff.name} - {staff.role}
                    </option>
                  ))}
                </select>
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
                  className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring [color-scheme:dark]"
                  required
                  suppressHydrationWarning
                />
              </div>

              <div className="flex flex-col gap-2">
                <label className="text-sm font-medium text-foreground">Time</label>
                <div className="h-11 flex items-center px-3 rounded-md border border-dashed border-white/20 bg-white/5 text-sm text-foreground/60">
                  Real availability slots will connect to the backend API here.
                </div>
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
                    className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                    placeholder="Jane Doe"
                  />
                </div>
                
                <div className="flex flex-col gap-2">
                  <label htmlFor="phone" className="text-sm font-medium text-foreground">Phone Number</label>
                  <input 
                    type="tel" 
                    id="phone" 
                    className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                    placeholder="+94 77 123 4567"
                  />
                </div>
              </div>

              <div className="flex flex-col gap-2">
                <label htmlFor="email" className="text-sm font-medium text-foreground">Email Address</label>
                <input 
                  type="email" 
                  id="email" 
                  className="h-11 rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  placeholder="jane@example.com"
                />
              </div>

              <div className="flex flex-col gap-2">
                <label htmlFor="notes" className="text-sm font-medium text-foreground">Optional Notes</label>
                <textarea 
                  id="notes" 
                  rows={3}
                  className="rounded-md border border-white/20 bg-background px-3 py-2 text-sm text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring resize-none"
                  placeholder="Any special requests or allergies?"
                />
              </div>
            </div>

            {/* Actions */}
            <div className="pt-4 flex flex-col gap-4 items-center">
              <button 
                type="button"
                disabled
                className="h-12 w-full sm:w-auto px-8 rounded-md bg-accent/50 text-accent-foreground font-medium cursor-not-allowed opacity-80"
              >
                Complete Booking (Coming Soon)
              </button>
              <p className="text-xs text-foreground/50 text-center max-w-sm">
                Booking submissions are currently disabled. Real appointment persistence and confirmation will be added during the backend integration phase.
              </p>
            </div>

          </form>

        </div>
      </main>
    </div>
  );
}
