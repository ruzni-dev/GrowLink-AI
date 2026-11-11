import { useState } from 'react';
import type { OpportunityApplication, OpportunityApplicationStatus } from '../services/opportunityService';

type OpportunityApplicationCardProps = { application: OpportunityApplication; onStatusChange: (id: number, status: OpportunityApplicationStatus) => Promise<void>; onNotesSave: (id: number, notes: string) => Promise<void> };

export function OpportunityApplicationCard({ application, onStatusChange, onNotesSave }: OpportunityApplicationCardProps) {
  const [notes, setNotes] = useState(application.notes || '');
  const [savingNotes, setSavingNotes] = useState(false);
  async function saveNotes() {
    setSavingNotes(true);
    try { await onNotesSave(application.id, notes); } finally { setSavingNotes(false); }
  }
  return <article className="opportunity-application-card"><div className="opportunity-application-card__heading"><div><span className={`opportunity-category opportunity-category--${application.opportunity.category.toLowerCase()}`}>{application.opportunity.category}</span><h2>{application.opportunity.title}</h2><p>{application.opportunity.organizationName}</p></div><label className="opportunity-field"><span>Status</span><select value={application.status} onChange={(event) => void onStatusChange(application.id, event.target.value as OpportunityApplicationStatus)}>{['INTERESTED', 'APPLIED', 'SHORTLISTED', 'REJECTED', 'ACCEPTED', 'WITHDRAWN'].map((status) => <option key={status} value={status}>{status.toLowerCase()}</option>)}</select></label></div><div className="opportunity-application-card__dates"><span>{application.appliedAt ? `Applied ${new Date(application.appliedAt).toLocaleDateString()}` : 'Not marked as applied'}</span><span>Updated {new Date(application.updatedAt).toLocaleDateString()}</span></div><label className="opportunity-field"><span>Private notes</span><textarea rows={3} maxLength={4000} value={notes} onChange={(event) => setNotes(event.target.value)} placeholder="Keep your own application notes" /></label><button className="button button--outline" type="button" disabled={savingNotes || notes === (application.notes || '')} onClick={() => void saveNotes()}>{savingNotes ? 'Saving...' : 'Save notes'}</button></article>;
}