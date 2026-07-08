import Link from 'next/link';
import { featuredServices } from '@/lib/services';
import { staffMembers } from '@/lib/staff';

export default function Home() {
  return (
    <div className="flex flex-col flex-1 bg-transparent font-sans">
      <main className="flex-1 flex flex-col items-center">
        
        {/* Hero Section */}
        <section className="w-full flex flex-col items-center justify-center min-h-[70vh] py-20 px-4 sm:px-6 lg:px-8">
          <div className="w-full max-w-4xl mx-auto flex flex-col items-center text-center gap-8">
            {/* Eyebrow */}
            <div className="inline-block px-4 py-1.5 rounded-sm border border-accent/30 bg-accent/5 backdrop-blur-sm">
              <span className="text-xs font-semibold tracking-[0.2em] text-accent uppercase">
                Premium Hair & Beauty
              </span>
            </div>
            
            {/* Headline */}
            <h1 className="text-5xl sm:text-6xl md:text-7xl font-display font-medium tracking-wide text-foreground">
              Elevate Your Style <br/> at <span className="text-accent italic">Velvet Salon</span>
            </h1>
            
            {/* Copy */}
            <p className="max-w-2xl text-lg sm:text-xl font-light tracking-wide leading-relaxed text-foreground/70">
              Experience world-class styling, coloring, and spa treatments in a luxurious, relaxing atmosphere tailored to you.
            </p>
            
            {/* CTAs */}
            <div className="flex flex-col sm:flex-row items-center gap-6 mt-8 w-full sm:w-auto">
              <Link
                href="/booking"
                className="flex h-12 w-full sm:w-auto items-center justify-center rounded-sm bg-accent/90 px-10 text-sm font-medium tracking-wide uppercase text-accent-foreground shadow-lg shadow-accent/10 transition-all hover:bg-accent hover:shadow-accent/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
              >
                Book Appointment
              </Link>
              <Link
                href="#featured-services"
                className="flex h-12 w-full sm:w-auto items-center justify-center rounded-sm border border-white/20 bg-black/20 backdrop-blur-sm px-10 text-sm font-medium tracking-wide uppercase text-foreground transition-all hover:bg-white/10 hover:border-white/30 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
              >
                Explore Services
              </Link>
            </div>
            
            {/* Trust Row */}
            <div className="mt-16 pt-8 border-t border-white/5 flex flex-col sm:flex-row items-center justify-center gap-4 sm:gap-8 text-sm tracking-wide text-foreground/50 uppercase">
              <div className="flex items-center gap-3">
                <span className="h-1.5 w-1.5 rounded-full bg-accent"></span>
                Open Tue–Sun, 09:00 AM – 07:00 PM
              </div>
              <div className="hidden sm:block text-foreground/20">•</div>
              <div className="flex items-center gap-3">
                <span className="h-1.5 w-1.5 rounded-full bg-accent"></span>
                Colombo, Sri Lanka
              </div>
            </div>
          </div>
        </section>

        {/* Featured Services Section */}
        <section id="featured-services" className="w-full py-24 px-4 sm:px-6 lg:px-8 border-t border-white/5 bg-background/50">
          <div className="w-full max-w-5xl mx-auto flex flex-col gap-16">
            
            {/* Section Header */}
            <div className="flex flex-col gap-4 text-center sm:text-left">
              <h2 className="text-4xl sm:text-5xl font-display font-medium text-foreground">
                Featured Services
              </h2>
              <p className="max-w-2xl text-base sm:text-lg font-light tracking-wide text-foreground/60">
                Discover our most popular treatments, meticulously crafted to help you look and feel your absolute best.
              </p>
            </div>

            {/* Services Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {featuredServices.map((service) => (
                <div 
                  key={service.id}
                  className="flex flex-col justify-between p-8 rounded-sm border border-white/5 bg-white/[0.02] backdrop-blur-sm hover:bg-white/[0.04] hover:border-accent/30 transition-all duration-300"
                >
                  <div className="flex flex-col gap-3">
                    <h3 className="text-2xl font-display text-foreground">
                      {service.name}
                    </h3>
                    <div className="flex items-center gap-4 text-sm tracking-wide text-foreground/50 uppercase">
                      <span>{service.durationMinutes} min</span>
                    </div>
                  </div>
                  <div className="mt-10 flex items-center justify-between">
                    <span className="text-xl font-medium tracking-wide text-accent">
                      {service.priceFormatted}
                    </span>
                    <Link 
                      href="/booking" 
                      className="text-sm font-medium tracking-wide uppercase text-foreground/70 hover:text-accent transition-colors"
                      aria-label={`Book ${service.name}`}
                    >
                      Book &rarr;
                    </Link>
                  </div>
                </div>
              ))}
            </div>

          </div>
        </section>

        {/* Our Team Section */}
        <section id="our-team" className="w-full py-24 px-4 sm:px-6 lg:px-8 border-t border-white/5">
          <div className="w-full max-w-5xl mx-auto flex flex-col gap-16">
            
            {/* Section Header */}
            <div className="flex flex-col items-center gap-4 text-center">
              <h2 className="text-4xl sm:text-5xl font-display font-medium text-foreground">
                Our Team
              </h2>
              <p className="max-w-2xl text-base sm:text-lg font-light tracking-wide text-foreground/60">
                Meet the talented professionals dedicated to providing you with an exceptional salon experience.
              </p>
            </div>

            {/* Team Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-10">
              {staffMembers.map((staff) => (
                <div 
                  key={staff.id}
                  className="group flex flex-col items-center text-center p-10 rounded-sm border border-white/5 bg-white/[0.02] backdrop-blur-sm hover:bg-white/[0.04] hover:border-accent/30 transition-all duration-300"
                >
                  <div className="h-32 w-32 rounded-full bg-black/40 border border-white/10 flex items-center justify-center mb-8 group-hover:border-accent/50 transition-colors">
                    <span className="text-4xl font-display text-accent/50 group-hover:text-accent transition-colors">
                      {staff.name.charAt(0)}
                    </span>
                  </div>
                  <h3 className="text-2xl font-display text-foreground mb-2">
                    {staff.name}
                  </h3>
                  <p className="text-xs font-semibold tracking-widest uppercase text-accent mb-4">
                    {staff.role}
                  </p>
                  <p className="text-sm font-light tracking-wide text-foreground/50">
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
