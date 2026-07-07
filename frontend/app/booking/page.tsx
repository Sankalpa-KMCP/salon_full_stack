import { Metadata } from 'next';
import BookingClient from './booking-client';

export const metadata: Metadata = {
  title: 'Book Appointment | Velvet Salon',
  description: 'Book your premium hair and beauty experience at Velvet Salon.',
};

export default function BookingPage() {
  return <BookingClient />;
}
