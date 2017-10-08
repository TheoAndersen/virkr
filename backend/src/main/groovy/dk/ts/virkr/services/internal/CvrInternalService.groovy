package dk.ts.virkr.services.internal

import dk.ts.virkr.cvr.integration.CvrClient
import dk.ts.virkr.cvr.integration.model.deltager.VirksomhedSummariskRelation
import dk.ts.virkr.cvr.integration.model.deltager.Vrdeltagerperson
import dk.ts.virkr.services.model.Ejer
import dk.ts.virkr.services.model.EjerAfVirksomhed
import dk.ts.virkr.services.model.EjerGraf
import dk.ts.virkr.services.model.EjerRelation
import dk.ts.virkr.services.model.EjerType
import dk.ts.virkr.cvr.integration.model.virksomhed.Navn
import dk.ts.virkr.services.model.ReelEjerandel
import dk.ts.virkr.cvr.integration.model.virksomhed.Vrvirksomhed
import dk.ts.virkr.services.model.DeltagerSoegeresultat
import dk.ts.virkr.services.model.DeltagerVirksomhed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by sorenhartvig on 03/07/2017.
 */
@Service
class CvrInternalService {

  Logger logger = LoggerFactory.getLogger(CvrInternalService.class)

  @Autowired
  CvrClient cvrClient

  EjerGraf hentEjergraf(String cvrnummer) {
    EjerGraf ejerGraf = new EjerGraf()
    Vrvirksomhed vrvirksomhed = cvrClient.hentVirksomhed(cvrnummer)

    EjerAfVirksomhed ejerAfVirksomhed = new EjerAfVirksomhed()
    ejerAfVirksomhed.cvrnummer = vrvirksomhed.cvrNummer
    ejerAfVirksomhed.virksomhedsnavn = vrvirksomhed.virksomhedMetadata.nyesteNavn.navn
    ejerAfVirksomhed.ejer = new Ejer()

    // Pseudo ejer for at have øverste niveau med ud, blive muligvis recactored så man mere eksplicit sætter roden
    ejerAfVirksomhed.ejer.navn = ejerAfVirksomhed.virksomhedsnavn
    ejerAfVirksomhed.ejer.forretningsnoegle = ejerAfVirksomhed.cvrnummer
    ejerAfVirksomhed.ejer.enhedsnummer = vrvirksomhed.enhedsNummer
    ejerAfVirksomhed.ejer.ejertype = EjerType.ROD

    ejerGraf.ejere << ejerAfVirksomhed

    berigEjergraf(vrvirksomhed, ejerGraf, ejerAfVirksomhed)

    return ejerGraf
  }

  EjerGraf berigEjergrafMedReelleEjerAndele(Vrvirksomhed vrvirksomhed, EjerGraf ejerGraf) {
    List<List<Ejer>> ejergrene =[]

    vrvirksomhed.ejere.each { ejer->
      List
    }
  }

  /**
   *
   * @param vrvirksomhed virksomhed hvis ejere skal traverseres
   * @param ejerGraf grafen der skal beriges
   * @param virksomhed modervirksomheden til vrvirksomheden
   */
  void berigEjergraf(Vrvirksomhed vrvirksomhed, EjerGraf ejerGraf, EjerAfVirksomhed virksomhed) {

    logger.info("Beriger ejergraf med virksomheden : $vrvirksomhed.virksomhedMetadata.nyesteNavn.navn")
    vrvirksomhed.ejere.each { ejer->
      if (ejer.forretningsnoegle && ejer.forretningsnoegle == vrvirksomhed.cvrNummer) {
        // skip den findes jo givetvis allerede
        return
      }
      logger.info("Tilføjer ejer : $ejer.navn")
      EjerAfVirksomhed ejerAfVirksomhed = new EjerAfVirksomhed()
      ejerAfVirksomhed.cvrnummer = vrvirksomhed.cvrNummer
      ejerAfVirksomhed.virksomhedsnavn = vrvirksomhed.virksomhedMetadata.nyesteNavn.navn
      ejerAfVirksomhed.ejer = ejer
      ejerGraf.ejere << ejerAfVirksomhed
      EjerRelation ejerRelation = new EjerRelation()
      ejerRelation.virksomhed = virksomhed
      ejerRelation.ejer = ejerAfVirksomhed

      List<ReelEjerandel> moderEjere = virksomhed.ejer.reelleEjerandele
      ejer.reelleEjerandele = beregnReelleEjerAndele(moderEjere, ejer, ejerAfVirksomhed.cvrnummer, ejerAfVirksomhed.virksomhedsnavn)

      ejerGraf.ejerRelationer << ejerRelation

      if (ejer.ejertype != EjerType.PERSON) {
        Vrvirksomhed v = cvrClient.hentVirksomhed(ejer.forretningsnoegle)
        if (v && v.cvrNummer != vrvirksomhed.cvrNummer) {
          berigEjergraf(v, ejerGraf, ejerAfVirksomhed)
        }
      }
    }
  }

