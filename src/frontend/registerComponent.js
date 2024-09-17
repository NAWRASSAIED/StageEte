import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function RegisterUserComponent() {
    const navigate = useNavigate();

    useEffect(() => {
        const fetchForm = async () => {
            try {
                const response = await fetch('/frontend/RegisterForm.html');
                const formHtml = await response.text();

                const registerFormContainer = document.getElementById('registerFormContainer');
                if (registerFormContainer) {
                    registerFormContainer.innerHTML = formHtml;

                    const registerForm = document.getElementById('registerForm');
                    if (registerForm) {
                        registerForm.addEventListener('submit', handleSubmit);
                    }
                }
            } catch (error) {
                console.error('Failed to fetch RegisterForm.html:', error);
            }
        };

        fetchForm();

        return () => {
            const registerForm = document.getElementById('registerForm');
            if (registerForm) {
                registerForm.removeEventListener('submit', handleSubmit);
            }
        };
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const formData = new FormData(e.target);
        const user = {
            name: formData.get('name'),
            lastname: formData.get('lastname'),
            username: formData.get('username'),
            email: formData.get('email'),
            password: formData.get('password'),
            address: formData.get('address'),
            number: formData.get('number'),
        };
        const roleName = formData.get('roleName');

        try {
            const response = await fetch(`http://localhost:8040/api/auth/signup/employee/${roleName}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(user)
            });

            if (response.ok) {
                const data = await response.json();
                console.log(data);

                // Show notification for NORMAL role
                if (roleName === 'NORMAL') {
                    toast.success('Registration successful! Sign in Now...');
                }

                // Redirection based on roleName
                if (roleName === 'ADMIN') {
                    navigate('/sites'); 
                } else if (roleName === 'NORMAL') {
                    setTimeout(() => {
                        navigate('/signin');
                    }, 3000); // 3 seconds delay to show the toast
                }
            } else {
                console.error('Registration error:', response.statusText);
            }
        } catch (error) {
            console.error('Network error:', error);
        }
    };

    return (
        <div>
            <div id="registerFormContainer" />
            <ToastContainer />
        </div>
    );
}

export default RegisterUserComponent;

