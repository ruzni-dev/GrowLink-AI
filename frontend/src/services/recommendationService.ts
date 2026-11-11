import type { MentorshipMode } from './mentorshipService';
import type { PublicTalent } from './talentService';
import type { Opportunity } from './opportunityService';

export type RemotePreference = 'ANY' | 'REMOTE_ONLY' | 'ONSITE_ONLY';
export type RecommendationPreferences = { preferredCategories: string[]; preferredLocations: string[]; interestedSkills: string[]; learningInterests: string[]; learningGoals: string[]; remotePreference: RemotePreference; updatedAt?: string };
export type OpportunityMatch = { opportunity: Opportunity; matchPercentage: number; matchedSkills: string[]; reasons: string[] };
export type MentorMatch = { offerId: number; mentorName: string; profileImage?: string; location?: string; title: string; description: string; category: string; skills: string[]; experience?: string; availability: string; preferredMode: MentorshipMode; matchPercentage: number; matchedSkills: string[]; reasons: string[] };
export type TalentMatch = { talent: PublicTalent; matchPercentage: number; matchedSkills: string[]; reasons: string[] };
export type LearningMatch = { skillArea: string; matchPercentage: number; requiredByOpportunities: number; reasons: string[] };
export type RecommendationDashboard = { personalizedFor: string; opportunities: OpportunityMatch[]; mentors: MentorMatch[]; talents: TalentMatch[]; learningAreas: LearningMatch[] };

const emptyPreferences: RecommendationPreferences = { preferredCategories: [], preferredLocations: [], interestedSkills: [], learningInterests: [], learningGoals: [], remotePreference: 'ANY' };

async function request<T>(token: string, path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`/api/recommendations${path}`, {
    ...options,
    headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}), Authorization: `Bearer ${token}`, ...options.headers },
  });
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.message || 'Unable to load recommendations.');
  return body as T;
}

export const recommendationService = {
  dashboard: (token: string) => request<RecommendationDashboard>(token, '/dashboard'),
  preferences: async (token: string) => (await request<RecommendationPreferences>(token, '/preferences')) || emptyPreferences,
  savePreferences: (token: string, preferences: RecommendationPreferences) => request<RecommendationPreferences>(token, '/preferences', { method: 'PUT', body: JSON.stringify(preferences) }),
};

export const recommendationCategoryOptions: { value: string; label: string }[] = [
  { value: 'JOB', label: 'Jobs' },
  { value: 'INTERNSHIP', label: 'Internships' },
  { value: 'SCHOLARSHIP', label: 'Scholarships' },
  { value: 'TRAINING', label: 'Training' },
  { value: 'VOLUNTEERING', label: 'Volunteering' },
  { value: 'COMPETITION', label: 'Competitions' },
  { value: 'HACKATHON', label: 'Hackathons' },
  { value: 'EVENT', label: 'Events' },
  { value: 'EDUCATION_LEARNING', label: 'Education & Learning' },
  { value: 'TECHNOLOGY_DIGITAL_SKILLS', label: 'Technology & Digital Skills' },
  { value: 'CAREER_PROFESSIONAL_DEVELOPMENT', label: 'Career & Professional Development' },
  { value: 'MENTORSHIP_GUIDANCE', label: 'Mentorship & Guidance' },
  { value: 'SPORTS_COACHING', label: 'Sports & Coaching' },
  { value: 'VOLUNTEERING_SOCIAL_SERVICES', label: 'Volunteering & Social Services' },
  { value: 'ENTREPRENEURSHIP', label: 'Entrepreneurship' },
  { value: 'ARTS_CULTURE', label: 'Arts & Culture' },
  { value: 'OTHER', label: 'Other' },
];