// src/backend/SidebarComponent.js
import React, { useState, useEffect } from 'react';

const SidebarComponent = () => {
  const [sidebarContent, setSidebarContent] = useState('');

  useEffect(() => {
    const fetchSidebarHTML = async () => {
      try {
        const response = await fetch('/backend/sidebar.html'); // URL de votre fichier HTML
        if (!response.ok) {
          throw new Error('Failed to load sidebar HTML');
        }
        const html = await response.text();
        setSidebarContent(html);
      } catch (error) {
        console.error('Error fetching sidebar HTML:', error);
        // Gérez les erreurs de chargement ici
      }
    };

    fetchSidebarHTML();
  }, []);

  return (
    <div dangerouslySetInnerHTML={{ __html: sidebarContent }} />
  );
};

export default SidebarComponent;
