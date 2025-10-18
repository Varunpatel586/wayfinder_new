// Load trips on page load
document.addEventListener('DOMContentLoaded', function() {
    loadTrips();
});

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
        const response = await fetch('/api/my-trips');

        if (!response.ok) {
            throw new Error('Failed to load trips');
        }

        const trips = await response.json();

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
            <span class="status ${statusClass}">${statusLabel}