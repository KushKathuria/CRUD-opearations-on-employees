// DOM Elements
const loginSection = document.getElementById('login-section');
const registerSection = document.getElementById('register-section');
const uploadSection = document.getElementById('upload-section');
const resultsSection = document.getElementById('results-section');
const showRegisterLink = document.getElementById('show-register');
const showLoginLink = document.getElementById('show-login');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const uploadForm = document.getElementById('upload-form');
const resumeFile = document.getElementById('resume-file');
const resumeDataDiv = document.getElementById('resume-data');
const newUploadBtn = document.getElementById('new-upload');

// Check if user is already logged in
if (localStorage.getItem('token')) {
    // User is logged in, show upload section
    showSection('upload-section');
} else {
    // User is not logged in, show login section
    showSection('login-section');
}

// Show the targeted section and hide others
function showSection(sectionId) {
    // Hide all sections
    [loginSection, registerSection, uploadSection, resultsSection]
        .forEach(section => {
            if (section) section.style.display = 'none';
        });
    
    // Show the targeted section
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.style.display = 'block';
    }
}

// Toggle between login and register forms
showRegisterLink.addEventListener('click', (e) => {
    e.preventDefault();
    showSection('register-section');
});

showLoginLink.addEventListener('click', (e) => {
    e.preventDefault();
    showSection('login-section');
});

// Login form submission
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        
        if (response.ok) {
            const token = await response.text();
            localStorage.setItem('token', token);
            showSection('upload-section');
            showMessage('Login successful!', 'success');
        } else {
            const error = await response.text();
            showMessage(`Login failed: ${error}`, 'error');
        }
    } catch (error) {
        showMessage(`Login error: ${error.message}`, 'error');
    }
});

// Register form submission
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const username = document.getElementById('reg-username').value;
    const password = document.getElementById('reg-password').value;
    
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password, role: 'ROLE_USER' })
        });
        
        if (response.ok) {
            showMessage('Registration successful! Please login.', 'success');
            showSection('login-section');
        } else {
            const error = await response.text();
            showMessage(`Registration failed: ${error}`, 'error');
        }
    } catch (error) {
        showMessage(`Registration error: ${error.message}`, 'error');
    }
});

// Upload form submission
uploadForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const file = resumeFile.files[0];
    if (!file) {
        showMessage('Please select a file', 'error');
        return;
    }
    
    const formData = new FormData();
    formData.append('file', file);
    
    try {
        showSection('results-section');
        resumeDataDiv.innerHTML = '<p class="loading">Analyzing your resume...</p>';
        
        const response = await fetch('/api/resume/upload', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: formData
        });
        
        if (response.ok) {
            const result = await response.json();
            displayResumeData(result);
        } else {
            const error = await response.text();
            resumeDataDiv.innerHTML = `<p class="error">Upload failed: ${error}</p>`;
        }
    } catch (error) {
        resumeDataDiv.innerHTML = `<p class="error">Upload error: ${error.message}</p>`;
    }
});

// Display resume data
function displayResumeData(data) {
    if (!data || !data.resumeId) {
        resumeDataDiv.innerHTML = '<p class="error">No data to display</p>';
        return;
    }
    
    let html = `
        <div class="resume-field">
            <h3>Upload Status</h3>
            <p class="success">${data.message}</p>
        </div>
        <div class="resume-field">
            <h3>File ID</h3>
            <p>${data.resumeId}</p>
        </div>
    `;
    
    resumeDataDiv.innerHTML = html;
}

// Show message
function showMessage(message, type) {
    const messageDiv = document.createElement('div');
    messageDiv.className = type;
    messageDiv.textContent = message;
    
    // Insert message at the top of the current section
    const currentSection = document.querySelector('.section:not(.hidden)');
    if (currentSection) {
        currentSection.insertBefore(messageDiv, currentSection.firstChild);
        
        // Remove message after 5 seconds
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
        }, 5000);
    }
}

// New upload button
newUploadBtn.addEventListener('click', () => {
    resumeFile.value = '';
    showSection('upload-section');
});

// Logout function
function logout() {
    localStorage.removeItem('token');
    showSection('login-section');
}
