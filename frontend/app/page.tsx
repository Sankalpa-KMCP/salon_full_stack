import Link from 'next/link';
import { featuredServices } from '@/lib/services';
import { staffMembers } from '@/lib/staff';

export default function Home() {
  return (
    <div className="flex flex-col flex-1 bg-background font-sans">
      <main className="flex-1 flex flex-col items-center">
        
        {/* Hero Section */}
        <section className="w-full flex flex-col items-center justify-center py-20 px-4 sm:px-6 lg:px-8">
          <div className="w-full max-w-4xl mx-auto flex flex-col items-center text-center gap-8">
            {/* Eyebrow */}
            <div className="inline-block px-3 py-1 rounded-full border border-accent/20 bg-accent/5">
              <span className="text-xs font-semibold tracking-widest text-accent uppercase">
                Premium Hair & Beauty
              </span>
            </div>
            
            {/* Headline */}
            <h1 className="text-4xl sm:text-5xl md:text-6xl font-bold tracking-tight text-foreground">
              Elevate Your Style at <span className="text-accent">Velvet Salon</span>
            </h1>
            
            {/* Copy */}
            <p className="max-w-2xl text-lg sm:text-xl leading-relaxed text-foreground/80">
              Experience world-class styling, coloring, and spa treatments in a luxurious, relaxing atmosphere tailored to you.
            </p>
            
            {/* CTAs */}
            <div className="flex flex-col sm:flex-row items-center gap-4 mt-4 w-full sm:w-auto">
              <Link
                href="/booking"
                className="flex h-12 w-full sm:w-auto items-center justify-center rounded-md bg-accent px-8 text-base font-medium text-accent-foreground shadow transition-colors hover:bg-accent/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
              >
                Book Your Appointment
              </Link>
              <Link
                href="#featured-services"
                className="flex h-12 w-full sm:w-auto items-center justify-center rounded-md border border-white/10 bg-transparent px-8 text-base font-medium text-foreground transition-colors hover:bg-white/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
              >
                Explore Services
              </Link>
            </div>
            
            {/* Trust Row */}
            <div className="mt-12 pt-8 border-t border-white/10 flex flex-col sm:flex-row items-center justify-center gap-2 sm:gap-6 text-sm text-foreground/60">
              <div className="flex items-center gap-2">
                <span className="h-1.5 w-1.5 rounded-full bg-accent"></span>
                Open Tue–Sun, 09:00 AM – 07:00 PM
              </div>
              <div className="hidden sm:block text-foreground/30">•</div>
              <div className="flex items-center gap-2">
                <span className="h-1.5 w-1.5 rounded-full bg-accent"></span>
                Colombo, Sri Lanka
              </div>
            </div>
          </div>
        </section>

        {/* Featured Services Section */}
        <section id="featured-services" className="w-full py-20 px-4 sm:px-6 lg:px-8 border-t border-white/5 bg-background">
          <div className="w-full max-w-5xl mx-auto flex flex-col gap-12">
            
            {/* Section Header */}
            <div className="flex flex-col gap-4 text-center sm:text-left">
              <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-foreground">
                Featured Services
              </h2>
              <p className="max-w-2xl text-base sm:text-lg text-foreground/70">
                Discover our most popular treatments, meticulously crafted to help you look and feel your absolute best.
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
                    <h3 className="text-xl font-semibold text-foreground">
                      {service.name}
                    </h3>
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
        </section>

        {/* Our Team Section */}
        <section id="our-team" className="w-full py-20 px-4 sm:px-6 lg:px-8 border-t border-white/5 bg-background/50">
          <div className="w-full max-w-5xl mx-auto flex flex-col gap-12">
            
            {/* Section Header */}
            <div className="flex flex-col items-center gap-4 text-center">
              <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-foreground">
                Our Team
              </h2>
              <p className="max-w-2xl text-base sm:text-lg text-foreground/70">
                Meet the talented professionals dedicated to providing you with an exceptional salon experience.
              </p>
            </div>

            {/* Team Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              {staffMembers.map((staff) => (
                <div 
                  key={staff.id}
                  className="flex flex-col items-center text-center p-8 rounded-2xl border border-white/10 bg-white/5"
                >
                  <div className="h-24 w-24 rounded-full bg-accent/20 flex items-center justify-center mb-6">
                    <span className="text-3xl font-semibold text-accent">
                      {staff.name.charAt(0)}
                    </span>
                  </div>
                  <h3 className="text-xl font-semibold text-foreground mb-1">
                    {staff.name}
                  </h3>
                  <p className="text-sm font-medium text-accent mb-3">
                    {staff.role}
                  </p>
                  <p className="text-sm text-foreground/60">
                    Specialty: {staff.specialty}
                  </p>
                </div>
              ))}
            </div>
            
          </div>
        </section>

      </main>
    </div>
  );
}
