import React, { useEffect, useState } from 'react';
import axios from 'axios';
import SidebarComponent from './SidebarComponent';
import { Bar, Pie } from 'react-chartjs-2';
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, ArcElement } from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, ArcElement);

const Sites = () => {
    const [sites, setSites] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [hoveredLink, setHoveredLink] = useState(null);
    const [showPopup, setShowPopup] = useState(false);
    const [tickets, setTickets] = useState([]);
    const [selectedSite, setSelectedSite] = useState(null);
    const [users, setUsers] = useState([]);
    const [selectedTicket, setSelectedTicket] = useState(null);
    const [selectedUser, setSelectedUser] = useState(null);
    const [showUserPopup, setShowUserPopup] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const [ticketsPerPage] = useState(6); // Nombre de tickets par page

    useEffect(() => {
        const fetchSites = async () => {
            try {
                const response = await axios.get('http://localhost:8040/sites/info');
                setSites(response.data);
                setLoading(false);
            } catch (error) {
                setError('Error fetching site data.');
                setLoading(false);
            }
        };

        const fetchUsers = async () => {
            try {
                const response = await axios.get('http://localhost:8040/users');
                const allUsers = response.data;

                // Filtrer les utilisateurs pour ne garder que ceux avec le rôle "NORMAL"
                const normalUsers = allUsers.filter(user =>
                    user.roles && user.roles.some(role => role.name === 'NORMAL')
                );

                setUsers(normalUsers);
            } catch (error) {
                console.error('Error fetching user data:', error.response ? error.response.data : error.message);
                setError('Error fetching user data.');
            }
        };

        fetchSites();
        fetchUsers();
    }, []);

   
    const handleSiteClick = async (siteName) => {
        try {
            const response = await axios.get(`http://localhost:8040/bysitename/${siteName}`);

            const severityLevels = {
                'Critical': 1,
                'Severe': 2,
                'Moderate': 3
            };

            const sortedTickets = response.data.sort((a, b) => {
                return severityLevels[a.severity] - severityLevels[b.severity];
            });

            setTickets(sortedTickets);
            setSelectedSite(siteName);
            setShowPopup(true);
            setCurrentPage(1); // Réinitialiser à la première page
        } catch (error) {
            setError('Error fetching tickets.');
        }
    };

    // Calculer les tickets actuels
    const indexOfLastTicket = currentPage * ticketsPerPage;
    const indexOfFirstTicket = indexOfLastTicket - ticketsPerPage;
    const currentTickets = tickets.slice(indexOfFirstTicket, indexOfLastTicket);

    // Changer de page
    const paginateNext = () => {
        if (currentPage < Math.ceil(tickets.length / ticketsPerPage)) {
            setCurrentPage(currentPage + 1);
        }
    };

    const paginatePrevious = () => {
        if (currentPage > 1) {
            setCurrentPage(currentPage - 1);
        }
    };

    const handleAssignButtonClick = (ticket) => {
        setSelectedTicket(ticket);
        setShowUserPopup(true);
    };

    const handleUserSelection = async () => {
        console.log('Assigning ticket:', selectedTicket);
        console.log('Selected user:', selectedUser);
        if (selectedTicket && selectedUser) {
            try {
                // Assurez-vous que l'URL de l'API est correcte
                const response = await axios.put(`http://localhost:8040/${selectedTicket.id}/assign/${selectedUser.id}`);
                
                // Vérification de la réponse de l'API
                console.log('Ticket assigned successfully:', response.data);
                
                // Mettez à jour la liste des tickets avec les données mises à jour
                setTickets(tickets.map(t => t.id === response.data.id ? response.data : t));
                
                // Réinitialisez les états
                setSelectedUser(null);
                setShowUserPopup(false);
            } catch (error) {
                console.error('Error assigning ticket:', error.response ? error.response.data : error.message);
                setError('Error assigning ticket.');
            }
        } else {
            console.warn('No user selected or no ticket selected.');
            setError('No user selected or no ticket selected.');
        }
    };
    
    

    const handlePopupClose = () => {
        setShowPopup(false);
        setSelectedSite(null);
    };

    const handleUserPopupClose = () => {
        setShowUserPopup(false);
        setSelectedUser(null);
        setSelectedTicket(null);
    };

    if (loading) return <p>Loading...</p>;
    if (error) return <p>{error}</p>;

    // Calculate the counts of each importance level
    const importanceLevels = ['very low', 'low', 'normal', 'high', 'very_high'];
    const counts = importanceLevels.reduce((acc, level) => {
        acc[level] = sites.filter(site => site.importance === level).length;
        return acc;
    }, {});

    // Prepare data for the bar chart
    const barChartData = {
        labels: importanceLevels,
        datasets: [
            {
                label: 'Number of Sites',
                data: importanceLevels.map(level => counts[level]),
                backgroundColor: 'rgba(0,123,255,0.6)',
                borderColor: 'rgba(0,123,255,1)',
                borderWidth: 1,
            },
        ],
    };

    const barChartOptions = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top',
            },
            tooltip: {
                callbacks: {
                    label: (context) => `Number of Sites: ${context.raw}`,
                },
            },
        },
        scales: {
            x: {
                beginAtZero: true,
                title: {
                    display: true,
                    text: 'Importance Level',
                },
            },
            y: {
                beginAtZero: true,
                title: {
                    display: true,
                    text: 'Number of Sites',
                },
                ticks: {
                    callback: (value) => Number.isInteger(value) ? value : '', // Display only integer values
                },
            },
        },
        layout: {
            padding: {
                top: 20, // Adjust this value to move the chart down
            },
        },
    };
     // Calculate the counts of each ticket status
     const statusCounts = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'].reduce((acc, status) => {
        acc[status] = tickets.filter(ticket => ticket.status === status).length;
        return acc;
    }, {});

    // Prepare data for the pie chart
    const pieChartData = {
        labels: Object.keys(statusCounts),
        datasets: [
            {
                data: Object.values(statusCounts),
                backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0'],
                borderColor: ['#FFF', '#FFF', '#FFF', '#FFF'],
                borderWidth: 2,
            },
        ],
    };

    const pieChartOptions = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top',
            },
        },
    };
    const handleReminderClick = async (ticket) => {
        console.log('Selected ticket:', ticket); // Log the selected ticket details
    
        if (ticket && ticket.user && ticket.severity) {
            try {
                const emailData = {
                    subject: "Reminder: Action Required",
                    message: `This is a reminder about the ticket with severity: ${ticket.severity}. Ticket details:\n\n` +
                             `Name: ${ticket.assetId}\n` +
                             `Description: ${ticket.description}\n` +
                             `Status: ${ticket.status}\n`
                };
    
                const response = await axios.post(
                    `http://localhost:8040/send/${ticket.user.id}/${ticket.severity}`,
                    emailData
                );
    
                console.log('Email sent:', response.data.message);
            } catch (error) {
                console.error('Error sending reminder email:', error);
                setError('Error sending reminder email.');
            }
        } else {
            setError('Ticket details are missing. Please ensure the ticket has a user and severity level.');
        }
    };
    
    
    
    

    return (
        <div style={styles.container}>
            <SidebarComponent />
            <div style={styles.content}>
                <header style={styles.header}>
                    <h1 style={styles.headerTitle}>SecureFlow Admin Dashboard</h1>
                </header>
                <div style={styles.mainContent}>
                    <div style={styles.tableContainer}>
                        <h1 style={styles.title}>All Sites</h1>
                        <table style={styles.table}>
                            <thead>
                                <tr>
                                    <th style={styles.th}>Name</th>
                                    <th style={styles.th}>Description</th>
                                    <th style={styles.th}>Assets</th>
                                    <th style={styles.th}>Importance</th>
                                    <th style={styles.th}>Last Scan Time</th>
                                </tr>
                            </thead>
                            <tbody>
                                {sites.map((site) => (
                                    <tr key={site.id} style={styles.row}>
                                        <td style={styles.td}>
                                            <a
                                                href="#"
                                                style={{
                                                    ...styles.link,
                                                    ...(hoveredLink === site.id ? styles.linkHover : {}),
                                                }}
                                                onMouseEnter={() => setHoveredLink(site.id)}
                                                onMouseLeave={() => setHoveredLink(null)}
                                                onClick={() => handleSiteClick(site.name)}
                                            >
                                                {site.name}
                                            </a>
                                        </td>
                                        <td style={styles.td}>{site.description}</td>
                                        <td style={styles.td}>{site.assets}</td>
                                        <td style={styles.td}>{site.importance}</td>
                                        <td style={styles.td}>{site.lastScanTime}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    <div style={styles.chartContainer}>
                        <Bar data={barChartData} options={barChartOptions} />
                    </div>
                </div>
            </div>
            {showPopup && (
                <>
                    <div style={styles.overlay} />
                    <div style={styles.popup}>
                        <div style={styles.popupContent}>
                            <span className="popup-close" onClick={handlePopupClose}>&times;</span>
                            <div style={styles.popupBody}>
                                <div style={styles.pieChartContainer}>
                                    <Pie data={pieChartData} options={pieChartOptions} />
                                </div>
                                <div style={styles.ticketContainer}>
                                    <h2 style={styles.popupTitle}>Tickets for {selectedSite}</h2>
                                    <div style={styles.ticketGrid}>
                                        {currentTickets.map((ticket) => (
                                            <div key={ticket.id} style={styles.ticket}>
                                                <div style={styles.ticketInfoSection}>
                                                    <div style={styles.ticketTitle}>Name: {ticket.description}</div>
                                                    <div style={styles.ticketInfo}>
                                                        <strong>Severity:</strong> {ticket.severity}
                                                    </div>
                                                  
                                                    <div style={styles.ticketInfo}>
                                                        <strong>Status:</strong> {ticket.status}
                                                    </div>
                                                </div>
                                                <div style={styles.ticketButtonsSection}>
                                                    <button
                                                        style={styles.assignButton}
                                                        onClick={() => handleAssignButtonClick(ticket)}
                                                    >
                                                        Assign User
                                                    </button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                    <div style={styles.pagination}>
                                        <button
                                            onClick={paginatePrevious}
                                            style={{ 
                                                ...styles.pageButton, 
                                                opacity: currentPage === 1 ? 0.5 : 1, 
                                                cursor: currentPage === 1 ? 'not-allowed' : 'pointer' 
                                            }}
                                            disabled={currentPage === 1}
                                        >
                                            &lt; {/* Flèche gauche */}
                                        </button>
                                        <button
                                            onClick={paginateNext}
                                            style={{ 
                                                ...styles.pageButton, 
                                                opacity: currentPage === Math.ceil(tickets.length / ticketsPerPage) ? 0.5 : 1, 
                                                cursor: currentPage === Math.ceil(tickets.length / ticketsPerPage) ? 'not-allowed' : 'pointer' 
                                            }}
                                            disabled={currentPage === Math.ceil(tickets.length / ticketsPerPage)}
                                        >
                                            &gt; {/* Flèche droite */}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </>
            )}
            {showUserPopup && (
                <div style={styles.userPopup}>
                   <span className="popup-close" onClick={handleUserPopupClose}>&times;</span>
                    <h2 style={styles.userPopupTitle}>Assign Ticket</h2>
                    <select
    style={styles.userSelect}
    onChange={(e) => {
        const user = users.find(u => u.id === parseInt(e.target.value));
        setSelectedUser(user);
    }}
>
    <option value="">Select User</option>
    {users.map(user => (
        <option key={user.id} value={user.id}>{user.username}</option>
    ))}
</select>


                    <button style={styles.assignBtn} onClick={handleUserSelection}>Assign</button>
                </div>
            )}
        </div>
    );
};

const styles = {
    pagination: {
        display: 'flex',
        justifyContent: 'center',
        marginTop: '20px',
    },
    pageButton: {
        backgroundColor: '#007bff',
        color: '#fff',
        border: 'none',
        borderRadius: '5px',
        padding: '10px 15px',
        cursor: 'pointer',
        margin: '0 5px',
    },
    title:{    fontSize: '24px', /* Taille de la police du titre */
        fontWeight: 'bold', /* Rendre le titre en gras */
        color: '#333', /* Couleur du texte du titre */
        marginBottom: '20px', /* Espace sous le titre */
        textAlign: 'center', /* Centrer le texte */
        borderBottom: '2px solid #ddd', /* Bordure sous le titre */
        paddingBottom: '10px', /* Espace sous le titre, avant la bordure */
        width: '100%' /* Prend toute la largeur disponible */},
    container: {
        display: 'flex',
        flexDirection: 'row',
        fontFamily: 'Arial, sans-serif',
        backgroundColor: '#f8f9fa',
        height: '100vh',
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
    mainContent: {
        display: 'flex',
        gap: '20px',
    },
    tableContainer: {
        flex: 1,
        marginRight: '20px',
    },
    chartContainer: {
        marginTop:'35px',
        width: '500px',
        padding: '20px',
        background: '#fff',
        borderRadius: '8px',
        boxShadow: '0 4px 8px rgba(0,0,0,0.1)',
    },
    table: {
        width: '100%',
        borderCollapse: 'collapse',
        marginBottom: '20px',
    },
    th: {
        backgroundColor: '#007bff',
        color: '#fff',
        padding: '10px',
        textAlign: 'center',
    },
    td: {
        padding: '10px',
        textAlign: 'center',
        borderBottom: '1px solid #ddd',
    },
    row: {
        cursor: 'pointer',
    },
    link: {
        color: '#007bff',
        textDecoration: 'none',
    },
    linkHover: {
        textDecoration: 'underline',
    },
    overlay: {
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        backgroundColor: 'rgba(0, 0, 0, 0.5)', // Black overlay
        zIndex: 999, // Just below the popup
    },
    popup: {
        position: 'fixed',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        backgroundColor: '#fff',
        border: '1px solid #dee2e6',
        borderRadius: '4px',
        boxShadow: '0 0 10px rgba(0,0,0,0.2)',
        zIndex: 2000,
        width: '80%',
        maxHeight: '90%',
        overflowY: 'auto',
        height:'700px'
    },
    popupContent: {
        padding: '20px',
    },
    closeBtn: {
        background: '#007bff',
        color: '#fff',
        border: 'none',
        borderRadius: '4px',
        padding: '10px 20px',
        cursor: 'pointer',
        marginBottom: '10px',
    },
    popupTitle: {
        fontSize: '24px', /* Taille de la police du titre */
    fontWeight: 'bold', /* Rendre le titre en gras */
    color: '#333', /* Couleur du texte du titre */
    marginBottom: '20px', /* Espace sous le titre */
    textAlign: 'center', /* Centrer le texte */
    borderBottom: '2px solid #ddd', /* Bordure sous le titre */
    paddingBottom: '10px', /* Espace sous le titre, avant la bordure */
    width: '100%' /* Prend toute la largeur disponible */
    },
    ticketContainer: {
        overflowY: 'auto',
        width:'800px',
        marginLeft:'400px',
        marginRight:'-50px',
        marginTop:'-500px'
        
    },
    ticketGrid: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    ticket: {
        backgroundColor: '#2e86c1 ',
        border: '1px solid #dee2e6',
        borderRadius: '4px',
        margin: '10px',
        width: '365px',
        height:'130px',
        boxSizing: 'border-box',
        padding: '10px',
        display: 'flex',
        flexDirection: 'row',
        overflow: 'hidden', // Ensure contents do not overflow
    },
    ticketInfoSection: {
        flex: '1 1 70%',
        paddingRight: '10px',
        display: 'flex',
        flexDirection: 'column', // Stack information vertically
        justifyContent: 'center',
    },
    ticketButtonsSection: {
        marginTop:'-45px',
        marginRight:'-10px',
        height:'200px',
        flex: '1 1 30%',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'flex-end', // Align buttons to the right
        backgroundColor: '#338dff', // Yellow background
        padding: '10px',
    },
    ticketTitle: {
        fontSize: '14px', // Taille de police ajustée
        fontWeight: 'bold',
        marginBottom: '10px', // Espace entre le titre et les informations
        textAlign: 'left', // Aligne le titre à droite
    },
    ticketInfo: {
        marginBottom: '5px',
        fontSize: '12px',
        textAlign: 'left', // Aligne les informations à droite
    },
    assignButton: {
        backgroundColor: '#ffc107',
        color: '#fff',
        border: 'none',
        borderRadius: '4px',
        padding: '10px',
        cursor: 'pointer',
        marginBottom: '5px',
        width: '100px', // Fill the width of the button section
    },
    reminderButton: {
        backgroundColor: '#ffc107',
        color: '#000',
        border: 'none',
        borderRadius: '4px',
        padding: '10px',
        cursor: 'pointer',
        width: '100px', // Fill the width of the button section
    },
    pieChartContainer: {
        width: '300px', // Adjust width of pie chart
        height: '400px', // Adjust height of pie chart
        marginLeft: '20px',
        marginTop:'100px'
    },
    userPopup: {
        position: 'fixed',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        backgroundColor: '#fff',
        border: '1px solid #dee2e6',
        borderRadius: '8px',
        boxShadow: '0 0 10px rgba(0,0,0,0.2)',
        zIndex: 2000,
        width: '400px',
        padding: '20px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        textAlign: 'center',
    },
    userPopupTitle: {
        fontSize: '20px',
        marginBottom: '20px',
        fontWeight: 'bold',
    },
    userSelect: {
        width: '100%',
        padding: '10px',
        marginBottom: '20px',
        border: '1px solid #ddd',
        borderRadius: '4px',
    },
    buttonGroup: {
        display: 'flex',
        justifyContent: 'center',
        gap: '10px',
        marginTop: 'auto', // Pushes the button group to the bottom of the popup
    },
    assignBtn: {
        backgroundColor: '#ffc107',
        color: '#fff',
        border: 'none',
        borderRadius: '4px',
        padding: '10px 20px',
        cursor: 'pointer',
        fontSize: '16px',
        width: '120px',
    },
    closeBtn: {
        backgroundColor: '#6c757d',
        color: '#fff',
        border: 'none',
        borderRadius: '4px',
        padding: '10px 20px',
        cursor: 'pointer',
        fontSize: '16px',
        width: '120px',
    },
};


export default Sites;