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

    messageDiv.innerHTML = `<div class="${type}">${message}</div>`;
    messageDiv.style.display = 'block';

    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 5000);
}

function formatItinerary(text) {
    // Improved formatting
    let formatted = text
        // Headers
        .replace(/^## (.*?)$/gm, '<h4 class="itinerary-header">$1</h4>')
        .replace(/^### (.*?)$/gm, '<h5 class="itinerary-subheader">$1</h5>')
        // Bold text
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        // Day markers
        .replace(/Day (\d+):/g, '<div class="day-marker">Day $1:</div>')
        // Lists
        .replace(/^- (.*?)$/gm, '<li>$1</li>')
        .replace(/^• (.*?)$/gm, '<li>$1</li>')
        // Line breaks
        .replace(/\n\n/g, '</p><p>')
        .replace(/\n/g, '<br>');

    // Wrap consecutive list items
    formatted = formatted.replace(/(<li>.*?<\/li>(\s*<br>)?)+/g, function(match) {
        return '<ul class="itinerary-list">' + match.replace(/<br>/g, '') + '</ul>';
    });

    return '<div class="itinerary-content"><p>' + formatted + '</p></div>';
}

function setMinDates() {
    const today = new Date().toISOString().split('T')[0];
    const startDateInput = document.getElementById('start-date');
    const endDateInput = document.getElementById('end-date');

    if (startDateInput) {
        startDateInput.min = today;
        startDateInput.value = today; // Set default to today
    }
    if (endDateInput) {
        endDateInput.min = today;
        // Set default end date to 3 days from today
        const defaultEnd = new Date();
        defaultEnd.setDate(defaultEnd.getDate() + 3);
        endDateInput.value = defaultEnd.toISOString().split('T')[0];
    }
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

        const data = await response.json();

        if (data.success === false) {
            throw new Error(data.message || 'Failed to generate itinerary');
        }

        loading.style.display = 'none';
        result.style.display = 'block';

        const itineraryContent = document.getElementById('itinerary-content');
        itineraryContent.innerHTML = formatItinerary(data.itinerary);

        // Add save functionality for logged-in users
        if (isLoggedIn) {
            addSaveItineraryButton(data.itinerary);
        }

    } catch (error) {
        console.error('Error generating route:', error);
        loading.style.display = 'none';
        alert('Failed to generate itinerary: ' + error.message);
    } finally {
        btn.disabled = false;
    }
}

function addSaveItineraryButton(itinerary) {
    const resultDiv = document.getElementById('route-result');
    const existingSaveBtn = document.getElementById('save-itinerary-btn');

    if (!existingSaveBtn) {
        const saveBtn = document.createElement('button');
        saveBtn.id = 'save-itinerary-btn';
        saveBtn.className = 'btn btn-success';
        saveBtn.innerHTML = '💾 Save Itinerary to Trip';
        saveBtn.onclick = () => saveItineraryToTrip(itinerary);
        resultDiv.appendChild(saveBtn);
    }
}

async function saveItineraryToTrip(itinerary) {
    // Pre-fill the notes with the generated itinerary
    const notesField = document.getElementById('trip-notes');
    if (notesField) {
        notesField.value = 'Generated Itinerary:\n\n' + itinerary;
        // Scroll to the add trip form
        document.querySelector('.add-trip-card').scrollIntoView({
            behavior: 'smooth',
            block: 'center'
        });
        showMessage('Itinerary copied to notes. Please select dates and save your trip.', 'info');
    }
}

async function handleAddTrip(e) {
    e.preventDefault();

    // For non-logged in users (shouldn't happen as form is hidden, but just in case)
    if (!isLoggedIn) {
        window.location.href = `/login?redirect=/place/${placeId}`;
        return;
    }

    const startDate = document.getElementById('start-date').value;
    const endDate = document.getElementById('end-date').value;
    const notes = document.getElementById('trip-notes').value;

    // Validate dates
    if (new Date(endDate) < new Date(startDate)) {
        showMessage('End date must be after start date', 'error');
        return;
    }

    // Get token from cookie
    const token = getCookie('token');

    if (!token) {
        showMessage('Session expired. Please login again.', 'error');
        setTimeout(() => {
            window.location.href = `/login?redirect=/place/${placeId}`;
        }, 2000);
        return;
    }

    // Show loading state
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.textContent = 'Adding trip...';

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

        if (data.requireLogin) {
            showMessage('Please login to add trips', 'error');
            setTimeout(() => {
                window.location.href = `/login?redirect=/place/${placeId}`;
            }, 1500);
        } else if (data.success) {
            showMessage('Trip added successfully! Redirecting to My Trips...', 'success');
            document.getElementById('add-trip-form').reset();
            setMinDates(); // Reset date fields
            setTimeout(() => {
                window.location.href = '/my-trips';
            }, 1500);
        } else {
            showMessage(data.message || 'Failed to add trip', 'error');
        }

    } catch (error) {
        console.error('Error adding trip:', error);
        showMessage('An error occurred. Please try again.', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    }
}

function setupEventListeners() {
    // Generate Route Button
    const generateBtn = document.getElementById('generate-route-btn');
    if (generateBtn) {
        generateBtn.addEventListener('click', generateRoute);
    }

    // Add to Trips Form - only for logged-in users
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
            if (endInput.value && endInput.value < this.value) {
                endInput.value = this.value;
            }
        });
    }

    // Login prompt buttons
    const loginButtons = document.querySelectorAll('.login-prompt-card .btn');
    loginButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            if (!this.href) {
                e.preventDefault();
                window.location.href = `/login?redirect=/place/${placeId}`;
            }
        });
    });
}

// ========== STYLING ==========

function addStyles() {
    const style = document.createElement('style');
    style.textContent = `
        .message .success {
            padding: 12px;
            background-color: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .message .error {
            padding: 12px;
            background-color: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
            border-radius: 6px;
        }

        .message .info {
            padding: 12px;
            background-color: #d1ecf1;
            border: 1px solid #bee5eb;
            color: #0c5460;
            border-radius: 6px;
        }

        .itinerary-content {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-top: 15px;
        }

        .day-marker {
            font-weight: bold;
            color: #2c3e50;
            margin: 20px 0 10px 0;
            font-size: 1.1em;
            border-bottom: 2px solid #3498db;
            padding-bottom: 5px;
        }

        .itinerary-header {
            color: #2c3e50;
            margin: 15px 0 10px 0;
            font-size: 1.2em;
        }

        .itinerary-subheader {
            color: #34495e;
            margin: 10px 0 8px 0;
            font-size: 1.1em;
        }

        .itinerary-list {
            margin: 10px 0;
            padding-left: 25px;
        }

        .itinerary-list li {
            margin: 5px 0;
            line-height: 1.6;
        }

        #save-itinerary-btn {
            margin-top: 15px;
            width: 100%;
        }

        .loading {
            text-align: center;
            padding: 30px;
        }

        .spinner {
            border: 3px solid #f3f3f3;
            border-top: 3px solid #3498db;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
            margin: 0 auto 15px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    `;
    document.head.appendChild(style);
}

// ========== INITIALIZATION ==========

document.addEventListener('DOMContentLoaded', function() {
    setupEventListeners();
    setMinDates();
    addStyles();

    // Debug logging
    console.log('Page loaded');
    console.log('User logged in:', isLoggedIn);
    console.log('Place ID:', placeId);

    if (isLoggedIn) {
        const token = getCookie('token');
        console.log('Token exists:', !!token);
    }
});