import { useCallback, useEffect, useState, type FormEvent, type ReactNode } from 'react';
import { useAuth } from '../context/useAuth';
import { recommendationCategoryOptions, recommendationService, type RecommendationDashboard as DashboardData, type RecommendationPreferences } from '../services/recommendationService';
import './recommendations.css';

type RecommendationDashboardProps = { onNavigate: (path: string) => void; onNotify: (message: string) => void };
type PreferenceForm = Omit<RecommendationPreferences, 'updatedAt'>;
const emptyPreferences: PreferenceForm = { preferredCategories: [], preferredLocations: [], interestedSkills: [], learningInterests: [], learningGoals: [], remotePreference: 'ANY' };

function parseList(value: string) { return [...new Set(value.split(',').map((item) => item.trim()).filter(Boolean))]; }
function csv(values: string[]) { return values.join(', '); }

export function RecommendationDashboard({ onNavigate, onNotify }: RecommendationDashboardProps) {
  const { token, user } = useAuth();
  const [dashboard, setDashboard] = useState<DashboardData>();
  const [preferences, setPreferences] = useState<PreferenceForm>(emptyPreferences);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [preferencesOpen, setPreferencesOpen] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    if (!token) return;
    setLoading(true); setError('');
    try {
      const [nextDashboard, nextPreferences] = await Promise.all([
        recommendationService.dashboard(token), recommendationService.preferences(token),
      ]);
      setDashboard(nextDashboard);
      setPreferences({ preferredCategories: nextPreferences.preferredCategories, preferredLocations: nextPreferences.preferredLocations, interestedSkills: nextPreferences.interestedSkills, learningInterests: nextPreferences.learningInterests, learningGoals: nextPreferences.learningGoals, remotePreference: nextPreferences.remotePreference });
    } catch (loadError) { setError(loadError instanceof Error ? loadError.message : 'Unable to load your recommendations.'); }
    finally { setLoading(false); }
  }, [token]);

  useEffect(() => { void load(); }, [load]);

  async function savePreferences(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token) return;
    setSaving(true); setError('');
    try {
      await recommendationService.savePreferences(token, preferences);
      setPreferencesOpen(false);
      onNotify('Recommendation preferences saved.');
      await load();
    } catch (saveError) { setError(saveError instanceof Error ? saveError.message : 'Unable to save preferences.'); }
    finally { setSaving(false); }
  }

  function setList(key: keyof Pick<PreferenceForm, 'preferredLocations' | 'interestedSkills' | 'learningInterests' | 'learningGoals'>, value: string) {
    setPreferences((current) => ({ ...current, [key]: parseList(value) }));
  }

  if (loading) return <main className="recommendation-page recommendation-state" role="status"><span className="loading-spinner" />Building your recommendations...</main>;
  if (error && !dashboard) return <main className="recommendation-page recommendation-state" role="alert"><h1>Recommendations unavailable</h1><p>{error}</p><button className="button button--outline" type="button" onClick={() => void load()}>Try again</button></main>;

  return <main className="recommendation-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/dashboard" onClick={(event) => { event.preventDefault(); onNavigate('/dashboard'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><nav aria-label="Recommendation navigation"><a href="/opportunities" onClick={(event) => { event.preventDefault(); onNavigate('/opportunities'); }}>Opportunities</a><a href="/mentors" onClick={(event) => { event.preventDefault(); onNavigate('/mentors'); }}>Mentors</a><a href="/talents" onClick={(event) => { event.preventDefault(); onNavigate('/talents'); }}>Talent</a></nav></div></header><section className="recommendation-hero"><div className="shell recommendation-hero__inner"><div><p className="eyebrow">A clearer next step</p><h1>Recommendations<br /><span>for {dashboard?.personalizedFor || user?.fullName || 'you'}.</span></h1><p>Relevant connections and opportunities, with the reasons behind each match.</p></div><button className="button button--light" type="button" onClick={() => setPreferencesOpen(!preferencesOpen)}>{preferencesOpen ? 'Close preferences' : 'Edit preferences'}</button></div></section><div className="shell recommendation-content">{error && <p className="recommendation-error" role="alert">{error}</p>}{preferencesOpen && <form className="recommendation-preferences" onSubmit={(event) => void savePreferences(event)}><div className="recommendation-section-heading"><div><p className="eyebrow">Personalize your feed</p><h2>Recommendation preferences</h2></div></div><fieldset className="recommendation-category-options"><legend>Preferred categories</legend>{recommendationCategoryOptions.map((option) => <label key={option.value}><input type="checkbox" checked={preferences.preferredCategories.includes(option.value)} onChange={(event) => setPreferences((current) => ({ ...current, preferredCategories: event.target.checked ? [...current.preferredCategories, option.value] : current.preferredCategories.filter((item) => item !== option.value) }))} /><span>{option.label}</span></label>)}</fieldset><div className="recommendation-form-grid"><label className="recommendation-field"><span>Preferred locations</span><input value={csv(preferences.preferredLocations)} onChange={(event) => setList('preferredLocations', event.target.value)} placeholder="Colombo, Remote" /></label><label className="recommendation-field"><span>Interested skills</span><input value={csv(preferences.interestedSkills)} onChange={(event) => setList('interestedSkills', event.target.value)} placeholder="Java, facilitation" /></label><label className="recommendation-field"><span>Learning interests</span><input value={csv(preferences.learningInterests)} onChange={(event) => setList('learningInterests', event.target.value)} placeholder="Education, technology" /></label><label className="recommendation-field"><span>Learning goals</span><input value={csv(preferences.learningGoals)} onChange={(event) => setList('learningGoals', event.target.value)} placeholder="Build a portfolio, mentor others" /></label><label className="recommendation-field"><span>Remote preference</span><select value={preferences.remotePreference} onChange={(event) => setPreferences({ ...preferences, remotePreference: event.target.value as PreferenceForm['remotePreference'] })}><option value="ANY">Any location</option><option value="REMOTE_ONLY">Remote only</option><option value="ONSITE_ONLY">In person only</option></select></label></div><div className="recommendation-preferences__actions"><button className="button button--primary" type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save preferences'}</button></div></form>}
      <RecommendationSection eyebrow="Opportunities" title="Worth exploring" empty="No current opportunities match the profile and preferences available yet." onBrowse={() => onNavigate('/opportunities')} browseLabel="Browse opportunities">{dashboard?.opportunities.map((item) => <article className="recommendation-card" key={item.opportunity.id}><div className="recommendation-card__top"><span className="recommendation-category">{item.opportunity.category}</span><MatchScore value={item.matchPercentage} /></div><h3>{item.opportunity.title}</h3><p className="recommendation-card__subtitle">{item.opportunity.organizationName} · {item.opportunity.remote ? 'Remote' : item.opportunity.location || 'On-site'}</p><MatchedSkills skills={item.matchedSkills} /><ReasonList reasons={item.reasons} /><button className="button button--outline" type="button" onClick={() => onNavigate(`/opportunities/${item.opportunity.id}`)}>View opportunity</button></article>)}</RecommendationSection>
      <RecommendationSection eyebrow="Mentorship" title="People to learn from" empty="No active public mentorship offers match the information currently available." onBrowse={() => onNavigate('/mentors')} browseLabel="Explore mentors">{dashboard?.mentors.map((item) => <article className="recommendation-card" key={item.offerId}><div className="recommendation-card__top"><span className="recommendation-category">{item.category.replaceAll('_', ' ')}</span><MatchScore value={item.matchPercentage} /></div><h3>{item.title}</h3><p className="recommendation-card__subtitle">{item.mentorName} · {item.location || 'Community mentor'}</p><MatchedSkills skills={item.matchedSkills} /><ReasonList reasons={item.reasons} /><button className="button button--outline" type="button" onClick={() => onNavigate(`/mentors/${item.offerId}`)}>View mentor</button></article>)}</RecommendationSection>
      <RecommendationSection eyebrow="Community" title="People with relevant experience" empty="No public active profiles currently match your skills and interests." onBrowse={() => onNavigate('/talents')} browseLabel="Discover talent">{dashboard?.talents.map((item) => <article className="recommendation-card" key={item.talent.id}><div className="recommendation-card__top"><span className="recommendation-category">{item.talent.location || 'GrowLink member'}</span><MatchScore value={item.matchPercentage} /></div><h3>{item.talent.fullName}</h3><p className="recommendation-card__subtitle">{item.talent.skills.slice(0, 3).map((skill) => skill.name).join(' · ') || 'Community member'}</p><MatchedSkills skills={item.matchedSkills} /><ReasonList reasons={item.reasons} /><button className="button button--outline" type="button" onClick={() => onNavigate(`/talents/${item.talent.id}`)}>View talent</button></article>)}</RecommendationSection>
      <RecommendationSection eyebrow="Learning areas" title="Skills worth building" empty="No skill gaps were identified from current opportunity requirements." onBrowse={() => onNavigate('/opportunities?category=TRAINING')} browseLabel="Find training">{dashboard?.learningAreas.map((item) => <article className="recommendation-card recommendation-card--learning" key={item.skillArea}><div className="recommendation-card__top"><span className="recommendation-category">Skill priority</span><MatchScore value={item.matchPercentage} /></div><h3>{item.skillArea}</h3><p className="recommendation-card__subtitle">Required by {item.requiredByOpportunities} current {item.requiredByOpportunities === 1 ? 'opportunity' : 'opportunities'}</p><ReasonList reasons={item.reasons} /><button className="button button--outline" type="button" onClick={() => onNavigate(`/opportunities?category=TRAINING&q=${encodeURIComponent(item.skillArea)}`)}>Explore learning area</button></article>)}</RecommendationSection>
      <p className="recommendation-disclaimer">Match scores describe profile relevance based on available information. They are not eligibility checks or guarantees of selection.</p>
    </div></main>;
}

function RecommendationSection({ eyebrow, title, empty, onBrowse, browseLabel, children }: { eyebrow: string; title: string; empty: string; onBrowse: () => void; browseLabel: string; children: ReactNode }) {
  const content = Array.isArray(children) ? children.filter(Boolean) : children;
  const hasContent = Array.isArray(content) ? content.length > 0 : Boolean(content);
  return <section className="recommendation-section"><div className="recommendation-section-heading"><div><p className="eyebrow">{eyebrow}</p><h2>{title}</h2></div><button className="recommendation-text-button" type="button" onClick={onBrowse}>{browseLabel} →</button></div>{hasContent ? <div className="recommendation-grid">{children}</div> : <div className="recommendation-empty"><p>{empty}</p><button className="button button--outline" type="button" onClick={onBrowse}>{browseLabel}</button></div>}</section>;
}

function MatchScore({ value }: { value: number }) {
  return <span className="recommendation-score" aria-label={`${value}% match`}><strong>{value}%</strong><small>match</small></span>;
}

function MatchedSkills({ skills }: { skills: string[] }) {
  return skills.length ? <div className="recommendation-skills">{skills.map((skill) => <span key={skill}>{skill}</span>)}</div> : null;
}

function ReasonList({ reasons }: { reasons: string[] }) {
  return <ul className="recommendation-reasons">{reasons.map((reason) => <li key={reason}>{reason}</li>)}</ul>;
}