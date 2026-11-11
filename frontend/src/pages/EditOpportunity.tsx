import { useEffect, useState } from 'react';
import { OpportunityForm } from '../components/OpportunityForm';
import { useAuth } from '../context/useAuth';
import { opportunityService, type Opportunity, type OpportunityPayload } from '../services/opportunityService';
import './opportunity.css';

type EditOpportunityProps = { id: number; onNavigate: (path: string) => void; onNotify: (message: string) => void };

export function EditOpportunity({ id, onNavigate, onNotify }: EditOpportunityProps) {
  const { token } = useAuth();
  const [opportunity, setOpportunity] = useState<Opportunity>();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    if (!token) { setError('Sign in to edit your opportunity.'); setLoading(false); return; }
    opportunityService.myOpportunities(token, 0, 50).then((page) => {
      if (!active) return;
      const found = page.content.find((item) => item.id === id);
      if (!found) { setError('This opportunity is not available to edit.'); return; }
      if (found.status === 'ARCHIVED') { setError('Archived opportunities cannot be edited.'); return; }
      setOpportunity(found);
    }).catch((loadError: unknown) => { if (active) setError(loadError instanceof Error ? loadError.message : 'Unable to load this opportunity.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [id, token]);

  async function submit(payload: OpportunityPayload) {
    if (!token) return;
    setSaving(true); setError('');
    try {
      await opportunityService.update(token, id, payload);
      onNotify('Opportunity updated.');
      onNavigate('/my-opportunities');
    } catch (saveError) { setError(saveError instanceof Error ? saveError.message : 'Unable to update this opportunity.'); }
    finally { setSaving(false); }
  }

  if (loading) return <main className="opportunity-state opportunity-state--page" role="status"><span className="loading-spinner" />Loading your opportunity...</main>;
  if (!opportunity) return <main className="opportunity-state opportunity-state--page" role="alert"><h1>Opportunity unavailable</h1><p>{error}</p><button className="button button--outline" type="button" onClick={() => onNavigate('/my-opportunities')}>Back to my opportunities</button></main>;
  return <main className="opportunity-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/my-opportunities" onClick={(event) => { event.preventDefault(); onNavigate('/my-opportunities'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a></div></header><section className="opportunity-editor shell"><p className="eyebrow">Manage your listing</p><h1>Edit opportunity</h1><OpportunityForm initial={opportunity} editing saving={saving} error={error} onSubmit={submit} onCancel={() => onNavigate('/my-opportunities')} /></section></main>;
}