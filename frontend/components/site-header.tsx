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
    <header className="sticky top-0 z-50 w-full border-b border-white/10 bg-background/80 backdrop-blur-md">
      <div className="container mx-auto flex h-16 items-center justify-between px-4 sm:px-6 lg:px-8">
        <Link href="/" className="flex items-center gap-2" onClick={() => setIsMobileMenuOpen(false)}>
          <span className="text-xl font-semibold tracking-tight text-foreground">
            Velvet Salon
          </span>
        </Link>
        <nav className="hidden md:flex items-center gap-6 text-sm font-medium">
          <Link href="/services" className="text-foreground/80 hover:text-accent transition-colors">
            Services
          </Link>
          <Link href="/team" className="text-foreground/80 hover:text-accent transition-colors">
            Team
          </Link>
          <Link href="/gallery" className="text-foreground/80 hover:text-accent transition-colors">
            Gallery
          </Link>
          <Link href="/contact" className="text-foreground/80 hover:text-accent transition-colors">
            Contact
          </Link>
        </nav>
        <div className="flex items-center gap-4">
          <Link
            href="/booking"
            className="hidden md:inline-flex h-9 items-center justify-center rounded-md bg-accent px-4 py-2 text-sm font-medium text-accent-foreground shadow transition-colors hover:bg-accent/90 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50"
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
        <div id="mobile-menu" className="md:hidden border-t border-white/10 bg-background px-4 py-4">
          <nav className="flex flex-col gap-4 text-sm font-medium">
            <Link href="/services" className="text-foreground/80 hover:text-accent transition-colors" onClick={() => setIsMobileMenuOpen(false)}>
              Services
            </Link>
            <Link href="/team" className="text-foreground/80 hover:text-accent transition-colors" onClick={() => setIsMobileMenuOpen(false)}>
              Team
            </Link>
            <Link href="/gallery" className="text-foreground/80 hover:text-accent transition-colors" onClick={() => setIsMobileMenuOpen(false)}>
              Gallery
            </Link>
            <Link href="/contact" className="text-foreground/80 hover:text-accent transition-colors" onClick={() => setIsMobileMenuOpen(false)}>
              Contact
            </Link>
            <Link
              href="/booking"
              className="mt-2 inline-flex h-9 w-full items-center justify-center rounded-md bg-accent px-4 py-2 text-sm font-medium text-accent-foreground shadow transition-colors hover:bg-accent/90 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
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
