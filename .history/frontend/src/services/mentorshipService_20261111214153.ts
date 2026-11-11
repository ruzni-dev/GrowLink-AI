export type MentorshipMode = 'ONLINE' | 'IN_PERSON' | 'BOTH';
export type MentorshipOfferStatus = 'ACTIVE' | 'PAUSED' | 'CLOSED';
export type MentorshipRequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';
export type MentorshipCategory = { value: string; label: string };
export type MentorshipSkill = { id: number; name: string; category?: string; proficiencyLevel?: string; yearsOfExperience?: number };
export type MentorshipOffer = {
  id: number;
  mentorName: string;
  mentorProfileImage?: string;
  mentorBio?: string;
  mentorLocation?: string;
  title: string;
  description: string;
  category: string;
  categoryLabel: string;
  skills: MentorshipSkill[];
  experience?: string;
  availability: string;
  preferredMode: MentorshipMode;
  location?: string;
  status: MentorshipOfferStatus;
  createdAt: string;
  updatedAt: string;
};
export type MentorshipRequest = {
  id: number;
  offerId: number;
  offerTitle: string;
  mentorName: string;
  menteeName: string;
  message: string;
  goals: string;
  preferredMode: MentorshipMode;
  status: MentorshipRequestStatus;
  createdAt: string;
  updatedAt: string;
};
export type PagedResponse<T> = { content: T[]; number: number; size: number; totalElements: number; totalPages: number; first: boolean; last: boolean };
export type OfferSearch = { q?: string; category?: string; location?: string; page?: number; size?: number; sortBy?: string };
export type OfferPayload = { title: string; description: string; category: string; skillIds: number[]; experience?: string; availability: string; preferredMode: MentorshipMode; location?: string };
export type RequestPayload = { offerId: number; message: string; goals: string; preferredMode: MentorshipMode };

export const mentorshipCategories: MentorshipCategory[] = [
  { value: 'EDUCATION_LEARNING', label: 'Education & Learning' },
  { value: 'TECHNOLOGY_DIGITAL_SKILLS', label: 'Technology & Digital Skills' },
  { value: 'CAREER_PROFESSIONAL_DEVELOPMENT', label: 'Career & Professional Development' },
  { value: 'MENTORSHIP_GUIDANCE', label: 'Mentorship & Guidance' },
  { value: 'SPORTS_COACHING', label: 'Sports & Coaching' },
  { value: 'VOLUNTEERING_SOCIAL_SERVICES', label: 'Volunteering & Social Services' },
  { value: 'ENTREPRENEURSHIP', label: 'Entrepreneurship' },
  { value: 'ARTS_CULTURE', label: 'Arts & Culture' },
];

async function request<T>(token: string | null, path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`/api/mentorship${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.message || 'Unable to complete the mentorship request.');
  return body as T;
}

function queryString(params: Record<string, string | number | undefined>) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && String(value).trim() !== '') query.set(key, String(value));
  });
  return query.size ? `?${query}` : '';
}

const json = (body: unknown): RequestInit => ({ method: 'POST', body: JSON.stringify(body) });

export const mentorshipService = {
  findOffers: (params: OfferSearch = {}) => request<PagedResponse<MentorshipOffer>>(null, `/offers${queryString(params)}`),
  getOffer: (id: number) => request<MentorshipOffer>(null, `/offers/${id}`),
  createOffer: (token: string, payload: OfferPayload) => request<MentorshipOffer>(token, '/offers', json(payload)),
  updateOffer: (token: string, id: number, payload: OfferPayload) => request<MentorshipOffer>(token, `/offers/${id}`, { ...json(payload), method: 'PUT' }),
  changeOfferStatus: (token: string, id: number, status: MentorshipOfferStatus) => request<MentorshipOffer>(token, `/offers/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }), headers: { 'Content-Type': 'application/json' } }),
  myOffers: (token: string, page = 0, size = 20) => request<PagedResponse<MentorshipOffer>>(token, `/my-offers${queryString({ page, size })}`),
  createRequest: (token: string, payload: RequestPayload) => request<MentorshipRequest>(token, '/requests', json(payload)),
  receivedRequests: (token: string, page = 0, size = 20) => request<PagedResponse<MentorshipRequest>>(token, `/requests/received${queryString({ page, size })}`),
  sentRequests: (token: string, page = 0, size = 20) => request<PagedResponse<MentorshipRequest>>(token, `/requests/sent${queryString({ page, size })}`),
  acceptRequest: (token: string, id: number) => request<MentorshipRequest>(token, `/requests/${id}/accept`, { method: 'PATCH' }),
  rejectRequest: (token: string, id: number) => request<MentorshipRequest>(token, `/requests/${id}/reject`, { method: 'PATCH' }),
  cancelRequest: (token: string, id: number) => request<MentorshipRequest>(token, `/requests/${id}/cancel`, { method: 'PATCH' }),
  completeRequest: (token: string, id: number) => request<MentorshipRequest>(token, `/requests/${id}/complete`, { method: 'PATCH' }),
};