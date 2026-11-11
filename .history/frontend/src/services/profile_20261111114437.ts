export type Profile = {
  id: number;
  fullName: string;
  email: string;
  profileImage?: string;
  bio?: string;
  dateOfBirth?: string;
  phone?: string;
  location?: string;
  country?: string;
  website?: string;
  linkedinUrl?: string;
  githubUrl?: string;
  createdAt: string;
  updatedAt: string;
};

export type Education = { id: number; institutionName: string; qualification: string; fieldOfStudy?: string; startYear?: number; endYear?: number; currentlyStudying: boolean; description?: string };
export type Skill = { id: number; skillId: number; name: string; skillDescription?: string; category?: string; proficiencyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT'; yearsOfExperience: number; description?: string };
export type Experience = { id: number; jobTitle: string; organization: string; employmentType?: string; startDate?: string; endDate?: string; currentlyWorking: boolean; description?: string };
export type Interest = { id: number; name: string };

async function request<T>(token: string, path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`/api/profiles${path}`, { ...options, headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}`, ...(options.headers || {}) } });
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.message || Object.values(body?.fieldErrors || {})[0] || 'Unable to save profile changes.');
  return body as T;
}

export const profileApi = {
  getProfile: (token: string) => request<Profile>(token, '/me'),
  updateProfile: (token: string, data: Partial<Profile>) => request<Profile>(token, '/me', { method: 'PUT', body: JSON.stringify(data) }),
  getEducation: (token: string) => request<Education[]>(token, '/me/education'),
  addEducation: (token: string, data: Omit<Education, 'id'>) => request<Education>(token, '/me/education', { method: 'POST', body: JSON.stringify(data) }),
  updateEducation: (token: string, id: number, data: Omit<Education, 'id'>) => request<Education>(token, `/me/education/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteEducation: (token: string, id: number) => request<{ message: string }>(token, `/me/education/${id}`, { method: 'DELETE' }),
  getSkills: (token: string) => request<Skill[]>(token, '/me/skills'),
  addSkill: (token: string, data: Omit<Skill, 'id' | 'skillId' | 'name'> & { name: string }) => request<Skill>(token, '/me/skills', { method: 'POST', body: JSON.stringify(data) }),
  updateSkill: (token: string, id: number, data: Omit<Skill, 'id' | 'skillId' | 'name'> & { name: string }) => request<Skill>(token, `/me/skills/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteSkill: (token: string, id: number) => request<{ message: string }>(token, `/me/skills/${id}`, { method: 'DELETE' }),
  getExperience: (token: string) => request<Experience[]>(token, '/me/experience'),
  addExperience: (token: string, data: Omit<Experience, 'id'>) => request<Experience>(token, '/me/experience', { method: 'POST', body: JSON.stringify(data) }),
  updateExperience: (token: string, id: number, data: Omit<Experience, 'id'>) => request<Experience>(token, `/me/experience/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteExperience: (token: string, id: number) => request<{ message: string }>(token, `/me/experience/${id}`, { method: 'DELETE' }),
  getInterests: (token: string) => request<Interest[]>(token, '/interests'),
  getUserInterests: (token: string) => request<Interest[]>(token, '/me/interests'),
  updateInterests: (token: string, interestIds: number[]) => request<Interest[]>(token, '/me/interests', { method: 'PUT', body: JSON.stringify({ interestIds }) }),
};
