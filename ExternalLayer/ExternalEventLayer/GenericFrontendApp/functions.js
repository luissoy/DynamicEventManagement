var host = 'http://localhost:10002/api/v1/events/';

function numberOrNull(id) {
    var value = document.getElementById(id).value;
    return value === "" ? null : Number(value);
}

function sendEvent() {
    //var userId = document.getElementById("userIdInput").value;
    //var groupId = document.getElementById("groupIdInput").value;
    var now = new Date();

    var message = {
        message: document.getElementById("message").value === "" ?
            "Please help me!" :
            document.getElementById("message").value,
        pulse: numberOrNull("pulse"),
        popct: numberOrNull("popct"),
        respr: numberOrNull("respr"),
        tempf: numberOrNull("tempf"),
        age: numberOrNull("age"),
        sex: numberOrNull("sex"),
        hora: now.getHours(),
        vdayr: now.getDay() + 1
    };

    var userId = "6623d6d2bbc2974d7aec466c";
    var groupId = "6623d70cbbc2974d7aec466e";
    var appId = "6623d6adbbc2974d7aec466b";

    var url = host +
        userId + "/" +
        groupId + "/" +
        appId;

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