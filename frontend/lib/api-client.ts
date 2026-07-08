export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

export interface ServiceDto {
  slug: string;
  name: string;
  description: string;
  price: number;
  durationMinutes: number;
  imageUrl: string | null;
}

export interface StaffDto {
  slug: string;
  name: string;
  role: string;
  specialty: string;
  bio: string;
  imageUrl: string | null;
}

export interface SlotDto {
  startTime: string;
  endTime: string;
}

export interface AvailabilityResponseDto {
  serviceSlug: string;
  staffSlug: string;
  date: string;
  timeZone: string;
  slots: SlotDto[];
}

export interface BookingRequestDto {
  serviceSlug: string;
  staffSlug: string;
  startTime: string;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  notes?: string;
}

export interface BookingResponseDto {
  id: string;
  serviceSlug: string;
  staffSlug: string;
  staffName: string;
  startTime: string;
  endTime: string;
  status: string;
  cancellationToken: string;
}

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

async function fetchJson<T>(url: string, options?: RequestInit): Promise<T> {
  if (!API_BASE_URL) {
    throw new Error('NEXT_PUBLIC_API_BASE_URL is not configured.');
  }

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  if (!response.ok) {
    let errorMessage = 'An unexpected error occurred.';
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch {
      // Ignore JSON parse errors for non-JSON responses
    }
    throw new ApiError(response.status, errorMessage);
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}

export const apiClient = {
  getServices: () => fetchJson<ServiceDto[]>('/api/catalog/services'),

  getStaff: (serviceSlug?: string) => {
    const url = serviceSlug ? `/api/catalog/staff?serviceSlug=${serviceSlug}` : '/api/catalog/staff';
    return fetchJson<StaffDto[]>(url);
  },

  getAvailability: (serviceSlug: string, staffSlug: string, date: string) => {
    return fetchJson<AvailabilityResponseDto>(
      `/api/booking/availability?serviceSlug=${serviceSlug}&staffSlug=${staffSlug}&date=${date}`
    );
  },

  createBooking: (data: BookingRequestDto) => {
    return fetchJson<BookingResponseDto>('/api/booking', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }
};
