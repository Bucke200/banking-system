const API_BASE_URL = 'http://localhost:8080/api'; // Adjust if your backend runs elsewhere
let jwtToken = localStorage.getItem('jwtToken');
let currentUsername = localStorage.getItem('username');

// --- UI Update Functions ---

function showSection(sectionId, display = 'block') {
    const section = document.getElementById(sectionId);
    if (section) {
        section.style.display = display;
    }
}

function hideSection(sectionId) {
    const section = document.getElementById(sectionId);
    if (section) {
        section.style.display = 'none';
    }
}

function displayMessage(elementId, message, isError = false) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.style.color = isError ? 'red' : 'green';
        // Clear message after a few seconds
        setTimeout(() => { element.textContent = ''; }, 5000);
    }
}

function updateLoginState() {
    if (jwtToken) {
        hideSection('auth-section');
        showSection('logged-in-section');
        document.getElementById('welcome-username').textContent = currentUsername || 'User';
        fetchAccounts(); // Fetch accounts automatically on login/refresh
    } else {
        showSection('auth-section');
        hideSection('logged-in-section');
        document.getElementById('welcome-username').textContent = '';
        // Clear sensitive data areas on logout
        document.getElementById('accounts-list').innerHTML = '';
        document.getElementById('history-list').innerHTML = '';
    }
}

// --- API Helper ---

async function apiRequest(endpoint, method = 'GET', body = null, requiresAuth = true) {
    const headers = {
        'Content-Type': 'application/json',
    };
    if (requiresAuth && jwtToken) {
        headers['Authorization'] = `Bearer ${jwtToken}`;
    }

    const config = {
        method: method,
        headers: headers,
    };

    if (body) {
        config.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
        const responseData = await response.text(); // Read response body as text first

        if (!response.ok) {
            // Try to parse error message if JSON, otherwise use text
            let errorMessage = responseData;
            try {
                const errorJson = JSON.parse(responseData);
                errorMessage = errorJson.message || errorJson.error || JSON.stringify(errorJson);
            } catch (e) {
                // Ignore if parsing fails, use the raw text
            }
             console.error(`API Error (${response.status}): ${errorMessage}`);
            throw new Error(errorMessage || `HTTP error! status: ${response.status}`);
        }

        // If response is OK, try to parse as JSON, otherwise return text
        try {
            return JSON.parse(responseData);
        } catch (e) {
            return responseData; // Return as text if not valid JSON (e.g., simple success messages)
        }

    } catch (error) {
        console.error('API Request Failed:', error);
        throw error; // Re-throw the error to be caught by the calling function
    }
}

// --- Authentication ---

async function registerUser() {
    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const messageEl = 'register-message';

    if (!username || !email || !password) {
        displayMessage(messageEl, 'All fields are required.', true);
        return;
    }

    try {
        const result = await apiRequest('/auth/register', 'POST', { username, email, password }, false);
        displayMessage(messageEl, result || 'Registration successful!', false); // Use text response if available
        // Optionally clear form
        document.getElementById('reg-username').value = '';
        document.getElementById('reg-email').value = '';
        document.getElementById('reg-password').value = '';
    } catch (error) {
        displayMessage(messageEl, `Registration failed: ${error.message}`, true);
    }
}

async function loginUser() {
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    const messageEl = 'login-message';

    if (!username || !password) {
        displayMessage(messageEl, 'Username and password are required.', true);
        return;
    }

    try {
        const result = await apiRequest('/auth/login', 'POST', { username, password }, false);
        if (result.jwt) {
            jwtToken = result.jwt;
            currentUsername = username; // Store username for display
            localStorage.setItem('jwtToken', jwtToken);
            localStorage.setItem('username', currentUsername);
            displayMessage(messageEl, 'Login successful!', false);
            updateLoginState();
            // Clear form
             document.getElementById('login-username').value = '';
             document.getElementById('login-password').value = '';
        } else {
            throw new Error('JWT token not received.');
        }
    } catch (error) {
        displayMessage(messageEl, `Login failed: ${error.message}`, true);
        logoutUser(); // Ensure clean state on login failure
    }
}

function logoutUser() {
    jwtToken = null;
    currentUsername = null;
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('username');
    updateLoginState();
}

// --- Accounts ---

async function fetchAccounts() {
    const listEl = document.getElementById('accounts-list');
    const messageEl = 'accounts-message';
    listEl.innerHTML = 'Loading...'; // Indicate loading
    displayMessage(messageEl, ''); // Clear previous messages

    try {
        const accounts = await apiRequest('/accounts', 'GET');
        listEl.innerHTML = ''; // Clear loading/previous content
        if (accounts && accounts.length > 0) {
            accounts.forEach(acc => {
                const div = document.createElement('div');
                div.innerHTML = `Account: <strong>${acc.accountNumber}</strong> - Balance: ${acc.balance.toFixed(2)}`;
                listEl.appendChild(div);
            });
        } else {
            listEl.innerHTML = 'No accounts found.';
        }
    } catch (error) {
        listEl.innerHTML = ''; // Clear loading message on error
        displayMessage(messageEl, `Failed to fetch accounts: ${error.message}`, true);
        if (error.message.includes('401') || error.message.includes('403') || error.message.includes('Forbidden')) {
             logoutUser(); // Log out if token is invalid/expired
        }
    }
}

