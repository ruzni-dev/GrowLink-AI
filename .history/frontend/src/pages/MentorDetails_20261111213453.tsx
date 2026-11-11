import { useEffect, useState } from 'react';
import { MentorshipRequestForm } from '../components/MentorshipRequestForm';
import { mentorshipService, type MentorshipOffer } from '../services/mentorshipService';
import './mentorship.css';

type MentorDetailsProps = { id: number; onNavigate: (path: string) => void; onNotify: (message: string) => void };

export function MentorDetails({ id, onNavigate, onNotify }: MentorDetailsProps) {
  const [offer, setOffer] = useState<MentorshipOffer>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    mentorshipService.getOffer(id).then((value) => { if (active) setOffer(value); })
      .catch((loadError: unknown) => { if (active) setError(loadError instanceof Error ? loadError.message : 'This mentorship offer is unavailable.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [id]);

  if (loading) return <main className="mentorship-state mentorship-state--page" role="status"><span className="loading-spinner" />Loading offer...</main>;
  if (error || !offer) return <main className="mentorship-state mentorship-state--page" role="alert"><h1>Offer unavailable</h1><p>{error || 'This offer is no longer available.'}</p><button className="button button--outline" type="button" onClick={() => onNavigate('/mentors')}>Back to mentors</button></main>;

  const initials = offer.mentorName.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase();
  return <main className="mentorship-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/mentors" onClick={(event) => { event.preventDefault(); onNavigate('/mentors'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><button className="button button--outline" type="button" onClick={() => onNavigate('/mentors')}>Back to mentors</button></div></header><div className="shell mentor-details-layout"><article className="mentor-detail"><div className="mentor-detail__mentor"><div className="mentor-detail__avatar">{offer.mentorProfileImage ? <img src={offer.mentorProfileImage} alt="" referrerPolicy="no-referrer" /> : <span>{initials}</span>}</div><div><p className="eyebrow">{offer.categoryLabel}</p><h1>{offer.title}</h1><p>Mentor: {offer.mentorName}{offer.mentorLocation ? ` · ${offer.mentorLocation}` : ''}</p></div></div><div className="mentor-detail__section"><h2>About this mentorship</h2><p>{offer.description}</p></div><div className="mentor-detail__section"><h2>Skills and expertise</h2><div className="mentorship-skill-list">{offer.skills.map((skill) => <span key={skill.id}>{skill.name}{skill.proficiencyLevel ? ` · ${skill.proficiencyLevel.toLowerCase()}` : ''}</span>)}</div>{offer.experience && <p className="mentor-detail__experience">{offer.experience}</p>}</div><div className="mentor-detail__facts"><div><small>Availability</small><strong>{offer.availability}</strong></div><div><small>Mode</small><strong>{offer.preferredMode === 'BOTH' ? 'Online or in person' : offer.preferredMode.replace('_', ' ')}</strong></div>{offer.location && <div><small>Location</small><strong>{offer.location}</strong></div>}</div>{offer.mentorBio && <div className="mentor-detail__section"><h2>Mentor introduction</h2><p>{offer.mentorBio}</p></div>}</article><aside className="mentor-request-aside"><MentorshipRequestForm offerId={offer.id} offerMode={offer.preferredMode} onNavigate={onNavigate} onSubmitted={() => { onNotify('Your mentorship request was sent.'); onNavigate('/my-mentorship'); }} /></aside></div></main>;
}