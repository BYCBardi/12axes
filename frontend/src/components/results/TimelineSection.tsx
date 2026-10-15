import { useState } from 'react';
import { t } from '../../i18n';
import type { Axis, AxisResult, CountryMatch, PersonalityMatch, TimelineEntry, TimelineEra } from '../../types/quiz';
import { resolveCountryFlagSrc } from '../../utils/countryFlags';
import { personalityInitials, resolvePersonalityImageSrc } from '../../utils/personalityImage';
import { SafeImg } from '../editorial/primitives';
import { CountryInfoSheet, flagAlt } from './CountriesSection';
import { PersonalityInfoSheet } from './PersonalitiesSection';

interface TimelineSectionProps {
  timeline: TimelineEra[];
  axes: Axis[];
  results: Map<string, AxisResult>;
}

type Selected = { type: 'personality'; match: PersonalityMatch } | { type: 'country'; match: CountryMatch };

interface EntryView {
  key: string;
  name: string;
  /** Data: vida da personalidade ou período do país (países atuais, "Atual"). */
  date: string;
  photo: JSX.Element;
  selected: Selected;
}

/** Dados de exibição de um perfil da linha do tempo (compartilhados com o relatório em PDF). */
export interface TimelineEntryInfo {
  key: string;
  name: string;
  /** Vida da personalidade, período do país histórico ou "Atual". */
  date: string;
  /** Retrato ou bandeira já resolvido, para <img>. */
  imageSrc: string;
}

export function timelineEntryInfo(entry: TimelineEntry): TimelineEntryInfo | null {
  if (entry.type === 'personality' && entry.personality) {
    const match = entry.personality;
    return {
      key: `p-${match.personalityId}`,
      name: match.name,
      date: match.lifespan,
      imageSrc: resolvePersonalityImageSrc(match.imagePath)
    };
  }
  if (entry.type === 'country' && entry.country) {
    const match = entry.country;
    return {
      key: `c-${match.countryId}`,
      name: match.name,
      date: match.historical && match.period ? match.period : t.timelineCurrent,
      imageSrc: resolveCountryFlagSrc(match.flagPath)
    };
  }
  return null;
}

function viewOf(entry: TimelineEntry): EntryView | null {
  const info = timelineEntryInfo(entry);
  if (!info) return null;
  if (entry.type === 'personality' && entry.personality) {
    const match = entry.personality;
    return {
      key: info.key,
      name: info.name,
      date: info.date,
      photo: (
        <SafeImg
          className="e-tl-photo"
          src={resolvePersonalityImageSrc(match.imagePath)}
          alt={t.portraitAlt(match.name)}
          fallback={personalityInitials(match.name)}
        />
      ),
      selected: { type: 'personality', match }
    };
  }
  if (entry.type === 'country' && entry.country) {
    const match = entry.country;
    return {
      key: info.key,
      name: info.name,
      date: info.date,
      photo: <SafeImg className="e-tl-photo e-tl-flag" src={resolveCountryFlagSrc(match.flagPath)} alt={flagAlt(match)} fallback="" />,
      selected: { type: 'country', match }
    };
  }
  return null;
}

// Linha do tempo das ideias: em cada era, os 4 perfis (personalidades e países juntos) mais
// compatíveis, só com foto, nome e data. No desktop as eras formam um quadro completo, sem
// paginação; no mobile viram uma linha vertical contínua.
export function TimelineSection({ timeline, axes, results }: TimelineSectionProps) {
  const [info, setInfo] = useState<Selected | null>(null);

  return (
    <section className="e-panel e-tl" id="linha-do-tempo" data-reveal>
      <h2>{t.timelineSectionTitle}</h2>

      <ol className="e-tl-eras">
        {timeline.map((era) => (
          <li key={era.key} className="e-tl-era">
            <div className="e-tl-head">
              <h3>{t.timelineEraNames[era.key]}</h3>
              <p>{t.timelineRange(era.fromYear, era.toYear)}</p>
            </div>
            <ul className="e-tl-list">
              {era.entries.map((entry) => {
                const view = viewOf(entry);
                if (!view) return null;
                return (
                  <li key={view.key}>
                    <button
                      type="button"
                      className="e-tl-item"
                      aria-label={t.personalityInfoAria(view.name)}
                      onClick={() => setInfo(view.selected)}
                    >
                      {view.photo}
                      <span className="e-tl-text">
                        <strong>{view.name}</strong>
                        <small>{view.date}</small>
                      </span>
                    </button>
                  </li>
                );
              })}
            </ul>
          </li>
        ))}
      </ol>

      {info?.type === 'personality' && (
        <PersonalityInfoSheet match={info.match} axes={axes} results={results} onClose={() => setInfo(null)} />
      )}
      {info?.type === 'country' && (
        <CountryInfoSheet match={info.match} axes={axes} results={results} onClose={() => setInfo(null)} />
      )}
    </section>
  );
}
