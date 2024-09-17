import React, { useState, useEffect, useRef } from 'react';
import { Chart, LineController, LineElement, PointElement, LinearScale, Title, CategoryScale } from 'chart.js';

Chart.register(LineController, LineElement, PointElement, LinearScale, Title, CategoryScale);

const TicketHistoryChart = ({ assetId }) => {
  const chartRef = useRef(null);
  const [chartInstance, setChartInstance] = useState(null);
  const [statusHistory, setStatusHistory] = useState([]);

  useEffect(() => {
    const fetchStatusHistory = async () => {
      try {
        const response = await fetch(`http://localhost:8040/asset/${assetId}/history`);
        if (response.ok) {
          const data = await response.json();
          setStatusHistory(data);
        } else {
          console.error('Erreur lors de la récupération de l\'historique des statuts:', response.statusText);
        }
      } catch (error) {
        console.error('Erreur réseau:', error);
      }
    };

    if (assetId) {
      fetchStatusHistory();
    }
  }, [assetId]);

  useEffect(() => {
    if (statusHistory.length > 0 && chartRef.current) {
      const ctx = chartRef.current.getContext('2d');

      // Détruire l'instance précédente du graphique s'il existe
      if (chartInstance) {
        chartInstance.destroy();
      }

      const newChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
          labels: statusHistory.map(entry => entry.date),  // Utiliser les dates pour l'axe des x
          datasets: [{
            label: 'Évolution du statut du ticket',
            data: statusHistory.map(entry => {
              switch (entry.status) {
                case 'OPEN':
                  return 1;
                case 'IN_PROGRESS':
                  return 2;
                case 'RESOLVED':
                  return 3;
                case 'CLOSED':
                  return 4;
                default:
                  return 0;
              }
            }),
            borderColor: 'rgba(75, 192, 192, 1)',
            fill: false,
            tension: 0.1
          }]
        },
        options: {
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                callback: (value) => {
                  switch (value) {
                    case 1:
                      return 'OPEN';
                    case 2:
                      return 'IN_PROGRESS';
                    case 3:
                      return 'RESOLVED';
                    case 4:
                      return 'CLOSED';
                    default:
                      return '';
                  }
                }
              }
            }
          }
        }
      });

      setChartInstance(newChartInstance);
    }
  }, [statusHistory]);

  return (
    <div>
      <h3>Historique des statuts pour l'asset ID: {assetId}</h3>
      <canvas ref={chartRef}></canvas>
    </div>
  );
};

export default TicketHistoryChart;
