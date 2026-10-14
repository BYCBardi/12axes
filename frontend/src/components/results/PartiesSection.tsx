import { t } from '../../i18n';
import type { PartyMatch } from '../../types/quiz';
import { resolveCountryFlagSrc } from '../../utils/countryFlags';
import { resolvePartyLogoSrc } from '../../utils/partyLogos';
import { SafeImg } from '../editorial/primitives';
import { FarList, MatchHero } from './parts';

interface PartiesSectionProps {
  top: PartyMatch[];
  distant: PartyMatch[];
}

// Tipo · período (se histórico) · país associado, abaixo do nome.
function tags(match: PartyMatch): string[] {
  return [match.category, match.historical ? match.period : '', match.countryName].filter(Boolean);
}

// Logo oficial quadrado 1:1 do partido/organização, sobre fundo claro para qualquer símbolo escuro.
function Logo({ match, className }: { match: PartyMatch; className: string }) {
  return (
    <SafeImg
      className={className}
      src={resolvePartyLogoSrc(match.logoPath)}
      alt={t.partyLogoAlt(match.name)}
      fallback={t.partyLogoUnavailable}
    />
  );
}

/*
 * Por enquanto só o perfil mais compatível e os mais distantes. Quando o catálogo tiver mais
 * perfis, entra aqui a lista "Também próximos, por dimensão" (política, social e econômica),
 * como em Países e Personalidades, o que exige um matcher por dimensão no backend.
 */
export function PartiesSection({ top, distant }: PartiesSectionProps) {
  if (top.length === 0) {
    return null;
  }
  const first = top[0];

  return (
    <section className="e-panel" id="partidos" data-reveal>
      <h2>{t.partiesSectionTitle}</h2>

      <MatchHero
        key={first.partyId}
        visual={<Logo match={first} className="e-logobig" />}
        compatibility={first.compatibility}
        name={first.name}
        tags={tags(first)}
        description={first.description}
        kickerPrefix={
          first.countryFlagPath ? (
            <SafeImg
              className="e-flagmini"
              src={resolveCountryFlagSrc(first.countryFlagPath)}
              alt={t.flagAlt(first.historical ? t.flagHistoricLabel : t.flagLabel, first.countryName)}
              fallback=""
            />
          ) : null
        }
      />

      <FarList
        title={t.partiesDistantTitle}
        items={distant.map((match) => ({
          key: match.partyId,
          name: match.name,
          caption: tags(match).join(' · ')
        }))}
      />
    </section>
  );
}
