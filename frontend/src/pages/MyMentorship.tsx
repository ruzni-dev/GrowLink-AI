import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../context/useAuth';
import { MentorshipOfferCard } from '../components/MentorshipOfferCard';
import { MentorshipRequestCard } from '../components/MentorshipRequestCard';
import { mentorshipService, type MentorshipOffer, type MentorshipRequest } from '../services/mentorshipService';
import './mentorship.css';

type MyMentorshipProps = { onNavigate: (path: string) => void; onNotify: (message: string) => void };
type Tab = 'received' | 'sent' | 'offers' | 'active' | 'completed';
const tabs: { value: Tab; label: string }[] = [{ value: 'received', label: 'Received requests' }, { value: 'sent', label: 'Sent requests' }, { value: 'offers', label: 'My offers' }, { value: 'active', label: 'Active mentorships' }, { value: 'completed', label: 'Completed' }];

export function MyMentorship({ onNavigate, onNotify }: MyMentorshipProps) {
  const { token } = useAuth();
  const [tab, setTab] = useState<Tab>('received');
  const [received, setReceived] = useState<MentorshipRequest[]>([]);
  const [sent, setSent] = useState<MentorshipRequest[]>([]);
  const [offers, setOffers] = useState<MentorshipOffer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    if (!token) return;
    setLoading(true); setError('');
    try {
      const [inbox, outbox, ownOffers] = await Promise.all([
        mentorshipService.receivedRequests(token, 0, 50),
        mentorshipService.sentRequests(token, 0, 50),
        mentorshipService.myOffers(token, 0, 50),
      ]);
      setReceived(inbox.content); setSent(outbox.content); setOffers(ownOffers.content);
    } catch (loadError) { setError(loadError instanceof Error ? loadError.message : 'Unable to load your mentorship activity.'); }
    finally { setLoading(false); }
  }, [token]);

  useEffect(() => { void load(); }, [load]);

  async function action(id: number, type: 'accept' | 'reject' | 'cancel' | 'complete') {
    if (!token) return;
    try {
      if (type === 'accept') await mentorshipService.acceptRequest(token, id);
      if (type === 'reject') await mentorshipService.rejectRequest(token, id);
      if (type === 'cancel') await mentorshipService.cancelRequest(token, id);
      if (type === 'complete') await mentorshipService.completeRequest(token, id);
      onNotify(`Mentorship request ${type === 'complete' ? 'completed' : `${type}ed`}.`);
      await load();
    } catch (actionError) { onNotify(actionError instanceof Error ? actionError.message : 'That action could not be completed.'); }
  }

  async function changeOfferStatus(id: number, status: 'ACTIVE' | 'PAUSED' | 'CLOSED') {
    if (!token) return;
    try { await mentorshipService.changeOfferStatus(token, id, status); onNotify(`Offer ${status.toLowerCase()}.`); await load(); }
    catch (statusError) { onNotify(statusError instanceof Error ? statusError.message : 'Offer status could not be updated.'); }
  }

  const active = [...received, ...sent].filter((request) => request.status === 'ACCEPTED');
  const completed = [...received, ...sent].filter((request) => request.status === 'COMPLETED');
  const visibleRequests = tab === 'received' ? received : tab === 'sent' ? sent : tab === 'active' ? active : completed;

  return <main className="mentorship-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/mentors" onClick={(event) => { event.preventDefault(); onNavigate('/mentors'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><nav aria-label="Mentorship navigation"><a href="/mentors" onClick={(event) => { event.preventDefault(); onNavigate('/mentors'); }}>Find a mentor</a><a href="/mentorship/create" onClick={(event) => { event.preventDefault(); onNavigate('/mentorship/create'); }}>Offer mentorship</a></nav></div></header><div className="shell my-mentorship"><div className="my-mentorship__heading"><div><p className="eyebrow">Your connections</p><h1>My mentorship</h1><p>Manage your requests, offers and ongoing mentorships.</p></div><button className="button button--primary" type="button" onClick={() => onNavigate('/mentorship/create')}>Create an offer</button></div><div className="mentorship-tabs" role="tablist" aria-label="My mentorship sections">{tabs.map((item) => <button key={item.value} type="button" role="tab" aria-selected={tab === item.value} className={tab === item.value ? 'mentorship-tab mentorship-tab--active' : 'mentorship-tab'} onClick={() => setTab(item.value)}>{item.label}</button>)}</div>{loading && <div className="mentorship-state" role="status"><span className="loading-spinner" />Loading your activity...</div>}{!loading && error && <div className="mentorship-state mentorship-state--error" role="alert"><h2>Activity could not be loaded</h2><p>{error}</p><button className="button button--outline" type="button" onClick={() => void load()}>Try again</button></div>}{!loading && !error && tab === 'offers' && <div className="mentorship-offer-grid mentorship-offer-grid--managed">{offers.map((offer) => <MentorshipOfferCard key={offer.id} offer={offer} owned onView={(id) => onNavigate(`/mentors/${id}`)} onStatusChange={changeOfferStatus} />)}{!offers.length && <div className="mentorship-state"><h2>No offers yet</h2><p>Share your skills and experience with the community.</p><button className="button button--primary" type="button" onClick={() => onNavigate('/mentorship/create')}>Create your first offer</button></div>}</div>}{!loading && !error && tab !== 'offers' && <div className="mentorship-request-list">{visibleRequests.map((request) => <MentorshipRequestCard key={`${tab}-${request.id}`} request={request} direction={tab === 'received' || tab === 'active' ? 'received' : 'sent'} onAction={(id, type) => void action(id, type)} />)}{!visibleRequests.length && <div className="mentorship-state"><h2>{tab === 'received' ? 'No incoming requests' : tab === 'sent' ? 'No requests sent yet' : tab === 'active' ? 'No active mentorships' : 'Nothing completed yet'}</h2><p>{tab === 'sent' ? 'Explore available mentors and send a thoughtful request.' : 'Your mentorship activity will appear here.'}</p>{tab === 'sent' && <button className="button button--outline" type="button" onClick={() => onNavigate('/mentors')}>Find a mentor</button>}</div>}</div>}</div></main>;
}