import axios from 'axios';

const API_URL = 'http://localhost:8030'; // Remplacez par l'URL de votre serveur Spring Boot

export const getAssets = async () => {
    const response = await axios.get(`${API_URL}/assets`);
    return response.data;
};

export const getAssetVulnerabilities = async (assetId) => {
    const response = await axios.get(`${API_URL}/assets/${assetId}/vulnerabilities`);
    return response.data;
};
