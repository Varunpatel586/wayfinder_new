// Global variables
let currentFilter = 'all';
let searchQuery = '';

// Load places on page load
document.addEventListener('DOMContentLoaded', function() {
    loadPlaces();
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    // Filter buttons
    const filterButtons = document.querySelectorAll('.filters button');
    filterButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            // Update active button
            document.querySelector('.filters .active').classList.remove('active');
            this.classList.add('active');
            
            // Update filter and reload
            currentFilter = this.getAttribute('data-filter');
            searchQuery = ''; // Clear search when filtering
            document.getElementById('search-input').value = '';
            loadPlaces();
        });
    });

    // Search functionality
    const searchBtn = document.getElementById('search-btn');
    const searchInput = document.getElementById('search-input');
    
    searchBtn.addEventListener('click', performSearch);
    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            performSearch();
        }
    });
}

// Perform search
function performSearch() {
    searchQuery = document.getElementById('search-input').value.trim();
    if (searchQuery) {
        // Reset filter to 'all' when searching
        currentFilter = 'all';
        document.querySelector('.filters .active').classList.remove('active');
        document.querySelector('.filters button[data-filter="all"]').classList.add('active');
    }
    loadPlaces();
}

// Load places from API
async function loadPlaces() {
    const container = document.getElementById('destinations-container');
    const loading = document.getElementById('loading');
    const noResults = document.getElementById('no-results');
    
    // Show loading
    loading.style.display = 'block';
    container.innerHTML = '';
    noResults.style.display = 'none';
    
    try {
        // Build URL with query parameters
        let url = '/api/places?';
        if (currentFilter !== 'all') {
            url += `category=${currentFilter}&`;
        }
        if (searchQuery) {
            url += `search=${encodeURIComponent(searchQuery)}&`;
        }
        
        // Fetch places
        const response = await fetch(url);
        
        if (!response.ok) {
            throw new Error('Failed to load places');
        }
        
        const places = await response.json();
        
        // Hide loading
        loading.style.display = 'none';
        
        // Check if any results
        if (places.length === 0) {
            noResults.style.display = 'block';
            return;
        }
        
        // Render places
        places.forEach(place => {
            container.appendChild(createPlaceCard(place));
        });
        
    } catch (error) {
        console.error('Error loading places:', error);
        loading.style.display = 'none';
        container.innerHTML = '<div class="alert alert-danger">Error loading destinations. Please try again.</div>';
    }
}

// Create place card element
function createPlaceCard(place) {
    const card = document.createElement('div');
    card.className = 'card';
    card.setAttribute('data-category', place.category || '');
    
    // Fallback image if none provided
    const imageUrl = place.imageUrl || 'https://via.placeholder.com/300x200?text=No+Image';
    
    // Truncate description to prevent overflow
    const description = place.shortDescription || place.description || 'Explore this amazing destination';
    const truncatedDescription = description.length > 100
        ? description.substring(0, 100) + '...'
        : description;

    card.innerHTML = `
        <img src="${imageUrl}"
             alt="${place.name}"
             onerror="this.src='https://via.placeholder.com/300x200?text=No+Image'">
        <div class="card-content">
            <h3>${place.name}</h3>
            <p>${truncatedDescription}</p>
            ${place.averageRating ? `
                <div class="rating">
                    <span>⭐ ${place.averageRating.toFixed(1)}</span>
                    ${place.reviewCount ? `<span class="review-count">(${place.reviewCount} reviews)</span>` : ''}
                </div>
            ` : ''}
            ${place.country ? `
                <div class="location">
                    <span>📍 ${place.country}</span>
                </div>
            ` : ''}
            ${place.address ? `
                <div class="address">
                    <span>${place.address}</span>
                </div>
            ` : ''}
        </div>
    `;

    // Add click handler to navigate to place details
    card.addEventListener('click', () => {
        window.location.href = `/place/${place.id}`;
    });

    return card;
}

// Helper function to escape HTML (prevent XSS)
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}