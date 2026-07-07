import Link from 'next/link';
import { featuredServices } from '@/lib/services';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Services | Velvet Salon',
  description: 'Explore our premium hair and beauty services.',
};

export default function ServicesPage() {
  return (
    <div className="flex flex-col flex-1 bg-background font-sans">
      <main className="flex-1 flex flex-col items-center py-20 px-4 sm:px-6 lg:px-8">
        <div className="w-full max-w-5xl mx-auto flex flex-col gap-12">
          
          {/* Page Header */}
          <div className="flex flex-col items-center gap-4 text-center">
            <h1 className="text-4xl sm:text-5xl font-bold tracking-tight text-foreground">
              Our Services
            </h1>
            <p className="max-w-2xl text-lg text-foreground/70">
              Discover our comprehensive range of premium hair styling, coloring, and beauty treatments.
            </p>
          </div>

          {/* Services Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {featuredServices.map((service) => (
              <div 
                key={service.id}
                className="flex flex-col justify-between p-6 rounded-2xl border border-white/10 bg-white/5 hover:bg-white/10 transition-colors"
              >
                <div className="flex flex-col gap-2">
                  <h2 className="text-xl font-semibold text-foreground">
                    {service.name}
                  </h2>
                  <div className="flex items-center gap-4 text-sm text-foreground/60">
                    <span>{service.durationMinutes} minutes</span>
                  </div>
                </div>
                <div className="mt-6 flex items-center justify-between">
                  <span className="text-lg font-medium text-accent">
                    {service.priceFormatted}
                  </span>
                  <Link 
                    href="/booking" 
                    className="text-sm font-medium text-foreground hover:text-accent transition-colors"
                    aria-label={`Book ${service.name}`}
                  >
                    Book →
                  </Link>
                </div>
              </div>
            ))}
          </div>

        </div>
      </main>
    </div>
  );
}
