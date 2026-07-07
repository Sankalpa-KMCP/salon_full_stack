export interface Service {
  id: string;
  name: string;
  priceFormatted: string;
  durationMinutes: number;
}

export const featuredServices: Service[] = [
  {
    id: "haircut-styling",
    name: "Haircut & Styling",
    priceFormatted: "LKR 4,500",
    durationMinutes: 45,
  },
  {
    id: "hair-coloring-balayage",
    name: "Hair Coloring (Balayage)",
    priceFormatted: "LKR 12,000",
    durationMinutes: 120,
  },
  {
    id: "hair-rebonding-straightening",
    name: "Hair Rebonding / Straightening",
    priceFormatted: "LKR 15,000",
    durationMinutes: 180,
  },
  {
    id: "classic-glow-facial",
    name: "Classic Glow Facial",
    priceFormatted: "LKR 5,000",
    durationMinutes: 60,
  },
  {
    id: "luxury-mani-pedi",
    name: "Luxury Manicure & Pedicure",
    priceFormatted: "LKR 4,000",
    durationMinutes: 75,
  },
];
