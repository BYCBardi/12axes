package com.twelveaxes.model;

import java.util.List;

/**
 * Uma era da linha do tempo com os perfis mais compativeis do usuario.
 *
 * @param key antiguidade, medieval, moderna, xix, xx ou xxi
 * @param fromYear ano em que a era comeca (nulo na Antiguidade, que nao tem inicio)
 * @param toYear ano em que a era termina, exclusivo (nulo na ultima, ainda aberta)
 * @param compatibility compatibilidade da era: media da compatibilidade dos 10 perfis mais
 *     compativeis do usuario na era (personalidades, paises e partidos juntos, sem os tetos por tipo)
 * @param entries os perfis mostrados da era (4, com tetos por tipo), do topo para baixo
 */
public record TimelineEra(String key, Integer fromYear, Integer toYear, double compatibility, List<TimelineEntry> entries) {
}
