import Link from 'next/link';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Contact Us | Velvet Salon',
  description: 'Get in touch with Velvet Salon for inquiries, or book your appointment online.',
};

export default function ContactPage() {
  return (
    <div className="flex flex-col flex-1 bg-background font-sans">
      <main className="flex-1 flex flex-col items-center py-20 px-4 sm:px-6 lg:px-8">
        <div className="w-full max-w-4xl mx-auto flex flex-col gap-16">
          
          {/* Page Header */}
          <div className="flex flex-col items-center gap-4 text-center">
            <h1 className="text-4xl sm:text-5xl font-bold tracking-tight text-foreground">
              Contact Us
            </h1>
            <p className="max-w-2xl text-lg text-foreground/70">
              We look forward to welcoming you to Velvet Salon. Find our opening hours below, or securely book your next visit online.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            {/* Opening Hours */}
            <div className="flex flex-col gap-6 p-8 rounded-2xl border border-white/10 bg-white/5">
              <h2 className="text-2xl font-semibold text-foreground">
                Opening Hours
              </h2>
              <ul className="flex flex-col gap-3 text-foreground/80">
                <li className="flex justify-between border-b border-white/5 pb-2">
                  <span>Tuesday &ndash; Sunday</span>
                  <span className="font-medium text-foreground">09:00 AM &ndash; 07:00 PM</span>
                </li>
                <li className="flex justify-between border-b border-white/5 pb-2 text-foreground/50">
                  <span>Monday</span>
                  <span>Closed</span>
                </li>
              </ul>
            </div>

            {/* Pending Contact Details */}
            <div className="flex flex-col gap-6 p-8 rounded-2xl border border-white/10 bg-white/5">
              <h2 className="text-2xl font-semibold text-foreground">
                Location & Information
              </h2>
              <div className="flex-1 flex flex-col items-center justify-center text-center gap-2 text-foreground/60 p-6 border border-dashed border-white/20 rounded-xl bg-background/50">
                <span className="text-sm font-medium uppercase tracking-widest text-accent">Pending</span>
                <p className="text-sm">
                  Our official address, phone number, and social profiles will be published soon.
                </p>
              </div>
            </div>
          </div>

          {/* Booking Callout */}
          <div className="flex flex-col items-center text-center p-8 rounded-2xl border border-accent/20 bg-accent/5 gap-6">
            <h2 className="text-2xl font-semibold text-foreground">
              Ready to visit?
            </h2>
            <p className="max-w-md text-foreground/70">
              Skip the wait and reserve your premium hair and beauty experience directly through our online booking system.
            </p>
            <Link
              href="/booking"
              className="mt-2 inline-flex h-12 items-center justify-center rounded-md bg-accent px-10 text-base font-medium text-accent-foreground shadow transition-colors hover:bg-accent/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              Book Online Now
            </Link>
          </div>

        </div>
      </main>
    </div>
  );
}
