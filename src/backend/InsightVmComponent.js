import React, { useEffect, useState } from 'react';
import SidebarComponent from './SidebarComponent'; // Import the SidebarComponent

const TicketList = () => {
    const [tickets, setTickets] = useState([]);
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedTicket, setSelectedTicket] = useState(null);
    const [showPopup, setShowPopup] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);

    // Pagination states
    const [currentPage, setCurrentPage] = useState(1);
    const ticketsPerPage = 8;

    // Fetch tickets and users from the backend
    useEffect(() => {
        const fetchTickets = async () => {
            try {
                const response = await fetch('http://localhost:8040/findAllTickets');
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                const contentType = response.headers.get('Content-Type');
                if (contentType && contentType.includes('application/json')) {
                    const data = await response.json();
                    setTickets(data);
                } else {
                    throw new Error('Expected JSON response but received ' + contentType);
                }
            } catch (error) {
                setError(error.message);
            } finally {
                setLoading(false);
            }
        };

        const fetchUsers = async () => {
            try {
                const response = await fetch('http://localhost:8040/users');
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                const contentType = response.headers.get('Content-Type');
                if (contentType && contentType.includes('application/json')) {
                    const data = await response.json();
                    setUsers(data);
                } else {
                    throw new Error('Expected JSON response but received ' + contentType);
                }
            } catch (error) {
                setError(error.message);
            }
        };

        fetchTickets();
        fetchUsers();
    }, []);

    // Calculate the tickets to display on the current page
    const indexOfLastTicket = currentPage * ticketsPerPage;
    const indexOfFirstTicket = indexOfLastTicket - ticketsPerPage;
    const currentTickets = tickets.slice(indexOfFirstTicket, indexOfLastTicket);

    // Handle the "Assign User" button click
    const handleAssignButtonClick = (ticket) => {
        setSelectedTicket(ticket);
        setShowPopup(true);
    };

    // Handle user selection and update ticket assignment
    const handleUserSelection = async () => {
        if (selectedTicket && selectedUser) {
            try {
                const response = await fetch(`http://localhost:8040/${selectedTicket.id}/assign/${selectedUser.id}`, {
                    method: 'PUT',
                });
                
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                
                // Check the response data
                const updatedTicket = await response.json();
                if (updatedTicket && updatedTicket.id) {
                    setTickets(tickets.map(t => t.id === updatedTicket.id ? updatedTicket : t));
                    setShowPopup(false);
                    setSelectedUser(null); // Clear the selected user
                } else {
                    throw new Error('Unexpected response format');
                }
            } catch (error) {
                console.error('Error while assigning ticket:', error);
                setError(error.message);
            }
        } else {
            console.error('Selected ticket or user is missing');
        }
    };
    
    // Handle closing the popup
    const handlePopupClose = () => {
        setShowPopup(false);
        setSelectedUser(null);
    };

    // Pagination button handlers
    const handlePageChange = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error}</p>;

    // Pagination logic
    const totalPages = Math.ceil(tickets.length / ticketsPerPage);

    return (
        <div style={{ display: 'flex' }}>
            <SidebarComponent /> {/* Add the SidebarComponent */}
            <div style={{ marginLeft: '300px', width: '100%', marginTop: '0px' }}>
                <style>
                    {`
                    .container {
                        max-width: 1200px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header-design {
                        width: 100%;
                        height: 80px;
                        background: linear-gradient(90deg, rgba(0,123,255,1) 0%, rgba(0,162,255,1) 50%, rgba(0,191,255,1) 100%);
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        color: white;
                        font-size: 1.5em;
                        font-weight: bold;
                        text-transform: uppercase;
                        letter-spacing: 2px;
                        border-radius: 15px;
                        margin-bottom: 20px;
                    }
                    .ticket-container {
                        padding: 20px;
                        margin: 10px auto;
                    }
                    .ticket-grid {
                        display: grid;
                        grid-template-columns: repeat(4, 1fr);
                        gap: 20px;
                    }
                    .ticket {
                        padding: 20px;
                        border-radius: 15px;
                        box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
                        transition: transform 0.3s, box-shadow 0.3s;
                        max-width: 300px;
                        margin: 0 auto;
                    }
                      /* Couleurs avec dégradé clair pour chaque ligne */
                      .ticket:nth-child(8n+1),
                      .ticket:nth-child(8n+3),
                      .ticket:nth-child(8n+6),
                      .ticket:nth-child(8n+8) {
                          background: #d0e8ff;
                      }

                      .ticket:nth-child(8n+2),
                      .ticket:nth-child(8n+4),
                      .ticket:nth-child(8n+5),
                      .ticket:nth-child(8n+7) {
                          background: linear-gradient(90deg, rgba(153, 210, 255, 1) 0%, rgba(178, 226, 255, 1) 50%, rgba(204, 229, 255, 1) 100%);
                      }

                    .ticket:hover {
                        transform: translateY(-10px);
                        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
                    }
                  .ticket-title {
                          font-size: 1.3em;
                          font-weight: bold;
                          margin-bottom: 10px;
                          color:#000080; 
                          padding-bottom: 5px;
                          transition: color 0.3s ease, border-bottom 0.3s ease;
                      }


                      .ticket-info {
                          margin-bottom: 10px;
                          color: #666;
                          font-size: 1.1em;
                      }

                      .ticket-info strong {
                          color: #333;
                          font-weight: bold;
                      }

                      .tickets-title {
                          text-align: center;
                          font-size: 2em;
                          font-weight: bold;
                          color: #000066;
                          text-transform: uppercase;
                          letter-spacing: 2px;
                          transition: color 0.3s ease, border-bottom 0.3s ease;
                      }
                      .assign-button {
                          margin-top: 10px;
                          padding: 10px;
                          border: none;
                          border-radius: 5px;
                          background-color: #007bff;
                          color: white;
                          cursor: pointer;
                          font-size: 1em;
                          transition: background-color 0.3s, transform 0.3s;
                      }
                      .assign-button:hover {
                          background-color: #0056b3;
                          transform: scale(1.05);
                      }
                      .popup {
                          position: fixed;
                          top: 0;
                          left: 0;
                          width: 100%;
                          height: 100%;
                          background: rgba(0, 0, 0, 0.5);
                          display: flex;
                          justify-content: center;
                          align-items: center;
                          z-index: 1000;
                          transition: opacity 0.3s ease-in-out;
                      }
                      .popup-content {
                          background: white;
                          padding: 20px;
                          border-radius: 10px;
                          width: 80%;
                          max-width: 500px;
                          box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
                          transition: transform 0.3s ease-in-out;
                          transform: scale(0.9);
                          display: flex;
                          flex-direction: column;
                          align-items: center;
                      }
                      .popup-content.show {
                          transform: scale(1);
                      }
                      .popup-content h2 {
                          margin-top: 0;
                          font-size: 1.5em;
                          color: #333;
                      }
                      .popup-content button {
                          padding: 10px;
                          border: none;
                          border-radius: 5px;
                          background-color: #007bff;
                          color: white;
                          cursor: pointer;
                          font-size: 1em;
                          transition: background-color 0.3s, transform 0.3s;
                      }
                      .popup-content button:hover {
                          background-color: #0056b3;
                          transform: scale(1.05);
                      }
                      .popup-content .button-group {
                          display: flex;
                          gap: 10px; /* Adjust the space between buttons */
                          margin-top: 10px;
                      }
                      .popup-content .assign-btn {
                          background-color: #007bff;
                      }
                      .popup-content .close-btn {
                          background-color: #dc3545;
                      }
                      .popup-content .close-btn:hover {
                          background-color: #c82333;
                      }
                      select {
                          width: 100%;
                          padding: 10px;
                          border: 1px solid #ddd;
                          border-radius: 5px;
                          font-size: 1em;
                          color: #333;
                          transition: border-color 0.3s;
                          margin-bottom: 10px; /* Add margin between select and buttons */
                      }
                      select:focus {
                          border-color: #007bff;
                          outline: none;
                      }
                    
                    .pagination {
                        display: flex;
                        justify-content: center;
                        margin: 20px 0;
                    }
                    .pagination button {
                        padding: 10px;
                        border: 1px solid #ddd;
                        border-radius: 5px;
                        background-color: #f8f9fa;
                        color: #007bff;
                        cursor: pointer;
                        font-size: 1em;
                        margin: 0 5px;
                        transition: background-color 0.3s, color 0.3s;
                    }
                    .pagination button:hover {
                        background-color: #007bff;
                        color: white;
                    }
                    .pagination .disabled {
                        cursor: not-allowed;
                        color: #ccc;
                    }
                    `}
                </style>
                <div className="container">
                    <div className="header-design">SecureFlow Admin Dashboard</div>
                    <h1 className="tickets-title">Ticket Dashboard</h1>
                    <div className="ticket-container">
                        <div className="ticket-grid">
                            {currentTickets.map((ticket) => (
                                <div key={ticket.id} className="ticket">
                                    <div className="ticket-title">IP: {ticket.assetId}</div>
                                    <div className="ticket-info">
                                        <strong>Severity:</strong> {ticket.severity}
                                    </div>
                                    <div className="ticket-info">
                                        <strong>Description:</strong> {ticket.description}
                                    </div>
                                    <button
                                        className="assign-button"
                                        onClick={() => handleAssignButtonClick(ticket)}
                                    >
                                        Assign User
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>
                    <div className="pagination">
                        <button
                            onClick={() => handlePageChange(currentPage - 1)}
                            disabled={currentPage === 1}
                            className={currentPage === 1 ? 'disabled' : ''}
                        >
                            Previous
                        </button>
                        {[...Array(totalPages).keys()].map((pageNumber) => (
                            <button
                                key={pageNumber + 1}
                                onClick={() => handlePageChange(pageNumber + 1)}
                                className={pageNumber + 1 === currentPage ? 'active' : ''}
                            >
                                {pageNumber + 1}
                            </button>
                        ))}
                        <button
                            onClick={() => handlePageChange(currentPage + 1)}
                            disabled={currentPage === totalPages}
                            className={currentPage === totalPages ? 'disabled' : ''}
                        >
                            Next
                        </button>
                    </div>
                </div>
            </div>
            {showPopup && (
                <div className="popup">
                    <div className={`popup-content ${showPopup ? 'show' : ''}`}>
                        <h2>Assign Ticket to User</h2>
                        <select
                            value={selectedUser ? selectedUser.id : ''}
                            onChange={(e) => setSelectedUser(users.find(user => user.id === parseInt(e.target.value)))}
                        >
                            <option value="">Select User</option>
                            {users.map((user) => (
                                <option key={user.id} value={user.id}>
                                    {user.username}
                                </option>
                            ))}
                        </select>
                        <div className="button-group">
                            <button className="assign-btn" onClick={handleUserSelection}>Assign</button>
                            <button className="close-btn" onClick={handlePopupClose}>Close</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default TicketList;