import { useState } from 'react';
import { OpportunityForm } from '../components/OpportunityForm';
import { useAuth } from '../context/useAuth';
import { opportunityService, type OpportunityPayload } from '../services/opportunityService';
import './opportunity.css';

type CreateOpportunityProps = { onNavigate: (path: string) => void; onNotify: (message: string) => void };

export function CreateOpportunity({ onNavigate, onNotify }: CreateOpportunityProps) {
  const { token, user } = useAuth();
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function submit(payload: OpportunityPayload) {
    if (!token || user?.status !== 'ACTIVE') { setError('An active signed-in account is required to publish an opportunity.'); return; }
    setSaving(true); setError('');
    try {
      const opportunity = await opportunityService.create(token, payload);
      onNotify(opportunity.status === 'DRAFT' ? 'Draft saved.' : 'Opportunity published.');
      onNavigate('/my-opportunities');
    } catch (saveError) { setError(saveError instanceof Error ? saveError.message : 'Unable to save this opportunity.'); }
    finally { setSaving(false); }
  }

  return <main className="opportunity-page"><header className="discovery-nav"><div className="shell discovery-nav__inner"><a className="brand" href="/opportunities" onClick={(event) => { event.preventDefault(); onNavigate('/opportunities'); }}><span className="brand__mark"><span>G</span></span><span className="brand__name">GrowLink <em>AI</em></span></a><button className="button button--outline" type="button" onClick={() => onNavigate('/my-opportunities')}>My opportunities</button></div></header><section className="opportunity-editor shell"><p className="eyebrow">Share an opportunity</p><h1>Create an opportunity</h1><p className="opportunity-editor__intro">List accurate details so the right people can decide whether to apply.</p><OpportunityForm saving={saving} error={error} onSubmit={submit} onCancel={() => onNavigate('/opportunities')} /></section></main>;
}