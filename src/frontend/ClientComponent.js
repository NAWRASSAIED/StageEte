import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

import './client.css'; // Ensure your CSS is up to date
import { Chart, LineController, LineElement, PointElement, LinearScale, Title, CategoryScale, TimeScale } from 'chart.js';
import 'chartjs-adapter-date-fns'; // Import date-fns adapter for time scale

Chart.register(LineController, LineElement, PointElement, LinearScale, Title, CategoryScale, TimeScale);

const ClientComponent = () => {
  const navigate = useNavigate();
  const [htmlContent, setHtmlContent] = useState('');
  const [tickets, setTickets] = useState([]);
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [newStatus, setNewStatus] = useState('OPEN'); // Default status
  const [statusOptions] = useState(['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']); // Combo box options
  const [showHistoryPopup, setShowHistoryPopup] = useState(false);
  const [statusChanged, setStatusChanged] = useState(false); // To track status changes
  const chartRef = useRef(null);
  const [chartInstance, setChartInstance] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [hasMoreTickets, setHasMoreTickets] = useState(true);
  const ticketsPerPage = 3;
 
  const containerRef = useRef(null);

  

  useEffect(() => {
    const fetchHTMLContent = async () => {
      try {
        const response = await fetch('/frontend/Client.html');
        if (!response.ok) {
          throw new Error('Failed to load HTML content');
        }
        const html = await response.text();
        setHtmlContent(html);
      } catch (error) {
        console.error('Error loading HTML content:', error);
      }
    };

    fetchHTMLContent();
  }, []);

  useEffect(() => {
    const fetchTickets = async () => {
      try {
        const token = localStorage.getItem('authToken');
        const userId = localStorage.getItem('userId');
  
        if (!token || !userId) {
          navigate('/signin');
          return;
        }
  
        const response = await fetch(`http://localhost:8040/tickets-user/${userId}`, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
  
        if (response.ok) {
          const tickets = await response.json();
          
          // Filter out tickets with status 'CLOSED'
          const filteredTickets = tickets.filter(ticket => ticket.status !== 'CLOSED');
  
          // Define severity levels
          const severityLevels = {
            'Critical': 1,
            'Severe': 2,
            'Moderate': 3,
            'LOW': 4
          };
  
          // Sort tickets by severity
          const sortedTickets = filteredTickets.sort((a, b) => {
            return severityLevels[a.severity] - severityLevels[b.severity];
          });
  
          // Set the sorted tickets
          setTickets(sortedTickets);
        } else {
          console.error('Failed to fetch tickets:', response.statusText);
        }
      } catch (error) {
        console.error('Network error:', error);
      }
    };
  
    fetchTickets();
  }, [navigate]);
  

  const fetchTicketDetails = async (ticketId) => {
    try {
      const response = await fetch(`http://localhost:8040/tickets/details/by-ip/${ticketId}`);
      if (response.ok) {
        const ticketDetails = await response.json();
        setSelectedTicket(ticketDetails);
        if (showHistoryPopup) {
          renderChart(ticketId);
        }
      } else {
        console.error('Failed to fetch ticket details:', response.statusText);
      }
    } catch (error) {
      console.error('Network error:', error);
    }
  };

  const fetchTicketStatusHistory = async (ticketId) => {
    try {
      const response = await fetch(`http://localhost:8040/asset/${ticketId}/history`);
      if (response.ok) {
        return await response.json();
      } else {
        console.error('Failed to fetch status history:', response.statusText);
        return [];
      }
    } catch (error) {
      console.error('Network error:', error);
      return [];
    }
  };

  const handleTicketClick = (ticketId) => {
    fetchTicketDetails(ticketId);
  };

  const handleStatusChange = (event) => {
    setNewStatus(event.target.value);
  };

  const handleStatusUpdate = async () => {
    if (selectedTicket) {
      try {
        const token = localStorage.getItem('authToken');
        const response = await fetch(`http://localhost:8040/status/${selectedTicket.id}?status=${newStatus}`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        if (response.ok) {
          const updatedTicket = await response.json();
          setSelectedTicket(prev => ({ ...prev, status: updatedTicket.status }));
          setStatusChanged(true); // Indicate a status change occurred
        } else {
          console.error('Failed to update status:', response.statusText);
        }
      } catch (error) {
        console.error('Network error:', error);
      }
    } else {
      console.error('No ticket selected');
    }
  };
  const statusMap = {
    'OPEN': 1,
    'IN_PROGRESS': 2,
    'RESOLVED': 3,
    'CLOSED': 4
  };
  
  const renderChart = async (ticketId) => {
    const statusHistory = await fetchTicketStatusHistory(ticketId);
  
    const ctx = chartRef.current?.getContext('2d');
    if (!ctx) {
      console.error('Canvas context not found');
      return;
    }
  
  
  
    // Define statusMap with specific y-axis values
    const statusMap = {
      'OPEN': 1,
      'IN_PROGRESS': 2,
      'RESOLVED': 3,
      'CLOSED': 4
    };
  
    // Sort statusHistory by changeDate
    const sortedHistory = statusHistory.sort((a, b) => new Date(a.changeDate) - new Date(b.changeDate));
  
    try {
      // Create the chart instance
      const newChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
          labels: sortedHistory.map(entry => new Date(entry.changeDate)), // Dates on x-axis
          datasets: [
            {
              label: 'Ticket Status History',
              data: sortedHistory.map(entry => ({
                x: new Date(entry.changeDate), // Convert date string to Date object
                y: statusMap[entry.status] // Map status to y-axis value
              })),
              fill: false,
              borderColor: 'rgba(75, 192, 192, 1)',
              backgroundColor: 'rgba(75, 192, 192, 0.2)',
              tension: 0.1, // Smooth curve
              pointRadius: 5, // Make points more visible
              pointBackgroundColor: 'rgba(75, 192, 192, 1)',
            }
          ]
        },
        options: {
          scales: {
            x: {
              type: 'time',
              time: {
                unit: 'minute', // Adjust this based on your needs
                tooltipFormat: 'yyyy-MM-DD HH:mm:ss'
              },
              title: {
                display: true,
                text: 'Date'
              },
              ticks: {
                callback: (value, index, values) => {
                    const date = new Date(value);
                    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
                }
            }
        },
            y: {
              beginAtZero: true,
              suggestedMax: 5, // Ensure the max value accommodates all statuses
              ticks: {
                stepSize: 1,
                callback: (value) => {
                  // Display status names at specific y values
                  const status = Object.keys(statusMap).find(key => statusMap[key] === value);
                  return status || '';
                }
              },
              title: {
                display: true,
                text: 'Status'
              }
            }
          }
        }
      });
  
      setChartInstance(newChartInstance);
    } catch (error) {
      console.error('Error creating chart:', error);
    }
  };
  
  

  const handleClosePopup = () => {
    setSelectedTicket(null);
    setShowHistoryPopup(false);
  };
  useEffect(() => {
    if (selectedTicket && showHistoryPopup) {
      renderChart(selectedTicket.id);
    }
  }, [selectedTicket, showHistoryPopup]);
  

  useEffect(() => {
    const parser = new DOMParser();
    const doc = parser.parseFromString(htmlContent, 'text/html');
    const ticketsContainer = doc.getElementById('tickets-cont');

    if (ticketsContainer) {
      if (tickets.length > 0) {
        const ticketsHTML = tickets.map(ticket =>
          `<div class="tickett" key="${ticket.id}" onclick="window.dispatchEvent(new CustomEvent('ticketClick', { detail: '${ticket.userId}' }))">
            <h3><i class="icon-severity-${ticket.severity.toLowerCase()}"></i>Name: ${ticket.assetId}</h3>
            <p class="severity"><strong>Severity:</strong> ${ticket.severity}</p>
            <div class="button-container">
    <button onclick="window.dispatchEvent(new CustomEvent('showDetails', { detail: '${ticket.id}' }))">Details</button>
    <button onclick="window.dispatchEvent(new CustomEvent('showHistory', { detail: '${ticket.id}' }))">History</button>
</div>
          </div>`
        ).join('');

        ticketsContainer.innerHTML = `
          <div class="tickets-wrapper">
            <h2>Your Tickets</h2>
            <div class="ticket-cont">
              ${ticketsHTML}
            </div>
          </div>`;
      } else {
        ticketsContainer.innerHTML = '<p class="no-tickets"><strong>You don\'t have tickets yet.</strong></p>';
      }

      setHtmlContent(doc.documentElement.outerHTML);

      // Add event listeners for ticket clicks and button actions
      window.addEventListener('ticketClick', (e) => handleTicketClick(e.detail));
      window.addEventListener('showDetails', (e) => {
        fetchTicketDetails(e.detail);
        setShowHistoryPopup(false);
      });
      window.addEventListener('showHistory', (e) => {
        fetchTicketDetails(e.detail);
        setShowHistoryPopup(true);
      });
    }
  }, [htmlContent, tickets]);

  return (
    <div>
      <div dangerouslySetInnerHTML={{ __html: htmlContent }} />

      {/* Style and content of the popup */}
      {selectedTicket && !showHistoryPopup && (
      <div className="popup-overlay">
      <div className="popup-content">
          <span className="popup-close" onClick={handleClosePopup}>&times;</span>
          <h2>More Details</h2>
          <div className="ticket-details">
    <p><strong>Description:</strong> <span class="value">{selectedTicket.description}</span></p>
    <p><strong>Severity:</strong> <span class="value">{selectedTicket.severity}</span></p>
    <p><strong>Remediation:</strong> <span class="value">{selectedTicket.remediation}</span></p>
    <p><strong>Status:</strong> <span class="value">{selectedTicket.status}</span></p>
</div>
          <div className="popup-status">
              <select id="status-select" value={newStatus} onChange={handleStatusChange}>
                  {statusOptions.map(option => (
                      <option key={option} value={option}>{option}</option>
                  ))}
              </select>
              <button className="update-status-button" onClick={handleStatusUpdate}>Update Status</button>

          </div>
          {statusChanged && <p className="success-message">Status updated successfully!</p>}
          <button className="custom-button history-button" onClick={() => setShowHistoryPopup(true)}>View History</button>


      </div>
  </div>
  
)}

      {/* Popup for chart */}
      {showHistoryPopup && (
        <div className="popup-overlay">
          <div className="popup-content">
            <h2>Ticket Status History</h2>
            <canvas ref={chartRef} width="400" height="200"></canvas>
            <button className="custom-button history-button" onClick={handleClosePopup}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ClientComponent;
