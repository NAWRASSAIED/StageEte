import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

function SignInComponent() {
    const navigate = useNavigate();
    const [tickets, setTickets] = useState([]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const formData = new FormData(e.target);
        const email = formData.get('email');
        const password = formData.get('password');

        try {
            const response = await fetch('http://localhost:8040/api/auth/signIn', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            if (response.ok) {
                const data = await response.json();
                console.log('Login successful:', data);

                const userId = data.id; // Extract userId from the login response
                console.log('User ID from login:', userId);

                // Store token and userId in localStorage
                localStorage.setItem('authToken', data.token);
                localStorage.setItem('userId', userId);

                // Fetch the current user details using the userId
                const userResponse = await fetch(`http://localhost:8040/api/user/Currentuser/${userId}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${data.token}`
                    }
                });

                if (userResponse.ok) {
                    const currentUser = await userResponse.json();
                    console.log('Current User:', currentUser);

                    const role = currentUser.roles && currentUser.roles.length > 0 ? currentUser.roles[0].name : undefined;
                    if (role === 'ADMIN') {
                        console.log('Role is ADMIN. Redirecting to /sites');
                        navigate('/sites');
                    } else if (role) {
                        console.log(`Role is ${role}. Redirecting to /client`);
                        navigate('/client');
                    } else {
                        console.error('Role is undefined or not recognized');
                    }

                } else {
                    console.error('Failed to fetch current user:', userResponse.statusText);
                }
            } else {
                console.error('Login error:', response.statusText);
            }
        } catch (error) {
            console.error('Network error:', error);
        }
    };

    useEffect(() => {
        const fetchForm = async () => {
            try {
                const response = await fetch('/frontend/SignInForm.html');
                const formHtml = await response.text();

                const signInFormContainer = document.getElementById('signInFormContainer');
                if (signInFormContainer) {
                    signInFormContainer.innerHTML = formHtml;

                    const signInForm = document.getElementById('signInForm');
                    if (signInForm) {
                        signInForm.addEventListener('submit', handleSubmit);
                    }
                }
            } catch (error) {
                console.error('Failed to fetch SignInForm.html:', error);
            }
        };

        fetchForm();

        return () => {
            const signInForm = document.getElementById('signInForm');
            if (signInForm) {
                signInForm.removeEventListener('submit', handleSubmit);
            }
        };
    }, []);

    return (
        <div>
            <div id="signInFormContainer" />
        </div>
    );
}

export default SignInComponent;

