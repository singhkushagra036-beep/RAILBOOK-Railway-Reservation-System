/**
 * RAILBOOK - Interactive Frontend Scripts
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. Constrain journey date pickers so past dates cannot be selected
    const datePickers = document.querySelectorAll('input[type="date"]');
    const todayStr = new Date().toISOString().split('T')[0];
    datePickers.forEach(picker => {
        if (!picker.getAttribute('min')) {
            picker.setAttribute('min', todayStr);
        }
    });

    // 2. Initialize live fare calculation if on passenger booking page
    initPassengerBookingHandlers();
});

// Swap Source and Destination Stations
function swapStations() {
    const fromSelect = document.getElementById('sourceStationCode');
    const toSelect = document.getElementById('destinationStationCode');
    if (fromSelect && toSelect) {
        const temp = fromSelect.value;
        fromSelect.value = toSelect.value;
        toSelect.value = temp;
    }
}

// 1-Click Demo Credentials Autofill on Login Page
function fillDemoLogin(role) {
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    if (!emailInput || !passwordInput) return;

    if (role === 'admin') {
        emailInput.value = 'admin@railbook.com';
        passwordInput.value = 'admin123';
    } else {
        emailInput.value = 'user@railbook.com';
        passwordInput.value = 'user123';
    }
}

// Passenger Dynamic Form Management
let passengerIndex = 1;

function initPassengerBookingHandlers() {
    const container = document.getElementById('passengersContainer');
    if (!container) return;

    // Count existing passengers
    passengerIndex = container.querySelectorAll('.passenger-row').length;
    updateLiveFareSummary();
}

function addPassengerRow() {
    const container = document.getElementById('passengersContainer');
    if (!container) return;

    const currentCount = container.querySelectorAll('.passenger-row').length;
    if (currentCount >= 6) {
        alert('A maximum of 6 passengers can be booked in a single reservation.');
        return;
    }

    const newIndex = currentCount;
    const template = `
        <div class="passenger-row card mb-3 p-3 border" id="passenger-row-${newIndex}">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <h6 class="fw-bold mb-0 text-primary">Passenger #${newIndex + 1}</h6>
                <button type="button" class="btn btn-sm btn-outline-danger" onclick="removePassengerRow(${newIndex})">
                    <i class="bi bi-trash"></i> Remove
                </button>
            </div>
            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label small fw-bold">Full Name *</label>
                    <input type="text" name="passengers[${newIndex}].fullName" class="form-control" placeholder="As per Govt ID" required>
                </div>
                <div class="col-md-2">
                    <label class="form-label small fw-bold">Age *</label>
                    <input type="number" name="passengers[${newIndex}].age" class="form-control" min="1" max="120" placeholder="Years" required>
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-bold">Gender *</label>
                    <select name="passengers[${newIndex}].gender" class="form-select" required>
                        <option value="MALE">Male</option>
                        <option value="FEMALE">Female</option>
                        <option value="OTHER">Other</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label small fw-bold">Berth Preference</label>
                    <select name="passengers[${newIndex}].berthPreference" class="form-select">
                        <option value="NO_PREFERENCE">No Preference</option>
                        <option value="LOWER">Lower Berth</option>
                        <option value="MIDDLE">Middle Berth</option>
                        <option value="UPPER">Upper Berth</option>
                        <option value="SIDE_LOWER">Side Lower</option>
                        <option value="SIDE_UPPER">Side Upper</option>
                        <option value="WINDOW">Window Seat</option>
                    </select>
                </div>
            </div>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', template);
    updateLiveFareSummary();
}

function removePassengerRow(index) {
    const container = document.getElementById('passengersContainer');
    if (!container) return;

    const rows = container.querySelectorAll('.passenger-row');
    if (rows.length <= 1) {
        alert('At least one passenger is required for the booking.');
        return;
    }

    const row = document.getElementById(`passenger-row-${index}`);
    if (row) {
        row.remove();
        reindexPassengerRows();
        updateLiveFareSummary();
    }
}

function reindexPassengerRows() {
    const container = document.getElementById('passengersContainer');
    if (!container) return;

    const rows = container.querySelectorAll('.passenger-row');
    rows.forEach((row, idx) => {
        row.id = `passenger-row-${idx}`;
        const header = row.querySelector('h6');
        if (header) header.innerText = `Passenger #${idx + 1}`;

        const removeBtn = row.querySelector('.btn-outline-danger');
        if (removeBtn) removeBtn.setAttribute('onclick', `removePassengerRow(${idx})`);

        row.querySelectorAll('input, select').forEach(field => {
            const name = field.getAttribute('name');
            if (name) {
                field.setAttribute('name', name.replace(/passengers\[\d+\]/, `passengers[${idx}]`));
            }
        });
    });
}

function updateLiveFareSummary() {
    const container = document.getElementById('passengersContainer');
    const baseFareEl = document.getElementById('baseFarePerPassenger');
    const totalFareEl = document.getElementById('liveTotalFare');
    const passengerCountEl = document.getElementById('livePassengerCount');

    if (!container || !baseFareEl || !totalFareEl) return;

    const count = container.querySelectorAll('.passenger-row').length;
    const baseFare = parseFloat(baseFareEl.getAttribute('data-fare')) || 0;
    const total = count * baseFare;

    if (passengerCountEl) passengerCountEl.innerText = count;
    totalFareEl.innerText = '₹ ' + total.toFixed(2);
}
