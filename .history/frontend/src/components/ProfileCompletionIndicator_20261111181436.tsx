import type { CSSProperties } from 'react';

type ProfileCompletionIndicatorProps = { percentage: number };

export function ProfileCompletionIndicator({ percentage }: ProfileCompletionIndicatorProps) {
  return <section className="profile-completion"><div className="profile-completion__top"><div><p className="eyebrow">Profile strength</p><h2>{percentage}% complete</h2></div><span className="profile-completion__ring" style={{ '--completion': `${percentage * 3.6}deg` } as CSSProperties}>{percentage}%</span></div><div className="profile-completion__bar"><span style={{ width: `${percentage}%` }} /></div><p>Add the missing pieces to help your future connections understand how you can contribute.</p></section>;
}
