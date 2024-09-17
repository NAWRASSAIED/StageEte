import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import SidebarComponent from './SidebarComponent';
import { Chart, LineController, LineElement, PointElement, LinearScale, Title, CategoryScale, TimeScale } from 'chart.js';
import 'chartjs-adapter-date-fns'; // Import date-fns adapter for time scale

Chart.register(LineController, LineElement, PointElement, LinearScale, Title, CategoryScale, TimeScale);

const ArchiveComponent = () => {
    const [tickets, setTickets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedTicketHistory, setSelectedTicketHistory] = useState([]);
    const [showHistoryPopup, setShowHistoryPopup] = useState(false);
    const chartRef = useRef(null);

    useEffect(() => {
        const fetchClosedTickets = async () => {
            try {
                const response = await axios.get('http://localhost:8040/closed');
                setTickets(response.data);
                setLoading(false);
            } catch (err) {
                setError(err);
                setLoading(false);
            }
        };

        fetchClosedTickets();
    }, []);

    const fetchTicketStatusHistory = async (assetId) => {
        try {
            const response = await fetch(`http://localhost:8040/asset/${assetId}/history`);
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

    const handleShowHistory = async (assetId) => {
        const history = await fetchTicketStatusHistory(assetId);
        setSelectedTicketHistory(history);
        setShowHistoryPopup(true);
    };

    const closePopup = () => {
        setShowHistoryPopup(false);
    };

    const renderChart = (chartData) => {
        const ctx = chartRef.current?.getContext('2d');
        if (!ctx) {
            console.error('Canvas context not found');
            return;
        }

        const statusMap = {
            'OPEN': 1,
            'IN_PROGRESS': 2,
            'RESOLVED': 3,
            'CLOSED': 4
        };

        const sortedHistory = chartData.sort((a, b) => new Date(a.changeDate) - new Date(b.changeDate));

        new Chart(ctx, {
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
                            callback: (value) => {
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
    };

    useEffect(() => {
        if (showHistoryPopup) {
            renderChart(selectedTicketHistory);
        }
    }, [showHistoryPopup, selectedTicketHistory]);

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error.message}</p>;

    return (
        <div style={styles.container}>
            <SidebarComponent />
            <div style={styles.content}>
                <header style={styles.header}>
                    <h1 style={styles.headerTitle}>SecureFlow Admin Dashboard</h1>
                </header>
                <h2 style={styles.title}>Closed Tickets</h2>
                <table style={styles.table}>
                    <thead>
                        <tr>
                            <th style={styles.th}>User</th>
                            <th style={styles.th}>Asset</th>
                            <th style={styles.th}>Severity</th>
                            <th style={styles.th}>Status</th>
                            <th style={styles.th}>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {tickets.map(ticket => (
                            <tr key={ticket.id} style={styles.row}>
                              
                                <td style={styles.td}>{ticket.assetId}</td>
                                <td style={styles.td}>{ticket.severity}</td>
                                <td style={styles.td}>{ticket.status}</td>
                                <td style={styles.td}>
                                    <button
                                        style={styles.historyButton}
                                        onClick={() => handleShowHistory(ticket.assetId)}
                                    >
                                        Show History
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {showHistoryPopup && (
                    <div style={styles.popupOverlay}>
                        <div style={styles.popupContent}>
                            <button onClick={closePopup} style={styles.closeButton}>Close</button>
                            <canvas ref={chartRef} />
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

const styles = {
    container: {
        display: 'flex',
        flexDirection: 'row',
        fontFamily: 'Arial, sans-serif',
        backgroundColor: '#f8f9fa',
        height: '100vh',
        width: '1400px'
    },
    content: {
        marginLeft: '300px',
        width: 'calc(100% - 300px)',
        padding: '20px',
    },
    header: {
        width: '100%',
        height: '80px',
        background: 'linear-gradient(90deg, rgba(0,123,255,1) 0%, rgba(0,162,255,1) 50%, rgba(0,191,255,1) 100%)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        color: 'white',
        fontSize: '1.5em',
        fontWeight: 'bold',
        textTransform: 'uppercase',
        letterSpacing: '2px',
        borderRadius: '15px',
        marginBottom: '20px',
    },
    headerTitle: {
        fontSize: '24px',
        margin: 0,
    },
    title: {
        fontSize: '28px',
        fontWeight: 'bold',
        color: '#081079',
        marginBottom: '20px',
        textAlign: 'center',
        borderBottom: '2px solid #ddd',
        paddingBottom: '10px',
    },
    table: {
        width: '100%',
        borderCollapse: 'collapse',
    },
    th: {
        backgroundColor: '#007bff',
        color: '#fff',
        padding: '10px',
        textAlign: 'center',
        border: '1px solid #ddd',
    },
    td: {
        padding: '10px',
        textAlign: 'center',
        borderBottom: '1px solid #ddd',
    },
    row: {
        cursor: 'pointer',
        transition: 'background-color 0.3s',
    },
    historyButton: {
        padding: '8px 12px',
        backgroundColor: '#2b8eff',
        color: '#fff',
        border: 'none',
        borderRadius: '4px',
        cursor: 'pointer',
        transition: 'background-color 0.3s',
    },
    popupOverlay: {
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        backgroundColor: 'rgba(0, 0, 0, 0.7)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
    },
    popupContent: {
        backgroundColor: 'white',
        padding: '20px',
        borderRadius: '8px',
        width: '80%',
        maxWidth: '600px',
        position: 'relative',
    },
    closeButton: {
        position: 'absolute',
        top: '10px',
        right: '10px',
        backgroundColor: '#ff4d4d',
        color: 'white',
        border: 'none',
        borderRadius: '4px',
        padding: '5px 10px',
        cursor: 'pointer',
    }
};

export default ArchiveComponent;
