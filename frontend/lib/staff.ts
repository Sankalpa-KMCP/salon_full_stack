export interface Staff {
  id: string;
  name: string;
  role: string;
  specialty: string;
}

export const staffMembers: Staff[] = [
  {
    id: "emma",
    name: "Emma",
    role: "Senior Hair Stylist",
    specialty: "Cuts & Styling",
  },
  {
    id: "sophia",
    name: "Sophia",
    role: "Color Specialist",
    specialty: "Balayage & Tints",
  },
  {
    id: "mia",
    name: "Mia",
    role: "Esthetician & Nail Technician",
    specialty: "Facials & Nails",
  },
];
