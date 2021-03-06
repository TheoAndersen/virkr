import React, { Component } from 'react';
import { Line } from 'react-chartjs-2';
import { resolveJsonValue } from '../../../utils/utils'

export default class VaerdiGraf extends Component {

  constructor(props) {
    super(props);

    Chart.defaults.global.scaleLabel = (label) => {
      return label.value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
    };

    Chart.defaults.global.multiTooltipTemplate = (label) => {
      return label.datasetLabel + ': ' + label.value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
    };

  }

  render() {
    return this.getGraf(this.props.regnskaber);
  }

  getGraf(regnskaber) {
    const { felt, label } = this.props;

    const aarLabels = regnskaber.map((regnskab) => {
      return regnskab.aktueltAarsregnskab.aar
    });

    const vaerdier = regnskaber.map((regnskab) => {
      const vaerdi = resolveJsonValue( felt, regnskab.aktueltAarsregnskab)
      if (vaerdi) {
        return vaerdi.vaerdi;
      }
      return null;
    });

    const options = {
      tooltips: {
        enabled: true,

        callbacks: {
          label: (tooltipItems, data) => {
            return data.datasets[tooltipItems.datasetIndex].label + ' : ' + tooltipItems.yLabel.toLocaleString().replace(/,/g, ".");
          }
        }
      },
      scales: {
        yAxes: [{
          ticks: {
            // Create scientific notation labels
            callback: (value) => {
              return value.toLocaleString().replace(/,/g, ".");
            }
          }
        }]
      }
    }

    const data = {
      labels: aarLabels,
      datasets: [
        {
          label: label,
          fill: false,
          lineTension: 0.1,
          backgroundColor: "#F19D4B",
          borderColor: "#F19D4B",
          borderCapStyle: 'butt',
          borderDash: [],
          borderDashOffset: 0.0,
          borderJoinStyle: 'miter',
          pointBorderColor: "#F19D4B",
          pointBackgroundColor: "#fff",
          pointBorderWidth: 1,
          pointHoverRadius: 5,
          pointHoverBackgroundColor: "#F19D4B",
          pointHoverBorderColor: "rgba(220,220,220,1)",
          pointHoverBorderWidth: 2,
          pointRadius: 1,
          pointHitRadius: 10,
          data: vaerdier,
          spanGaps: false
        }
      ]
    };

    return (
      <div className="graf">
        <Line data={data} options={options} />
      </div>
    );
  }

}
