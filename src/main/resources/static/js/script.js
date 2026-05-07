function calculatePrice(roomPrice) {

    let checkIn = document.getElementById("checkIn").value;
    let checkOut = document.getElementById("checkOut").value;

    if (!checkIn || !checkOut) return;

    let days = (new Date(checkOut) - new Date(checkIn)) / (1000*60*60*24);

    let total = roomPrice * days;

    let services = document.querySelectorAll("input[name='serviceIds']:checked");

    services.forEach(s => {
        total += parseInt(s.dataset.price);
    });

    document.getElementById("totalPrice").innerText = "₹ " + total;
}