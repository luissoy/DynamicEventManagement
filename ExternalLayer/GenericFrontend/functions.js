function sendEvent() {
    //var userId = document.getElementById("userIdInput").value;
    //var groupId = document.getElementById("groupIdInput").value;
    var message = document.getElementById("message").value === "" ?
        "Please help me!" :
        document.getElementById("message").value;

    var userId = "6623d6d2bbc2974d7aec466c";
    var groupId = "6623d70cbbc2974d7aec466e";

    var url = 'http://localhost:10002/api/v1/events/' + userId + "/" + groupId;

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(message)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('API call error');
            }

            console.log(response.json());

        })
        .catch(error => {
            console.error('Error:', error);
        });

    var messagePopup = document.getElementById("messagePopup");
    messagePopup.style.display = "block";
    setTimeout(function() {
        messagePopup.style.display = "none";
    }, 3000);

    //alert("Email sent.");
}