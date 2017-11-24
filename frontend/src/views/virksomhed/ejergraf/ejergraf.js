import React, { Component } from 'react';
import Graph from 'react-graph-vis';
import Modal from '../../common/modal.js';
import EjerVisning from '../../common/ejervisning';

export default class EjerGraf extends React.Component {

  openModal() {
    this.setState({ isModalOpen: true })
  }

  closeModal() {
    this.setState({ isModalOpen: false, specifikEjer:null})
  }

  constructor(props) {
    super(props);
    this.state = {};

    var map = new Map();
    var allMap = new Map();

    this.props.ejerGraf.ejere.forEach((ejer) =>{
      map.set(ejer.ejer.enhedsnummer, ejer.ejer.forretningsnoegle);
      allMap.set(ejer.ejer.enhedsnummer, ejer);
    });

    this.state = {ejerMap: map, ejerAllMap: allMap, isModalOpen: false};
    this._visVirksomhed = this._visVirksomhed.bind(this);
    this.openModal = this.openModal.bind(this);
    this.closeModal = this.closeModal.bind(this);
  }

  _visVirksomhed(enhedsnr) {
    const ejer = this.state.ejerAllMap.get(enhedsnr).ejer;
    this.setState({specifikEjer: ejer});
    this.openModal();
  }

  render() {
    const ejergraf = this.props.ejerGraf;

    const n = ejergraf.ejere.map((ejer) => {
      var group = ejer.ejer.ejertype == 'PERSON' ? 'personer' : 'virksomheder'
      if (ejer.ejer.ejertype == 'ROD') {
        group = 'rod';
      }
      return {id: ejer.ejer.enhedsnummer, label: ejer.ejer.navn, group: group};
    });

    const e = ejergraf.ejerRelationer.map((er) =>{
      const vnr = (er.virksomhed.ejer ? er.virksomhed.ejer.enhedsnummer:"0");
      return {from:er.ejer.ejer.enhedsnummer, to: vnr};
    });

    var graph = {
      nodes: n,
      edges: e
    };


    var options = {
      layout: {
        hierarchical: {
          direction: "UD",
          sortMethod: "directed"
        },
        improvedLayout: true
      },
      edges: {
        color: "#000000"
      },
      height: '100%',
      width: '100%',
      autoResize: true,
      interaction: {
        hover:true
      },
      groups: {
        virksomheder: {
          shape: 'icon',
          icon: {
            face: 'FontAwesome',
            code: '\uf0c0',
            size: 50,
            color: '#57169a'
          }
        },
        rod: {
          shape: 'icon',
          icon: {
            face: 'FontAwesome',
            code: '\uf0c0',
            size: 50,
            color: '#2058b2'
          }
        },
        personer: {
          shape: 'icon',
          icon: {
            face: 'FontAwesome',
            code: '\uf007',
            size: 50,
            color: '#aa00ff'
          }
        }
      }
    };

    var vv = this._visVirksomhed;

    var events = {
      click: function(event) {
        var { nodes } = event;

        if (nodes.length==1) {
          vv(nodes[0]);
        }
      }
    }

    var styles = {
      width: '100%', height: '100%'
    }

    return(
      <div className="ejergraf">
        <br/>
        <div className="row">
          <div className="col-2" />
          <div className="col-8">
            Klik på ejerne herunder for at se detaljer om ejeren og dennes direkte og indirekte andele i virksomhederne
          </div>
          <div className="col-2" />

        </div>
        <br/>
        <div className="row">
          <div className="col ejergrafcol">
            <Graph graph={graph} options={options} events={events} style={styles}/>
          </div>
        </div>
        <Modal isOpen={this.state.isModalOpen} onClose={() => this.closeModal()} className="card ejercard" width="70%">
          <EjerVisning ejer={this.state.specifikEjer} visVirksomhed={this.props.visVirksomhed}
                       visDeltager={this.props.visDeltager} />
        </Modal>
      </div>
    );
  }

}