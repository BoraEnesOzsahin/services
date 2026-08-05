document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const messageArea = document.getElementById('message-area');
    const tokenCard = document.getElementById('token-card');
    const tokenDisplay = document.getElementById('token-display');
    const testPrivateBtn = document.getElementById('test-private-btn');

    let accessToken = null;

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const username = loginForm.username.value;
        const password = loginForm.password.value;

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error_description || 'Login failed');
            }

            accessToken = data.access_token;
            showMessage('Login successful!', 'success');
            tokenDisplay.value = accessToken;
            tokenCard.style.display = 'block';

        } catch (error) {
            showMessage(`Error: ${error.message}`, 'error');
            tokenCard.style.display = 'none';
        }
    });

    testPrivateBtn.addEventListener('click', async () => {
        if (!accessToken) {
            showMessage('No access token available. Please login first.', 'error');
            return;
        }

        try {
            const response = await fetch('/private/hello', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${accessToken}`,
                },
            });

            const text = await response.text();

            if (!response.ok) {
                throw new Error(text || 'Failed to access private endpoint');
            }

            showMessage(`Private endpoint says: "${text}"`, 'success');

        } catch (error) {
            showMessage(`Error: ${error.message}`, 'error');

        }
    });

    function showMessage(message, type) {
        messageArea.textContent = message;
        messageArea.className = `message-area ${type}`;
    }
});
