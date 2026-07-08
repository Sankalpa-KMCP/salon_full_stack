"use client";

import { useState, useEffect } from 'react';
import Link from 'next/link';

export function SiteHeader() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isMobileMenuOpen) {
        setIsMobileMenuOpen(false);
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isMobileMenuOpen]);

  return (
    <header className="sticky top-0 z-50 w-full border-b border-white/5 bg-background/70 backdrop-blur-xl">
      <div className="container mx-auto flex h-20 items-center justify-between px-4 sm:px-6 lg:px-8">
        <Link href="/" className="flex items-center gap-2" onClick={() => setIsMobileMenuOpen(false)}>
          <span className="text-2xl font-display font-medium tracking-wide text-foreground">
            Velvet Salon
          </span>
        </Link>
        <nav className="hidden md:flex items-center gap-8 text-sm font-medium tracking-wide uppercase text-foreground/70">
          <Link href="/services" className="hover:text-accent transition-colors">
            Services
          </Link>
          <Link href="/team" className="hover:text-accent transition-colors">
            Team
          </Link>
          <Link href="/gallery" className="hover:text-accent transition-colors">
            Gallery
          </Link>
          <Link href="/contact" className="hover:text-accent transition-colors">
            Contact
          </Link>
        </nav>
        <div className="flex items-center gap-4">
          <Link
            href="/booking"
            className="hidden md:inline-flex h-10 items-center justify-center rounded-sm bg-accent/90 px-6 py-2 text-sm font-medium text-accent-foreground shadow-md shadow-accent/10 transition-all hover:bg-accent hover:shadow-accent/20 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50"
          >
            Book Now
          </Link>
          <button
            className="inline-flex items-center justify-center rounded-md p-2 text-foreground/80 hover:bg-white/5 hover:text-foreground md:hidden focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
            aria-label="Toggle Menu"
            aria-expanded={isMobileMenuOpen}
            aria-controls="mobile-menu"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="h-5 w-5"
            >
              <line x1="4" x2="20" y1="12" y2="12" />
              <line x1="4" x2="20" y1="6" y2="6" />
              <line x1="4" x2="20" y1="18" y2="18" />
            </svg>
          </button>
        </div>
      </div>
      
      {isMobileMenuOpen && (
        <div id="mobile-menu" className="md:hidden border-t border-white/5 bg-background/95 backdrop-blur-xl px-4 py-4">
          <nav className="flex flex-col gap-4 text-sm font-medium tracking-wide uppercase">
            <Link href="/services" className="text-foreground/70 hover:text-accent transition-colors py-2" onClick={() => setIsMobileMenuOpen(false)}>
              Services
            </Link>
            <Link href="/team" className="text-foreground/70 hover:text-accent transition-colors py-2" onClick={() => setIsMobileMenuOpen(false)}>
              Team
            </Link>
            <Link href="/gallery" className="text-foreground/70 hover:text-accent transition-colors py-2" onClick={() => setIsMobileMenuOpen(false)}>
              Gallery
            </Link>
            <Link href="/contact" className="text-foreground/70 hover:text-accent transition-colors py-2" onClick={() => setIsMobileMenuOpen(false)}>
              Contact
            </Link>
            <Link
              href="/booking"
              className="mt-4 inline-flex h-10 w-full items-center justify-center rounded-sm bg-accent/90 px-4 py-2 text-sm font-medium text-accent-foreground shadow transition-all hover:bg-accent focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
              onClick={() => setIsMobileMenuOpen(false)}
            >
              Book Now
            </Link>
          </nav>
        </div>
      )}
    </header>
  );
}
