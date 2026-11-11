export type OpportunityCategory = 'JOB' | 'INTERNSHIP' | 'SCHOLARSHIP' | 'TRAINING' | 'VOLUNTEERING' | 'COMPETITION' | 'HACKATHON' | 'EVENT' | 'OTHER';
export type OpportunityStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED' | 'ARCHIVED';
export type OpportunityApplicationStatus = 'INTERESTED' | 'APPLIED' | 'SHORTLISTED' | 'REJECTED' | 'ACCEPTED' | 'WITHDRAWN';
export type OpportunityCategoryOption = { value: OpportunityCategory; label: string };
export type Opportunity = {
  id: number;
  title: string;
  description: string;
  organizationName: string;
  category: OpportunityCategory;
  opportunityType?: string;
  location?: string;
  remote: boolean;
  eligibility?: string;
  requiredSkills: string[];
  qualification?: string;
  applicationUrl?: string;
  contactInformation?: string;
  startDate?: string;
  deadline?: string;
  status: OpportunityStatus;
  createdAt: string;
  updatedAt: string;
};
export type SavedOpportunity = { id: number; savedAt: string; opportunity: Opportunity };
export type OpportunityApplication = { id: number; opportunity: Opportunity; status: OpportunityApplicationStatus; notes?: string; appliedAt?: string; createdAt: string; updatedAt: string };
export type Paged<T> = { content: T[]; number: number; size: number; totalElements: number; totalPages: number; first: boolean; last: boolean };
export type OpportunityFilters = { q?: string; category?: string; location?: string; remote?: string; organization?: string; deadlineAfter?: string; deadlineBefore?: string; page?: number; size?: number; sortBy?: string };
export type OpportunityPayload = Omit<Opportunity, 'id' | 'createdAt' | 'updatedAt' | 'status'> & { status?: OpportunityStatus };

export const opportunityCategories: OpportunityCategoryOption[] = [
  { value: 'JOB', label: 'Job' },
  { value: 'INTERNSHIP', label: 'Internship' },
  { value: 'SCHOLARSHIP', label: 'Scholarship' },
  { value: 'TRAINING', label: 'Training' },
  { value: 'VOLUNTEERING', label: 'Volunteering' },
  { value: 'COMPETITION', label: 'Competition' },
  { value: 'HACKATHON', label: 'Hackathon' },
  { value: 'EVENT', label: 'Event' },
  { value: 'OTHER', label: 'Other' },
];

async function request<T>(token: string | null, path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`/api/opportunities${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
  const body = response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.message || 'Unable to complete the opportunity request.');
  return body as T;
}

function queryString(params: OpportunityFilters & { status?: string }) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') query.set(key, String(value));
  });
  return query.size ? `?${query}` : '';
}

const json = (body: unknown, method = 'POST'): RequestInit => ({ method, body: JSON.stringify(body) });

export const opportunityService = {
  categories: () => request<OpportunityCategoryOption[]>(null, '/categories'),
  search: (filters: OpportunityFilters = {}) => request<Paged<Opportunity>>(null, `/search${queryString(filters)}`),
  get: (id: number) => request<Opportunity>(null, `/${id}`),
  create: (token: string, payload: OpportunityPayload) => request<Opportunity>(token, '', json(payload)),
  update: (token: string, id: number, payload: OpportunityPayload) => request<Opportunity>(token, `/${id}`, json(payload, 'PUT')),
  changeStatus: (token: string, id: number, status: OpportunityStatus) => request<Opportunity>(token, `/${id}/status`, json({ status }, 'PATCH')),
  myOpportunities: (token: string, page = 0, size = 50) => request<Paged<Opportunity>>(token, `/my-opportunities${queryString({ page, size })}`),
  save: (token: string, id: number) => request<SavedOpportunity>(token, `/${id}/save`, json({})),
  unsave: (token: string, id: number) => request<void>(token, `/${id}/save`, { method: 'DELETE' }),
  saved: (token: string, page = 0, size = 50) => request<Paged<SavedOpportunity>>(token, `/saved${queryString({ page, size })}`),
  track: (token: string, id: number, status: OpportunityApplicationStatus = 'INTERESTED', notes?: string) => request<OpportunityApplication>(token, `/${id}/track`, json({ status, notes })),
  applications: (token: string, page = 0, size = 50) => request<Paged<OpportunityApplication>>(token, `/applications${queryString({ page, size })}`),
  changeApplicationStatus: (token: string, id: number, status: OpportunityApplicationStatus) => request<OpportunityApplication>(token, `/applications/${id}/status`, json({ status }, 'PATCH')),
  updateApplicationNotes: (token: string, id: number, notes?: string) => request<OpportunityApplication>(token, `/applications/${id}/notes`, json({ notes }, 'PATCH')),
};