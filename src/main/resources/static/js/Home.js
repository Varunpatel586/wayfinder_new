// Load featured places on page load
document.addEventListener('DOMContentLoaded', function() {
    loadFeaturedPlaces();
});

// Load featured places from API
async function loadFeaturedPlaces() {
    const container = document.getElementById('featured-destinations');
    const loading = document.getElementById('places-loading');

    // Show loading
    if (loading) loading.style.display = 'block';
    container.innerHTML = '';

    try {
        const response = await fetch('/api/featured-places');

        if (!response.ok) {
            throw new Error('Failed to load featured places');
        }

        const places = await response.json();

        // Hide loading
        if (loading) loading.style.display = 'none';

        // Check if any places exist
        if (places.length === 0) {
            container.innerHTML = '<p class="no-places">No featured destinations available at the moment.</p>';
            return;
        }

        // Render places
        places.forEach(place => {
            container.appendChild(createDestinationCard(place));
        });

    } catch (error) {
        console.error('Error loading featured places:', error);
        if (loading) loading.style.display = 'none';
        container.innerHTML = '<p class="error-message">Unable to load destinations. Please try again later.</p>';
    }
}

// Create destination card element
function createDestinationCard(place) {
    const card = document.createElement('div');
    card.className = 'destination-card';

    // Fallback image if none provided
    const imageUrl = place.imageUrl || 'https://via.placeholder.com/400x300?text=No+Image';

    // Format location (City, Country)
    const location = place.country ? `${place.name}, ${place.country}` : place.name;

    // Calculate estimated price (you can modify this logic)
    const estimatedPrice = calculatePrice(place);

    card.innerHTML = `
        <img src="${imageUrl}"
             alt="${place.name}"
             onerror="this.src='https://via.placeholder.com/400x300?text=No+Image'">
        <div class="content">
            <p>${location}</p>
            <div class="price-btn">
                <span>₹${estimatedPrice.toLocaleString('en-IN')}</span>
                <a href="/place/${place.id}" onclick="event.stopPropagation()">View Details</a>
            </div>
        </div>
    `;

    // Add click handler for entire card
    card.addEventListener('click', () => {
        window.location.href = `/place/${place.id}`;
    });

    return card;
}

// Calculate estimated price based on place rating/category
function calculatePrice(place) {
    // Base price
    let basePrice = 30000;

    // Adjust based on rating
    if (place.averageRating) {
        basePrice += (place.averageRating * 5000);
    }

    // Adjust based on category
    if (place.category) {
        if (place.category.includes('city')) basePrice += 10000;
        if (place.category.includes('beach')) basePrice += 5000;
        if (place.category.includes('adventure')) basePrice += 8000;
    }

    // Round to nearest thousand
    return Math.round(basePrice / 1000) * 1000;
}

// Navigation helpers (keeping your existing logic)
function viewLocationDetails(locationName) {
    // This can be kept for backward compatibility if needed
    window.location.href = `/explore?search=${encodeURIComponent(locationName)}`;
}