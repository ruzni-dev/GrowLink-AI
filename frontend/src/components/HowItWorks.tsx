import { steps } from '../data/mockData';
import { SectionHeading } from './SectionHeading';

export function HowItWorks() {
  return (
    <section className="section how-section" id="about">
      <div className="shell">
        <SectionHeading eyebrow="The GrowLink way" title="Turning community potential into real impact" description="A simpler way to find the right people, make a meaningful connection and keep momentum moving." align="center" />
        <div className="steps-grid">
          {steps.map((step, index) => <div className="step" key={step.number}><div className="step__number">{step.number}</div><div className="step__line">{index < steps.length - 1 && <span />}</div><h3>{step.title}</h3><p>{step.description}</p></div>)}
        </div>
      </div>
    </section>
  );
}
