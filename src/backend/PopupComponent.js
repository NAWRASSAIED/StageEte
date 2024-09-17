import React from 'react';

const PopupComponent = ({ tickets, onClose }) => {
    if (!tickets) return null;

    return (
        <div style={styles.overlay} onClick={onClose}>
            <div style={styles.popup} onClick={(e) => e.stopPropagation()}>
                <h2>Tickets</h2>
                {tickets.length === 0 ? (
                    <p>No tickets available for this site.</p>
                ) : (
                    <ul>
                        {tickets.map((ticket, index) => (
                            <li key={index} style={styles.ticketItem}>
                                <strong>IP:</strong> {ticket.ip} <br />
                                <strong>Severity:</strong> {ticket.severity} <br />
                                <strong>Description:</strong> {ticket.description}
                                <strong>Status:</strong> {ticket.status}
                            </li>
                        ))}
                    </ul>
                )}
                <button onClick={onClose} style={styles.closeButton}>Close</button>
            </div>
        </div>
    );
};

// Styles
const styles = {
    overlay: {
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0,0,0,0.5)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
    },
    popup: {
        backgroundColor: '#fff',
        padding: '20px',
        borderRadius: '8px',
        width: '80%',
        maxWidth: '600px',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
        position: 'relative',
    },
    ticketItem: {
        marginBottom: '10px',
    },
    closeButton: {
        position: 'absolute',
        top: '10px',
        right: '10px',
        backgroundColor: '#007bff',
        color: '#fff',
        border: 'none',
        borderRadius: '4px',
        padding: '5px 10px',
        cursor: 'pointer',
    },
};

export default PopupComponent;
