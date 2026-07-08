import Link from 'next/link';
import Image from 'next/image';
import { featuredServices } from '@/lib/services';
import { staffMembers } from '@/lib/staff';

const serviceImageMap: Record<string, string> = {
  'Signature Haircut': '/media/service-precision-cut.webp',
  'Color Transformation': '/media/service-color-ritual.webp',
  'Luxury Spa Treatment': '/media/service-spa-finish.webp',
};

export default function Home() {
  return (
    <div className="flex flex-col flex-1 bg-transparent font-sans">
      <main className="flex-1 flex flex-col items-center w-full">
        
        {/* Cinematic Hero Section */}
        <section className="relative w-full min-h-[85vh] flex items-center justify-center overflow-hidden">
          {/* Cinematic Hero Background */}
          <div className="absolute inset-0 z-0 bg-black">
            {/* Fallback Image */}
            <Image
              src="/media/hero-ambience.webp"
              alt="Velvet Salon Ambience"
              fill
              priority
              className="object-cover opacity-50 mix-blend-luminosity"
            />
            {/* Ambient Loop Video */}
            <video
              autoPlay
              loop
              muted
              playsInline
              poster="/media/hero-ambience.webp"
              className="absolute inset-0 w-full h-full object-cover opacity-50 mix-blend-luminosity motion-reduce:hidden"
            >
              <source src="/media/hero-ambience-loop.mp4" type="video/mp4" />
            </video>
            {/* Temporary Gradient Overlay */}
            <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-accent/10 via-background/80 to-background opacity-80" />
            <div className="absolute inset-0 bg-gradient-to-b from-background/40 via-transparent to-background" />
          </div>

          <div className="relative z-10 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col items-center text-center">
            {/* Eyebrow */}
            <div className="inline-block px-4 py-1.5 rounded-sm border border-accent/20 bg-accent/5 backdrop-blur-md mb-8">
              <span className="text-[10px] sm:text-xs font-semibold tracking-[0.3em] text-accent uppercase">
                Artistry & Elegance
              </span>
            </div>
            
            {/* Headline */}
            <h1 className="text-5xl sm:text-7xl md:text-8xl font-display font-medium tracking-tight text-foreground leading-[1.1] mb-6">
              Discover <br className="hidden sm:block"/> <span className="text-accent italic">Velvet</span> Salon
            </h1>
            
            {/* Copy */}
            <p className="max-w-2xl text-lg sm:text-xl font-light tracking-wide leading-relaxed text-foreground/70 mb-12">
              A sanctuary for bespoke styling, advanced coloring, and immersive spa treatments. Tailored for those who seek perfection.
            </p>
            
            {/* CTAs */}
            <div className="flex flex-col sm:flex-row items-center gap-6 w-full sm:w-auto">
              <Link
                href="/booking"
                className="group relative flex h-14 w-full sm:w-auto items-center justify-center overflow-hidden rounded-sm bg-accent px-12 text-sm font-medium tracking-[0.15em] uppercase text-accent-foreground shadow-xl shadow-accent/10 transition-all hover:bg-accent/90"
              >
                <span className="relative z-10">Reserve a Moment</span>
                <div className="absolute inset-0 -translate-x-full bg-white/20 transition-transform duration-500 group-hover:translate-x-0" />
              </Link>
              <Link
                href="#sensory-story"
                className="flex h-14 w-full sm:w-auto items-center justify-center rounded-sm border border-white/20 bg-black/30 backdrop-blur-md px-12 text-sm font-medium tracking-[0.15em] uppercase text-foreground transition-all hover:bg-white/10 hover:border-white/40"
              >
                Explore Experience
              </Link>
            </div>
          </div>
        </section>

        {/* Sensory Story / Salon Experience */}
        <section id="sensory-story" className="relative w-full py-32 px-4 sm:px-6 lg:px-8 bg-background border-t border-white/5">
          <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
            <div className="flex flex-col gap-8 order-2 lg:order-1">
              <h2 className="text-4xl sm:text-6xl font-display font-medium text-foreground leading-tight">
                A Symphony of <span className="text-accent italic">Senses</span>
              </h2>
              <p className="text-lg font-light tracking-wide leading-relaxed text-foreground/70">
                From the moment you step through our doors, you are enveloped in an atmosphere of serene luxury. The ambient lighting, the subtle fragrance of botanical oils, and the meticulous attention of our master stylists converge to create an experience that transcends traditional salon care.
              </p>
              <p className="text-lg font-light tracking-wide leading-relaxed text-foreground/70">
                We believe that true beauty is an expression of holistic well-being. Our bespoke treatments are designed not just to elevate your aesthetic, but to rejuvenate your spirit.
              </p>
              <div className="pt-8 flex items-center gap-12 text-sm font-medium tracking-[0.2em] uppercase text-foreground/50">
                <div className="flex flex-col gap-2">
                  <span className="text-3xl font-display text-accent">15+</span>
                  Years of Excellence
                </div>
                <div className="flex flex-col gap-2">
                  <span className="text-3xl font-display text-accent">3</span>
                  Master Stylists
                </div>
              </div>
            </div>
            {/* Media Placeholder: Experience Image */}
            <div className="relative order-1 lg:order-2 aspect-[4/5] w-full rounded-sm overflow-hidden bg-white/5 border border-white/10 group">
              <Image
                src="/media/service-spa-finish.webp"
                alt="Salon Experience"
                fill
                className="object-cover opacity-80 group-hover:opacity-100 transition-opacity duration-700"
              />
              <div className="absolute inset-0 bg-gradient-to-tr from-accent/10 to-transparent mix-blend-overlay" />
            </div>
          </div>
        </section>

        {/* Signature Services */}
        <section id="services" className="w-full py-32 px-4 sm:px-6 lg:px-8 border-t border-white/5 relative">
          {/* Ambient Background Element */}
          <div className="absolute top-0 right-0 w-[800px] h-[800px] bg-accent/5 rounded-full blur-[120px] -translate-y-1/2 translate-x-1/3 pointer-events-none" />

          <div className="w-full max-w-7xl mx-auto flex flex-col gap-20 relative z-10">
            <div className="flex flex-col md:flex-row md:items-end justify-between gap-8">
              <div className="flex flex-col gap-6 max-w-2xl">
                <span className="text-xs font-semibold tracking-[0.2em] text-accent uppercase">Our Offerings</span>
                <h2 className="text-4xl sm:text-6xl font-display font-medium text-foreground">
                  Signature Services
                </h2>
              </div>
              <Link
                href="/booking"
                className="inline-flex items-center gap-2 text-sm font-medium tracking-[0.15em] uppercase text-foreground hover:text-accent transition-colors pb-2 border-b border-white/20 hover:border-accent"
              >
                View Full Menu &rarr;
              </Link>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {featuredServices.map((service) => (
                <div 
                  key={service.id}
                  className="group flex flex-col rounded-sm border border-white/10 bg-black/40 overflow-hidden hover:border-accent/40 transition-all duration-500"
                >
                  {/* Media Placeholder: Service Image */}
                  <div className="relative aspect-[16/10] bg-white/5 overflow-hidden">
                    <Image
                      src={serviceImageMap[service.name] || '/media/hero-ambience.webp'}
                      alt={service.name}
                      fill
                      className="object-cover opacity-70 group-hover:scale-105 group-hover:opacity-100 transition-all duration-700"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent z-10" />
                  </div>

                  <div className="flex flex-col flex-1 p-8 relative z-20 -mt-12">
                    <div className="flex justify-between items-start mb-6">
                      <h3 className="text-3xl font-display text-foreground group-hover:text-accent transition-colors">
                        {service.name}
                      </h3>
                    </div>
                    <p className="text-sm font-light tracking-wide leading-relaxed text-foreground/60 mb-8 flex-1">
                      A customized treatment experience requiring {service.durationMinutes} minutes of dedicated artistry.
                    </p>
                    <div className="flex items-center justify-between mt-auto pt-6 border-t border-white/10">
                      <span className="text-xl font-medium tracking-wide text-foreground">
                        {service.priceFormatted}
                      </span>
                      <Link
                        href="/booking"
                        className="text-xs font-semibold tracking-[0.15em] uppercase text-accent hover:text-white transition-colors"
                      >
                        Book &rarr;
                      </Link>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Curators of Style / Team */}
        <section id="our-team" className="w-full py-32 px-4 sm:px-6 lg:px-8 border-t border-white/5 bg-black/20">
          <div className="w-full max-w-7xl mx-auto flex flex-col gap-20">
            <div className="flex flex-col items-center text-center gap-6 max-w-3xl mx-auto">
              <span className="text-xs font-semibold tracking-[0.2em] text-accent uppercase">The Artisans</span>
              <h2 className="text-4xl sm:text-6xl font-display font-medium text-foreground">
                Curators of Style
              </h2>
              <p className="text-lg font-light tracking-wide text-foreground/60">
                Our resident experts bring decades of international experience and an unyielding passion for their craft.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-12">
              {staffMembers.map((staff) => (
                <div 
                  key={staff.id}
                  className="group flex flex-col items-center text-center"
                >
                  {/* Media Placeholder: Portrait */}
                  <div className="relative w-full aspect-[4/5] mb-8 overflow-hidden rounded-sm bg-white/5 border border-white/10">
                    <Image
                      src="/media/portrait-stylist-1.webp"
                      alt={staff.name}
                      fill
                      className="object-cover opacity-70 group-hover:scale-105 transition-all duration-700 mix-blend-luminosity group-hover:mix-blend-normal"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-transparent to-transparent z-10 opacity-80 group-hover:opacity-40 transition-opacity duration-500" />
                  </div>

                  <h3 className="text-3xl font-display text-foreground mb-3">
                    {staff.name}
                  </h3>
                  <p className="text-xs font-semibold tracking-[0.2em] uppercase text-accent mb-4">
                    {staff.role}
                  </p>
                  <p className="text-sm font-light tracking-wide text-foreground/50 border-t border-white/10 pt-4 px-6 w-full">
                    Master of {staff.specialty}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Concierge / Final CTA */}
        <section className="relative w-full py-40 px-4 sm:px-6 lg:px-8 border-t border-white/5 overflow-hidden">
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom,_var(--tw-gradient-stops))] from-accent/10 via-background to-background opacity-60" />

          <div className="relative z-10 w-full max-w-4xl mx-auto flex flex-col items-center text-center gap-10">
            <h2 className="text-5xl sm:text-7xl font-display font-medium text-foreground">
              Begin Your <span className="text-accent italic">Journey</span>
            </h2>
            <p className="text-xl font-light tracking-wide text-foreground/60 max-w-2xl">
              Secure your appointment through our digital concierge. Select your service, choose your artisan, and reserve your time.
            </p>
            <div className="mt-8 flex flex-col sm:flex-row gap-6 w-full sm:w-auto">
              <Link
                href="/booking"
                className="flex h-14 w-full sm:w-auto items-center justify-center rounded-sm bg-accent px-12 text-sm font-medium tracking-[0.15em] uppercase text-accent-foreground shadow-xl shadow-accent/20 transition-all hover:bg-accent/90 hover:scale-[1.02]"
              >
                Book Now
              </Link>
              <Link
                href="/booking/manage"
                className="flex h-14 w-full sm:w-auto items-center justify-center rounded-sm border border-white/20 bg-transparent px-12 text-sm font-medium tracking-[0.15em] uppercase text-foreground transition-all hover:bg-white/5 hover:border-white/40"
              >
                Manage Appointment
              </Link>
            </div>
          </div>
        </section>

        {/* Minimal Footer */}
        <footer className="w-full border-t border-white/10 bg-black/60 py-12 px-4 sm:px-6 lg:px-8">
          <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-6">
            <div className="text-2xl font-display font-medium tracking-wide text-foreground">
              Velvet Salon
            </div>
            <div className="flex items-center gap-6 text-xs font-light tracking-widest uppercase text-foreground/50">
              <Link href="/privacy" className="hover:text-accent transition-colors">Privacy</Link>
              <Link href="/terms" className="hover:text-accent transition-colors">Terms</Link>
              <span>&copy; {new Date().getFullYear()} Velvet Salon</span>
            </div>
          </div>
        </footer>

      </main>
    </div>
  );
}
