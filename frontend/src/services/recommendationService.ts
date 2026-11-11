import type { MentorshipMode } from './mentorshipService';
import type { PublicTalent } from './talentService';
import type { Opportunity, OpportunityCategory, OpportunityCategoryOption } from './opportunityService';

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

export const recommendationCategoryOptions: OpportunityCategoryOption[] = [
  { value: 'JOB' as OpportunityCategory, label: 'Jobs' },
  { value: 'INTERNSHIP' as OpportunityCategory, label: 'Internships' },
  { value: 'SCHOLARSHIP' as OpportunityCategory, label: 'Scholarships' },
  { value: 'TRAINING' as OpportunityCategory, label: 'Training' },
  { value: 'VOLUNTEERING' as OpportunityCategory, label: 'Volunteering' },
  { value: 'COMPETITION' as OpportunityCategory, label: 'Competitions' },
  { value: 'HACKATHON' as OpportunityCategory, label: 'Hackathons' },
  { value: 'EVENT' as OpportunityCategory, label: 'Events' },
  { value: 'EDUCATION_LEARNING' as OpportunityCategory, label: 'Education & Learning' },
  { value: 'TECHNOLOGY_DIGITAL_SKILLS' as OpportunityCategory, label: 'Technology & Digital Skills' },
  { value: 'CAREER_PROFESSIONAL_DEVELOPMENT' as OpportunityCategory, label: 'Career & Professional Development' },
  { value: 'MENTORSHIP_GUIDANCE' as OpportunityCategory, label: 'Mentorship & Guidance' },
  { value: 'SPORTS_COACHING' as OpportunityCategory, label: 'Sports & Coaching' },
  { value: 'VOLUNTEERING_SOCIAL_SERVICES' as OpportunityCategory, label: 'Volunteering & Social Services' },
  { value: 'ENTREPRENEURSHIP' as OpportunityCategory, label: 'Entrepreneurship' },
  { value: 'ARTS_CULTURE' as OpportunityCategory, label: 'Arts & Culture' },
  { value: 'OTHER' as OpportunityCategory, label: 'Other' },
];