package dk.ts.virkr.services.model.graf

/**
 * Created by sorenhartvig on 20/10/2017.
 */
class DeltagerRelation {
  String deltagerEnhedsnummer
  String virksomhedEnhedsnummer
  String andelInterval

  DeltagerRelation(String deltagerEnhedsnummer, String virksomhedEnhedsnummer, String andelInterval) {
    this.deltagerEnhedsnummer = deltagerEnhedsnummer
    this.virksomhedEnhedsnummer = virksomhedEnhedsnummer
    this.andelInterval = andelInterval
  }
}
