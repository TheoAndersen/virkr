import React, { Component } from 'react';
import CvrSoegebox from './cvrsoegebox';
import CvrVisning from './cvrvisning';
import Soegeresultat from './soegeresultat';

import APIHelper from '../utils/apihelper.js';

export default class Virksomhed extends Component {

  constructor() {
    super();

    this._opdaterCvrNummer = this._opdaterCvrNummer.bind(this);
    this._visSoegeresultat = this._visSoegeresultat.bind(this);

    this.state = {
      visSpinner: false,
      cvrnummer: '',
      regnskaber: [],
      cvrdata: null,
      soegeresultat: null
    };
  }

  _visSoegeresultat(soegning) {
    this.setState({visSpinner: true, cvrnummer: '', regnskaber: []})
    APIHelper.soeg(soegning).then((data) => {
      this.setState({ soegeresultat: data, visSpinner: false })
    }, (fejl) => {
      alert(fejl);
    })
  }

  _opdaterCvrNummer(cvrnr) {
    this.setState({cvrnummer: cvrnr, soegeresultat: null});

    let _regnskabsdata;

    this.setState({ visSpinner: true }, () => {
      APIHelper.hentNoegletal(cvrnr)
        .then((data) => {
          _regnskabsdata = data.regnskabsdata
          return APIHelper.hentVirksomhedsdata(cvrnr);
        })
        .then((_cvrdata) => {
          this.setState({ visSpinner: false, cvrnummer: cvrnr, regnskaber: _regnskabsdata, cvrdata: _cvrdata })
        })
        .catch((err) => {
          this.setState({ visSpinner: false }, () => alert(err))
        })
    })
  }

  render() {

    const { cvrnummer, regnskaber, visSpinner, cvrdata, soegeresultat} = this.state;

    return (<div className="virksomhed">
      <div className="row">
        <div className="col">
          <h1 id="virkr-header"><a href="/" id="virk-header-a">Virkr</a></h1>
          <p id="virkr-tagline">Nøgletal om virksomheder</p>
        </div>
      </div>
      <div className="row">
        <div className="col">
          <CvrSoegebox opdaterCvr={this._visSoegeresultat} />
        </div>
      </div>
      <div className="row">
        <div className="col">
          {(cvrnummer !== '') ? <CvrVisning cvrnummer={cvrnummer} regnskaber={regnskaber} spinner={visSpinner}
                                            cvrdata={cvrdata} /> : null}
          {(soegeresultat !== null) ? <Soegeresultat soegeresultat={soegeresultat} opdaterCvrNummer={this._opdaterCvrNummer}/> : null }
        </div>
      </div>
    </div>);
  }

}
