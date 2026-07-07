import Link from 'next/link';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Gallery | Velvet Salon',
  description: 'Preview the categories of premium salon work we perform at Velvet Salon.',
};

export default function GalleryPage() {
  const categories = [
    { id: 'hair', title: 'Hair' },
    { id: 'makeup', title: 'Makeup' },
    { id: 'nails', title: 'Nails' },
    { id: 'treatments', title: 'Treatments' },
  ];

  return (
    <div className="flex flex-col flex-1 bg-background font-sans">
      <main className="flex-1 flex flex-col items-center py-20 px-4 sm:px-6 lg:px-8">
        <div className="w-full max-w-5xl mx-auto flex flex-col gap-16">
          
          {/* Page Header */}
          <div className="flex flex-col items-center gap-4 text-center">
            <h1 className="text-4xl sm:text-5xl font-bold tracking-tight text-foreground">
              Our Gallery
            </h1>
            <p className="max-w-2xl text-lg text-foreground/70">
              A preview of our signature services. Real client photography and our complete portfolio will be published here soon.
            </p>
          </div>

          {/* Gallery Categories Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-8">
            {categories.map((category) => (
              <div 
                key={category.id}
                className="group relative flex flex-col items-center justify-center aspect-[4/3] rounded-2xl border border-white/10 bg-white/5 overflow-hidden transition-colors hover:bg-white/10"
              >
                {/* Decorative Background Gradient (subtle) */}
                <div className="absolute inset-0 bg-gradient-to-br from-accent/5 to-transparent opacity-50 group-hover:opacity-100 transition-opacity" />
                
                {/* Content */}
                <div className="relative flex flex-col items-center gap-3 p-6 text-center">
                  <span className="text-sm font-medium uppercase tracking-widest text-accent">
                    Category
                  </span>
                  <h2 className="text-3xl font-semibold text-foreground">
                    {category.title}
                  </h2>
                </div>
              </div>
            ))}
          </div>

          {/* Booking Callout */}
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
