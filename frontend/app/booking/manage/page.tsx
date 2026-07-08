import { Metadata } from "next";
import { Suspense } from "react";
import ManageClient from "./manage-client";

export const metadata: Metadata = {
  title: "Manage Appointment | Velvet Salon",
  description: "View or cancel your upcoming Velvet Salon appointment.",
};

export default function ManagePage() {
  return (
    <Suspense fallback={<div className="min-h-screen flex items-center justify-center text-foreground">Loading...</div>}>
      <ManageClient />
    </Suspense>
  );
}
