import { useEffect, useState } from 'react';
import { talentService, type PublicTalent } from '../services/talentService';

type TalentProfileProps = { id: number; onNavigate: (path: string) => void };

export function TalentProfile({ id, onNavigate }: TalentProfileProps) {
  const [profile, setProfile] = useState<PublicTalent>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    talentService.getProfile(id)
      .then((result) => { if (active) setProfile(result); })
      .catch((loadError: unknown) => { if (active) setError(loadError instanceof Error ? loadError.message : 'This public profile is unavailable.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [id]);

  if (loading) return <main className="discovery-state discovery-state--page" role="status"><span className="loading-spinner" />Loading public profile...</main>;
  if (error || !profile) return <main className="discovery-state discovery-state--page discovery-state--error" role="alert"><h1>Profile unavailable</h1><p>{error || 'This public profile could not be found.'}</p><button className="button button--outline" type="button" onClick={() => onNavigate('/talents')}>Back to discovery</button></main>;

  return <main className="talent-profile"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/talents" onClick={(event) => { event.preventDefault(); onNavigate('/talents'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><a className="button button--outline" href="/talents" onClick={(event) => { event.preventDefault(); onNavigate('/talents'); }}>Back to discovery</a></div></header><div className="shell talent-profile__content"><section className="talent-profile__hero"><div className="talent-profile__portrait">{profile.profileImage ? <img src={profile.profileImage} alt="" referrerPolicy="no-referrer" /> : <span>{profile.fullName.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase()}</span>}</div><div><p className="eyebrow">Public profile</p><h1>{profile.fullName}</h1><p className="talent-profile__location">{[profile.location, profile.country].filter(Boolean).join(', ') || 'Location not listed'}</p><p className="talent-profile__intro">{profile.bio || 'No introduction has been added yet.'}</p></div></section><div className="talent-profile__sections"><section className="talent-profile__section"><p className="eyebrow">01 / Expertise</p><h2>Skills and strengths</h2>{profile.skills.length ? <div className="public-skill-list">{profile.skills.map((skill) => <article key={skill.name}><div><h3>{skill.name}</h3><p>{skill.category || 'Community skill'}</p></div><span>{skill.proficiencyLevel.toLowerCase()} · {skill.yearsOfExperience} {skill.yearsOfExperience === 1 ? 'year' : 'years'}</span>{skill.description && <small>{skill.description}</small>}</article>)}</div> : <p className="public-empty">No skills have been shared yet.</p>}</section><section className="talent-profile__section"><p className="eyebrow">02 / Learning</p><h2>Education</h2>{profile.education.length ? <div className="public-timeline">{profile.education.map((item) => <article key={item.id}><h3>{item.qualification}{item.fieldOfStudy ? ` · ${item.fieldOfStudy}` : ''}</h3><p>{item.institutionName}</p><small>{item.startYear ?? ''}{item.startYear && (item.endYear || item.currentlyStudying) ? '–' : ''}{item.currentlyStudying ? 'Present' : item.endYear ?? ''}</small>{item.description && <p>{item.description}</p>}</article>)}</div> : <p className="public-empty">No education has been shared yet.</p>}</section><section className="talent-profile__section"><p className="eyebrow">03 / Experience</p><h2>Work and service</h2>{profile.workExperience.length ? <div className="public-timeline">{profile.workExperience.map((item) => <article key={item.id}><h3>{item.jobTitle}</h3><p>{item.organization}{item.employmentType ? ` · ${item.employmentType}` : ''}</p><small>{item.startDate ?? ''}{item.startDate && (item.endDate || item.currentlyWorking) ? '–' : ''}{item.currentlyWorking ? 'Present' : item.endDate ?? ''}</small>{item.description && <p>{item.description}</p>}</article>)}</div> : <p className="public-empty">No work experience has been shared yet.</p>}</section><section className="talent-profile__section"><p className="eyebrow">04 / Interests</p><h2>What matters to them</h2>{profile.interests.length ? <div className="public-interest-list">{profile.interests.map((interest) => <span key={interest.id}>{interest.name}</span>)}</div> : <p className="public-empty">No interests have been shared yet.</p>}</section></div></div></main>;
}