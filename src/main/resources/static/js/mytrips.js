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
        const response = await fetch('/api/my-trips', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'same-origin' // Send HttpOnly cookie automatically
        });

        console.log('Response status:', response.status);

        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = '/login?next=/my-trips';
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

    // ✅ Format notes section
    let notesHtml = '';
    if (trip.notes && trip.notes.trim() !== '') {
        const maxPreviewLength = 150;
        const truncatedNotes = trip.notes.length > maxPreviewLength
            ? trip.notes.substring(0, maxPreviewLength) + '...'
            : trip.notes;

        notesHtml = `
            <div class="trip-notes">
                <h4>📝 Itinerary</h4>
                <div class="notes-preview">
                    <pre>${escapeHtml(truncatedNotes)}</pre>
                </div>
                ${trip.notes.length > maxPreviewLength
                    ? `<button class="btn-view-notes" data-trip-id="${trip.id}" data-place-name="${escapeHtml(trip.placeName)}" data-notes="${escapeHtml(trip.notes)}">View Full Itinerary</button>`
                    : ''}
            </div>
        `;
    }

    console.log('Creating card for:', trip.placeName, 'PlaceID:', trip.placeId, 'TripID:', trip.id);

    card.innerHTML = `
        <div class="trip-card-header">
            <span class="trip-name">${escapeHtml(trip.placeName)}</span>
            <span class="status ${statusClass}">${statusLabel}</span>
        </div>
        <img src="${trip.imageUrl || 'https://via.placeholder.com/300x200?text=No+Image'}"
             alt="${escapeHtml(trip.placeName)}"
             onerror="this.src='https://via.placeholder.com/300x200?text=No+Image'">
        <div class="trip-dates">${trip.dateRange}</div>

        ${notesHtml}

        <div class="trip-buttons">
            <button class="view-btn" data-place-id="${trip.placeId}">View Place</button>
            <button class="edit-btn" data-trip-id="${trip.id}">Delete Trip</button>
        </div>
    `;

    // Add event listeners
    const viewBtn = card.querySelector('.view-btn');
    const deleteBtn = card.querySelector('.edit-btn');
    const viewNotesBtn = card.querySelector('.btn-view-notes');

    viewBtn.addEventListener('click', function() {
        const placeId = this.getAttribute('data-place-id');
        console.log('View button clicked - navigating to place:', placeId);
        if (placeId) {
            window.location.href = `/place/${placeId}`;
        } else {
            console.error('No placeId found!');
        }
    });

    deleteBtn.addEventListener('click', function() {
        const tripId = this.getAttribute('data-trip-id');
        deleteTrip(tripId);
    });

    if (viewNotesBtn) {
        viewNotesBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            const placeName = this.getAttribute('data-place-name');
            const notes = this.getAttribute('data-notes');
            showNotesModal(placeName, notes);
        });
    }

    return card;
}

// Delete trip
async function deleteTrip(tripId) {
    if (!confirm('Are you sure you want to delete this trip?')) {
        return;
    }

    try {
        const response = await fetch(`/api/my-trips/${tripId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'same-origin'
        });

        if (response.ok) {
            loadTrips();
        } else {
            alert('Failed to delete trip. Please try again.');
        }

    } catch (error) {
        console.error('Error deleting trip:', error);
        alert('An error occurred while deleting the trip.');
    }
}

// Show notes in modal
function showNotesModal(placeName, notes) {
    // Remove any existing modal
    const existingModal = document.querySelector('.notes-modal');
    if (existingModal) {
        existingModal.remove();
    }

    const modal = document.createElement('div');
    modal.className = 'notes-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h2>📝 Itinerary: ${escapeHtml(placeName)}</h2>
                <button class="modal-close" onclick="this.closest('.notes-modal').remove()">×</button>
            </div>
            <div class="modal-body">
                <pre>${escapeHtml(notes)}</pre>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary" onclick="this.closest('.notes-modal').remove()">Close</button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    // Close on background click
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.remove();
        }
    });

    // Close on Escape key
    document.addEventListener('keydown', function closeOnEscape(e) {
        if (e.key === 'Escape') {
            modal.remove();
            document.removeEventListener('keydown', closeOnEscape);
        }
    });
}

// Utility: Escape HTML to prevent XSS
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}