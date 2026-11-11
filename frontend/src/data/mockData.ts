export type Talent = {
  name: string;
  role: string;
  skills: string[];
  location: string;
  description: string;
  initials: string;
  tone: string;
};

export const categories = [
  { icon: '◈', name: 'Education & Learning', description: 'Tutors, trainers and lifelong learning guides.', tone: 'mint' },
  { icon: '⌁', name: 'Technology & Digital Skills', description: 'Builders making digital tools more accessible.', tone: 'blue' },
  { icon: '↗', name: 'Career & Professional Development', description: 'Practical guidance for the next bold move.', tone: 'amber' },
  { icon: '✦', name: 'Mentorship & Guidance', description: 'Experienced voices ready to help you grow.', tone: 'rose' },
  { icon: '◉', name: 'Sports & Coaching', description: 'Coaches who turn consistency into confidence.', tone: 'orange' },
  { icon: '♧', name: 'Volunteering & Social Services', description: 'People-powered action for shared wellbeing.', tone: 'green' },
  { icon: '◇', name: 'Entrepreneurship', description: 'Founders, operators and ideas in motion.', tone: 'violet' },
  { icon: '＋', name: 'Arts & Culture', description: 'Artists and cultural practitioners sharing their craft.', tone: 'cyan' },
];

export const talents: Talent[] = [
  {
    name: 'Amina Rahman',
    role: 'Learning Experience Designer',
    skills: ['Curriculum', 'Facilitation'],
    location: 'Colombo, Sri Lanka',
    description: 'Designs practical learning pathways for young people finding their direction.',
    initials: 'AR',
    tone: 'coral',
  },
  {
    name: 'Daniel Mensah',
    role: 'Community Technology Mentor',
    skills: ['Web Development', 'Mentoring'],
    location: 'Accra, Ghana',
    description: 'Helps community teams use simple technology to turn ideas into impact.',
    initials: 'DM',
    tone: 'navy',
  },
  {
    name: 'Priya Nair',
    role: 'Career Development Coach',
    skills: ['Career Coaching', 'Leadership'],
    location: 'Bengaluru, India',
    description: 'Supports early-career professionals with clarity, confidence and connections.',
    initials: 'PN',
    tone: 'yellow',
  },
];

export const steps = [
  { number: '01', title: 'Discover', description: 'Explore people, skills and opportunities that expand what feels possible.' },
  { number: '02', title: 'Connect', description: 'Bring learners, mentors, organizations and communities closer together.' },
  { number: '03', title: 'Collaborate', description: 'Share knowledge, offer support and work together on what matters.' },
  { number: '04', title: 'Grow', description: 'Build skills, create opportunities and strengthen communities.' },
];