async function createAccount() {
    const initialBalanceInput = document.getElementById('initial-balance');
    const messageEl = 'create-account-message';
    let initialBalance = initialBalanceInput.value ? parseFloat(initialBalanceInput.value) : null;

    // Basic validation for negative balance
    if (initialBalance !== null && initialBalance < 0) {
        displayMessage(messageEl, 'Initial balance cannot be negative.', true);
        return;
    }

    const body = initialBalance !== null ? { initialBalance: initialBalance } : {}; // Send empty body if no balance specified

    try {
        const newAccount = await apiRequest('/accounts', 'POST', body);
        displayMessage(messageEl, `Account ${newAccount.accountNumber} created successfully!`, false);
        initialBalanceInput.value = ''; // Clear input
        fetchAccounts(); // Refresh the list
    } catch (error) {
        displayMessage(messageEl, `Failed to create account: ${error.message}`, true);
         if (error.message.includes('401') || error.message.includes('403') || error.message.includes('Forbidden')) {
             logoutUser(); // Log out if token is invalid/expired
        }
    }
}


// --- Transactions ---

async function transferFunds() {
    const fromAccountNumber = document.getElementById('transfer-from').value;
    const toAccountNumber = document.getElementById('transfer-to').value;
    const amountInput = document.getElementById('transfer-amount');
    const description = document.getElementById('transfer-description').value;
    const messageEl = 'transfer-message';

    if (!fromAccountNumber || !toAccountNumber || !amountInput.value) {
        displayMessage(messageEl, 'From Account, To Account, and Amount are required.', true);
        return;
    }

    const amount = parseFloat(amountInput.value);
    if (isNaN(amount) || amount <= 0) {
        displayMessage(messageEl, 'Invalid amount. Must be a positive number.', true);
        return;
    }

    try {
        const result = await apiRequest('/transactions/transfer', 'POST', {
            fromAccountNumber,
            toAccountNumber,
            amount,
            description
        });
        displayMessage(messageEl, `Transfer successful! Transaction ID: ${result.id}`, false);
        // Clear form
        document.getElementById('transfer-from').value = '';
        document.getElementById('transfer-to').value = '';
        amountInput.value = '';
        document.getElementById('transfer-description').value = '';
        fetchAccounts(); // Refresh account balances
    } catch (error) {
        displayMessage(messageEl, `Transfer failed: ${error.message}`, true);
         if (error.message.includes('401') || error.message.includes('403') || error.message.includes('Forbidden')) {
             logoutUser(); // Log out if token is invalid/expired
        }
    }
}

async function fetchHistory() {
    const accountNumber = document.getElementById('history-account').value;
    const listEl = document.getElementById('history-list');
    const messageEl = 'history-message';

    if (!accountNumber) {
        displayMessage(messageEl, 'Please enter an account number.', true);
        return;
    }

    listEl.innerHTML = 'Loading history...';
    displayMessage(messageEl, '');

    try {
        const history = await apiRequest(`/transactions/${accountNumber}`, 'GET');
        listEl.innerHTML = ''; // Clear loading

        if (history && history.length > 0) {
            const table = document.createElement('table');
            table.innerHTML = `
                <thead>
                    <tr>
                        <th>Timestamp</th>
                        <th>Type</th>
                        <th>From</th>
                        <th>To</th>
                        <th>Amount</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            `;
            const tbody = table.querySelector('tbody');
            history.forEach(tx => {
                const row = tbody.insertRow();
                row.innerHTML = `
                    <td>${new Date(tx.timestamp).toLocaleString()}</td>
                    <td>${tx.type}</td>
                    <td>${tx.fromAccountNumber || '-'}</td>
                    <td>${tx.toAccountNumber}</td>
                    <td>${tx.amount.toFixed(2)}</td>
                    <td>${tx.description || '-'}</td>
                `;
            });
            listEl.appendChild(table);
        } else {
            listEl.innerHTML = 'No transaction history found for this account.';
        }
    } catch (error) {
        listEl.innerHTML = ''; // Clear loading
        displayMessage(messageEl, `Failed to fetch history: ${error.message}`, true);
         if (error.message.includes('401') || error.message.includes('403') || error.message.includes('Forbidden')) {
             logoutUser(); // Log out if token is invalid/expired
        }
    }
}


// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    updateLoginState();
});
