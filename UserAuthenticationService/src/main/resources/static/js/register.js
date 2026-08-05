document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('register-form');
    const messageArea = document.getElementById('message-area');

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const username = registerForm.username.value;
        const email = registerForm.email.value;
        const password = registerForm.password.value;
        const confirmPassword = registerForm.confirmPassword.value;

        if (password !== confirmPassword) {
            showMessage('Passwords do not match.', 'error');
            return;
        }

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ 
                    username, 
                    email, 
                    password, 
                    confirmPassword 
                }),
            });

            const text = await response.text();

            if (!response.ok) {
                throw new Error(text || 'Registration failed');
            }

            showMessage(text, 'success');
            registerForm.reset(); // Clear the form

        } catch (error) {
            showMessage(`Error: ${error.message}`, 'error');
        }
    });

    function showMessage(message, type) {
        messageArea.textContent = message;
        messageArea.className = `message-area ${type}`;
    }
});
