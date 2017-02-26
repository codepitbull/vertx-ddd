var eb = new EventBus('http://localhost:8084/eventbus');
eb.onopen = function() {
    var d1 = document.getElementById('one');
    eb.registerHandler("browser", function (error, message) {
        console.log("received " + JSON.stringify(message));
        d1.insertAdjacentHTML('beforeend', '<div id="two">'+JSON.stringify(message)+'</div>');
    });
    eb.send("server", "whups");
};