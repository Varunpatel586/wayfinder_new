// ========== UTILITY FUNCTIONS ==========

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }
    return null;
}

function getAllCookies() {
    const cookies = {};
    document.cookie.split(';').forEach(cookie => {
        const [name, value] = cookie.trim().split('=');
        if (name) cookies[name] = value;
    });
    return cookies;
}

function showMessage(message, type) {
    const messageDiv = document.getElementById('trip-message');
    if (!messageDiv) return;

    messageDiv.textContent = message;
    messageDiv.className = 'message ' + type;
    messageDiv.style.display = 'block';

    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 5000);
}

function formatItinerary(text) {
    return text
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n## (.*?)\n/g, '\n<h4>$1</h4>\n')
        .replace(/\n### (.*?)\n/g, '\n<h5>$1</h5>\n')
        .replace(/\n- (.*?)\n/g, '\n<li>$1</li>\n')
        .replace(/\n\n/g, '<br><br>')
        .replace(/\n/g, '<br>');
}

function setMinDates() {
    const today = new Date().toISOString().split('T')[0];
    const startDateInput = document.getElementById('start-date');
    const endDateInput = document.getElementById('end-date');

    if (startDateInput) startDateInput.min = today;
    if (endDateInput) endDateInput.min = today;
}

// ========== MAIN FUNCTIONS ==========

async function generateRoute() {
    const btn = document.getElementById('generate-route-btn');
    const loading = document.getElementById('route-loading');
    const result = document.getElementById('route-result');
    const days = document.getElementById('trip-days').value;

    btn.disabled = true;
    loading.style.display = 'block';
    result.style.display = 'none';

    try {
        const response = await fetch('/api/generate-route', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                placeId: placeId,
                days: parseInt(days)
            })
        });

        if (!response.ok) {
            throw new Error('Failed to generate itinerary');
        }

        const data = await response.json();

        loading.style.display = 'none';
        result.style.display = 'block';

        document.getElementById('itinerary-content').innerHTML = formatItinerary(data.itinerary);

    } catch (error) {
        console.error('Error generating route:', error);
        loading.style.display = 'none';
        alert('Failed to generate itinerary. Please try again.');
    } finally {
        btn.disabled = false;
    }
}

async function handleAddTrip(e) {
    e.preventDefault();

    // DEBUG: Check all cookies
    console.log('All cookies:', document.cookie);

    const startDate = document.getElementById('start-date').value;
    const endDate = document.getElementById('end-date').value;
    const notes = document.getElementById('trip-notes').value;

    // Validate dates
    if (new Date(endDate) < new Date(startDate)) {
        showMessage('End date must be after start date', 'error');
        return;
    }

    // Get token from cookie
    let token = getCookie('token');
    console.log('Token found:', token); // DEBUG

    if (!token) {
        // Try localStorage as fallback
        token = localStorage.getItem('token');
        console.log('Token from localStorage:', token);
    }

    if (!token) {
        showMessage('Please login to add trips', 'error');
        setTimeout(() => {
            window.location.href = '/login';
        }, 2000);
        return;
    }

    try {
        const response = await fetch('/api/add-to-trips', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                placeId: placeId,
                startDate: startDate,
                endDate: endDate,
                notes: notes
            })
        });

        const data = await response.json();

        if (data.success) {
            showMessage('Trip added successfully! Redirecting to My Trips...', 'success');
            document.getElementById('add-trip-form').reset();
            setTimeout(() => {
                window.location.href = '/my-trips';
            }, 2000);
        } else {
            showMessage(data.message || 'Failed to add trip', 'error');
        }

    } catch (error) {
        console.error('Error adding trip:', error);
        showMessage('An error occurred. Please try again.', 'error');
    }
}

function setupEventListeners() {
    // Generate Route Button
    const generateBtn = document.getElementById('generate-route-btn');
    if (generateBtn) {
        generateBtn.addEventListener('click', generateRoute);
    }

    // Add to Trips Form
    const addTripForm = document.getElementById('add-trip-form');
    if (addTripForm) {
        addTripForm.addEventListener('submit', handleAddTrip);
    }

    // Date validation
    const startInput = document.getElementById('start-date');
    const endInput = document.getElementById('end-date');

    if (startInput && endInput) {
        startInput.addEventListener('change', function() {
            endInput.min = this.value;
        });
    }
}

// ========== INITIALIZATION ==========

document.addEventListener('DOMContentLoaded', function() {
    setupEventListeners();
    setMinDates();

    // DEBUG: Check if user is logged in
    const token = getCookie('token');
    console.log('Page loaded. User logged in:', !!token);
    console.log('All cookies on load:', getAllCookies());
});