import Link from 'next/link';
import { staffMembers } from '@/lib/staff';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Our Team | Velvet Salon',
  description: 'Meet the talented professionals at Velvet Salon.',
};

export default function TeamPage() {
  return (
    <div className="flex flex-col flex-1 bg-background font-sans">
      <main className="flex-1 flex flex-col items-center py-20 px-4 sm:px-6 lg:px-8">
        <div className="w-full max-w-5xl mx-auto flex flex-col gap-16">
          
          {/* Page Header */}
          <div className="flex flex-col items-center gap-4 text-center">
            <h1 className="text-4xl sm:text-5xl font-bold tracking-tight text-foreground">
              Our Team
            </h1>
            <p className="max-w-2xl text-lg text-foreground/70">
              Meet the talented professionals dedicated to providing you with an exceptional salon experience.
            </p>
          </div>

          {/* Team Grid */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {staffMembers.map((staff) => (
              <div 
                key={staff.id}
                className="flex flex-col items-center text-center p-8 rounded-2xl border border-white/10 bg-white/5 transition-colors hover:bg-white/10"
              >
                <div className="h-28 w-28 rounded-full bg-accent/20 flex items-center justify-center mb-6">
                  <span className="text-4xl font-semibold text-accent">
                    {staff.name.charAt(0)}
                  </span>
                </div>
                <h2 className="text-2xl font-semibold text-foreground mb-1">
                  {staff.name}
                </h2>
                <p className="text-sm font-medium text-accent mb-4">
                  {staff.role}
                </p>
                <p className="text-sm text-foreground/60 mb-6">
                  Specialty: {staff.specialty}
                </p>
                <Link 
                  href="/booking" 
                  className="mt-auto h-10 px-6 inline-flex items-center justify-center rounded-md border border-white/20 bg-transparent text-sm font-medium text-foreground transition-colors hover:bg-white/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                >
                  Book with {staff.name}
                </Link>
              </div>
            ))}
          </div>

          {/* General Booking CTA */}
          <div className="flex justify-center pt-8 border-t border-white/10">
            <Link
              href="/booking"
              className="flex h-12 items-center justify-center rounded-md bg-accent px-10 text-base font-medium text-accent-foreground shadow transition-colors hover:bg-accent/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              Book Your Appointment
            </Link>
          </div>

        </div>
      </main>
    </div>
  );
}
