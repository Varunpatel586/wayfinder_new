// Load trips on page load
document.addEventListener('DOMContentLoaded', function() {
    loadTrips();
});

// Get token from cookie
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }
    return null;
}

// Load trips from API
async function loadTrips() {
    const container = document.getElementById('trip-container');
    const loading = document.getElementById('loading');
    const noTripsMessage = document.getElementById('no-trips-message');

    // Show loading
    loading.style.display = 'block';
    container.innerHTML = '';
    noTripsMessage.style.display = 'none';

    try {
        // Get token from cookie
        const token = getCookie('token');

        const headers = {
            'Content-Type': 'application/json'
        };

        // Add Authorization header if token exists
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch('/api/my-trips', {
            method: 'GET',
            headers: headers,
            credentials: 'include' // Important: sends cookies with request
        });

        console.log('Response status:', response.status);

        if (!response.ok) {
            if (response.status === 401) {
                // Unauthorized - redirect to login
                window.location.href = '/login';
                return;
            }
            throw new Error('Failed to load trips');
        }

        const trips = await response.json();
        console.log('Loaded trips:', trips);

        // Hide loading
        loading.style.display = 'none';

        // Check if any trips exist
        if (trips.length === 0) {
            noTripsMessage.style.display = 'flex';
            return;
        }

        // Separate upcoming and completed trips
        const upcomingTrips = trips.filter(trip => trip.status === 'UPCOMING');
        const completedTrips = trips.filter(trip => trip.status === 'COMPLETED');

        // Render upcoming trips first
        upcomingTrips.forEach(trip => {
            container.appendChild(createTripCard(trip));
        });

        // Then render completed trips
        completedTrips.forEach(trip => {
            container.appendChild(createTripCard(trip));
        });

    } catch (error) {
        console.error('Error loading trips:', error);
        loading.style.display = 'none';
        container.innerHTML = `
            <div class="error-message">
                <p>❌ Error loading trips. Please try again later.</p>
            </div>
        `;
    }
}

// Create trip card element
function createTripCard(trip) {
    const card = document.createElement('div');
    card.className = 'trip-card';

    const statusClass = trip.status.toLowerCase();
    const statusLabel = trip.status === 'UPCOMING' ? 'Upcoming' : 'Completed';

    card.innerHTML = `
        <div class="trip-card-header">
            <span class="trip-name">${trip.placeName}</span>
            <span class="status ${statusClass}">${statusLabel}</span>
        </div>
        <img src="${trip.imageUrl || 'https://via.placeholder.com/300x200?text=No+Image'}"
             alt="${trip.placeName}"
             onerror="this.src='https://via.placeholder.com/300x200?text=No+Image'">
        <div class="trip-dates">${trip.dateRange}</div>
        <div class="trip-buttons">
            <button class="view-btn" onclick="viewTripDetails(${trip.id})">View Details</button>
            <button class="edit-btn" onclick="deleteTrip(${trip.id})">Delete Trip</button>
        </div>
    `;

    return card;
}

// View trip details
function viewTripDetails(tripId) {
    window.location.href = `/trip/${tripId}`;
}

// Delete trip
async function deleteTrip(tripId) {
    if (!confirm('Are you sure you want to delete this trip?')) {
        return;
    }

    try {
        const token = getCookie('token');

        const headers = {
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`/api/my-trips/${tripId}`, {
            method: 'DELETE',
            headers: headers,
            credentials: 'include'
        });

        if (response.ok) {
            // Reload trips after deletion
            loadTrips();
        } else {
            alert('Failed to delete trip. Please try again.');
        }

    } catch (error) {
        console.error('Error deleting trip:', error);
        alert('An error occurred while deleting the trip.');
    }
}