package com.twelveaxes.model;

import java.util.List;

/**
 * Uma era da linha do tempo com os perfis mais compativeis do usuario.
 *
 * @param key antiguidade, medieval, moderna, xix, xx ou xxi
 * @param fromYear ano em que a era comeca (nulo na Antiguidade, que nao tem inicio)
 * @param toYear ano em que a era termina, exclusivo (nulo na ultima, ainda aberta)
 * @param entries os perfis mais compativeis da era, personalidades e paises juntos, do topo para baixo
 */
public record TimelineEra(String key, Integer fromYear, Integer toYear, List<TimelineEntry> entries) {
}