  /**
   * Beregner ejerandel, ved at kigge på moderselskabets og moderens moder osv osv, og derved beregne sin andel i hver virksomhed
   * @param re ejerandele fra moderselskaber
   * @param ejer ejer der skal beregnes ud fra
   * @param cvrnr cvnr på ejerens umiddelbare ejerskab
   * @param virksomhedsnavn virksomhedsnavn på  ejerens umiddelbare ejerskab
   * @return
   */
  List<ReelEjerandel> beregnReelleEjerAndele(List<ReelEjerandel> re, Ejer ejer, String cvrnr, String virksomhedsnavn) {
    List<ReelEjerandel> reelleEjerandele = []
    re.each { r->
      reelleEjerandele << beregnReelEjerandel(ejer, r, r.virksomhedsnavn, r.cvrnummer)
    }
    reelleEjerandele << beregnReelEjerandel(ejer, null,virksomhedsnavn, cvrnr)
  }

  /**
   * Denne metode beregner en ejerandel ud fra den ejerandel virksomheden / eller personen direkte har i en given virksomhed
   * @param ejer
   * @param vrvirksomhed
   * @return
   */
  ReelEjerandel beregnReelEjerandel(Ejer ejer, ReelEjerandel reelEjerandel, String virksomhedsnavn, String cvrnr) {
    ReelEjerandel beregnetReelEjerandel = new ReelEjerandel()
    beregnetReelEjerandel.cvrnummer = cvrnr
    beregnetReelEjerandel.virksomhedsnavn = virksomhedsnavn
    beregnetReelEjerandel.kapitalklasse = ejer.kapitalklasse
    beregnetReelEjerandel.andel = reelEjerandel ? beregn(reelEjerandel.andel, ejer.andel) : ejer.andel
    beregnetReelEjerandel.stemmeprocent = reelEjerandel ? beregn(reelEjerandel.stemmeprocent, ejer.stemmeprocent) : ejer.stemmeprocent
    beregnetReelEjerandel.stemmeprocentInterval = Ejer.interval(beregnetReelEjerandel.stemmeprocent)
    beregnetReelEjerandel.andelInterval = Ejer.interval(beregnetReelEjerandel.andel)
    return beregnetReelEjerandel
  }

  String beregn(String re, String e)  {
    double a1 = Double.valueOf(re)
    double a2 = Double.valueOf(e)
    double resultat = a1*a2
    return String.valueOf(resultat)
  }

  /**
   * Omdanner en deltager til en kortere deltagere søgeresultat form hvor man direkte se relevante oplysninger om deltageren
   * @param vrdeltagerperson
   * @return
   */
  DeltagerSoegeresultat tilDeltager(Vrdeltagerperson vrdeltagerperson) {
    DeltagerSoegeresultat deltagerSoegeresultat = new DeltagerSoegeresultat()
    deltagerSoegeresultat.navn = vrdeltagerperson.navne[0].navn
    deltagerSoegeresultat.adresselinie = vrdeltagerperson.deltagerpersonMetadata.nyesteBeliggenhedsadresse.vejadresselinie
    deltagerSoegeresultat.bylinie = vrdeltagerperson.deltagerpersonMetadata.nyesteBeliggenhedsadresse.byLinje
    deltagerSoegeresultat.postnr = vrdeltagerperson.deltagerpersonMetadata.nyesteBeliggenhedsadresse.postnummer
    deltagerSoegeresultat.bynavn = vrdeltagerperson.deltagerpersonMetadata.nyesteBeliggenhedsadresse.bynavn
    deltagerSoegeresultat.enhedsNummer = vrdeltagerperson.enhedsNummer
    deltagerSoegeresultat.enhedstype = vrdeltagerperson.enhedstype
    deltagerSoegeresultat.virksomheder = []
    vrdeltagerperson.virksomhedSummariskRelation.each {vsr->
      // arbejder p.t. kun med aktuelle data, så der skal være et livsforløb hvor gyldigtil er null
      if (vsr.virksomhed.livsforloeb.find{ !it.periode.gyldigTil}) {
        deltagerSoegeresultat.virksomheder << lavDeltagerVirksomhed(vsr)
      }
    }
    return deltagerSoegeresultat
  }

  /**
   * Samler basis informationer om en virksomheds relation
   * @param virksomhedSummariskRelation
   * @return
   */
  DeltagerVirksomhed lavDeltagerVirksomhed(VirksomhedSummariskRelation virksomhedSummariskRelation) {
    DeltagerVirksomhed deltagerVirksomhed = new DeltagerVirksomhed()

    deltagerVirksomhed.cvrnr = virksomhedSummariskRelation.virksomhed.cvrNummer
    deltagerVirksomhed.enhedsNummer = virksomhedSummariskRelation.virksomhed.enhedsNummer

    Navn virksomhedsnavn = virksomhedSummariskRelation.virksomhed.navne.find { it->
      it.periode.gyldigTil == null
    }

    if (virksomhedsnavn) {
      deltagerVirksomhed.navn = virksomhedsnavn.navn
    }

    List<String> roller = virksomhedSummariskRelation.organisationer.collect {it->
      return it.organisationsNavn.find { !it.periode.gyldigTil }?.navn
    }

    roller = konverterRoller(roller)

    deltagerVirksomhed.roller = roller.join(", ")

    return deltagerVirksomhed

  }

  List<String> konverterRoller(List<String> roller) {
    return roller.collect { it->
      if (it.endsWith("er")) {
        return it.substring(0,it.length()-2)
      }
      if (it == 'EJERREGISTER') {
        return 'Ejer'
      }

      return it
    }.findAll {it != 'Reelle ejere'}
  }

}
