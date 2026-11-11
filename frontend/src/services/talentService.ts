export type TalentCategory = { value: string; label: string };
export type PublicSkill = { name: string; category?: string; proficiencyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT'; yearsOfExperience: number; description?: string };
export type PublicEducation = { id: number; institutionName: string; qualification: string; fieldOfStudy?: string; startYear?: number; endYear?: number; currentlyStudying: boolean; description?: string };
export type PublicExperience = { id: number; jobTitle: string; organization: string; employmentType?: string; startDate?: string; endDate?: string; currentlyWorking: boolean; description?: string };
export type PublicInterest = { id: number; name: string };
export type PublicTalent = { id: number; fullName: string; profileImage?: string; bio?: string; location?: string; country?: string; education: PublicEducation[]; skills: PublicSkill[]; workExperience: PublicExperience[]; interests: PublicInterest[] };
export type TalentPage = { content: PublicTalent[]; totalElements: number; totalPages: number; number: number; size: number; first: boolean; last: boolean };
export type TalentSearchParams = { name?: string; skill?: string; category?: string; location?: string; proficiencyLevel?: string; minimumExperience?: number | string; education?: string; page?: number; size?: number; sortBy?: string };

export const TALENT_CATEGORY_OPTIONS: TalentCategory[] = [
  { value: 'EDUCATION_LEARNING', label: 'Education & Learning' },
  { value: 'TECHNOLOGY_DIGITAL_SKILLS', label: 'Technology & Digital Skills' },
  { value: 'CAREER_PROFESSIONAL_DEVELOPMENT', label: 'Career & Professional Development' },
  { value: 'MENTORSHIP_GUIDANCE', label: 'Mentorship & Guidance' },
  { value: 'SPORTS_COACHING', label: 'Sports & Coaching' },
  { value: 'VOLUNTEERING_SOCIAL_SERVICES', label: 'Volunteering & Social Services' },
  { value: 'ENTREPRENEURSHIP', label: 'Entrepreneurship' },
  { value: 'ARTS_CULTURE', label: 'Arts & Culture' },
];

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`/api/talents${path}`);
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.message || 'Unable to load public talent profiles.');
  return body as T;
}

function searchPath(params: TalentSearchParams): string {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') query.set(key, String(value));
  });
  return `/search${query.size ? `?${query.toString()}` : ''}`;
}

export const talentService = {
  search: (params: TalentSearchParams = {}) => request<TalentPage>(searchPath(params)),
  getProfile: (id: number) => request<PublicTalent>(`/${id}`),
  getCategories: () => request<TalentCategory[]>('/categories'),
};