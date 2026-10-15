import { describe, expect, it } from 'vitest';
import { bestTimelineEra, eraCompatibility } from '../src/components/results/TimelineSection';
import type { CountryMatch, TimelineEra } from '../src/types/quiz';

const country = (compatibility: number) =>
  ({ type: 'country', personality: null, party: null, country: { compatibility } as CountryMatch }) as const;

const era = (key: TimelineEra['key'], compatibility: number | undefined, entryValues: number[]): TimelineEra =>
  ({ key, fromYear: null, toYear: null, compatibility, entries: entryValues.map(country) }) as unknown as TimelineEra;

describe('timeline era compatibility', () => {
  it('uses the compatibility computed by the backend', () => {
    expect(eraCompatibility(era('xx', 86.2, [90, 80]))).toBe(86.2);
  });

  it('falls back to the average of the shown profiles when the API has no era compatibility', () => {
    expect(eraCompatibility(era('xx', undefined, [90, 80]))).toBe(85);
    expect(eraCompatibility(era('xx', Number.NaN, [90, 70]))).toBe(80);
  });

  it('never returns NaN, even for an era without profiles', () => {
    expect(eraCompatibility(era('xx', undefined, []))).toBe(0);
  });

  it('picks the era with the highest compatibility, the oldest on a tie', () => {
    const eras = [era('antiguidade', 70, [70]), era('medieval', 85, [85]), era('moderna', 85, [85]), era('xx', 60, [60])];
    expect(bestTimelineEra(eras)?.key).toBe('medieval');
    expect(bestTimelineEra([era('xx', undefined, [])])).toBeNull();
  });
});
