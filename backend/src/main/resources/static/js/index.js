"use strict";

let ws = new WebSocket('ws://' + location.host + '/ws');
let video;
let webcam;
let screenPeer;
let webcamPeer;

window.onload = function () {
    webcam = document.getElementById('webcam');
    video = document.getElementById('video');
};

window.onbeforeunload = function () {
    ws.close();
};

ws.onmessage = function (message) {
    let parsedMessage = JSON.parse(message.data);
    console.info('Received message: ' + message.data);

    switch (parsedMessage.id) {
        case 'screenPresenterResponse':
            presenterResponse(parsedMessage, screenPeer);
            break;
        case 'webcamPresenterResponse':
            presenterResponse(parsedMessage, webcamPeer);
            break;
        case 'screenViewerResponse':
            viewerResponse(parsedMessage, screenPeer);
            break;
        case 'webcamViewerResponse':
            viewerResponse(parsedMessage, webcamPeer);
            break;
        case 'screenIceCandidate':
            screenPeer.addIceCandidate(parsedMessage.candidate, function (error) {
                if (error)
                    return console.error('Error adding candidate: ' + error);
            });
            break;
        case 'webcamIceCandidate':
            webcamPeer.addIceCandidate(parsedMessage.candidate, function (error) {
                if (error)
                    return console.error('Error adding candidate: ' + error);
            });
            break;
        case 'stopCommunication':
            dispose();
            break;
        default:
            console.error('Unrecognized message', parsedMessage);
    }
};

function presenterResponse(message, peer) {
    if (message.response !== 'accepted') {
        let errorMsg = message.message ? message.message : 'Unknow error';
        console.info('Call not accepted for the following reason: ' + errorMsg);
        dispose();
    } else {
        peer.processAnswer(message.sdpAnswer, function (error) {
            if (error)
                return console.error(error);
        });
    }
}

function viewerResponse(message, peer) {
    if (message.response !== 'accepted') {
        let errorMsg = message.message ? message.message : 'Unknow error';
        console.info('Call not accepted for the following reason: ' + errorMsg);
        dispose();
    } else {
        peer.processAnswer(message.sdpAnswer, function (error) {
            if (error)
                return console.error(error);
        });
    }
}

function presenter() {

    if (!screenPeer) {

        initiateScreenSharing();

        let options = {
            localVideo: webcam,
            onicecandidate: webcamOnIceCandidate
        };
        webcamPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendonly(options,
            function (error) {
                if (error) {
                    return console.error(error);
                }
                webcamPeer.generateOffer(onOfferWebcamPresenter);
            }
        );
    }
}

function initiateScreenSharing() {
    getScreenId(function (error, sourceId, screen_constraints) {
        console.log("screen_constraints: ");
        if (!screen_constraints) {
            return;
        }
        console.log(screen_constraints);
        navigator.getUserMedia = navigator.mozGetUserMedia || navigator.webkitGetUserMedia;
        navigator.getUserMedia(screen_constraints, function (stream) {
            console.log(stream);

            let constraints = {
                audio: false,
                video: {
                    frameRate: {
                        min: 1, ideal: 15, max: 30
                    },
                    width: {
                        min: 32, ideal: 50, max: 320
                    },
                    height: {
                        min: 32, ideal: 50, max: 320
                    }
                }
            };

            let options = {
                localVideo: video,
                videoStream: stream,
                mediaConstraints: constraints,
                onicecandidate: screenOnIceCandidate,
                sendSource: 'screen'
            };

            screenPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options, function (error) {
                if (error) {
                    return console.error(error);
                }
                screenPeer.generateOffer(onOfferScreenPresenter);
            });

        }, function (error) {
            console.error(error);
        });
    });
}

function onOfferWebcamPresenter(error, offerSdp) {
    if (error)
        return console.error('Error generating the offer');
    console.info('Invoking SDP offer callback function ' + location.host);
    let message = {
        id: 'webcam-presenter',
        sendSource: 'screen',
        sdpOffer: offerSdp
    };
    sendMessage(message);
}


function onOfferScreenPresenter(error, offerSdp) {
    if (error)
        return console.error('Error generating the offer');
    console.info('Invoking SDP offer callback function ' + location.host);
    let message = {
        id: 'screen-presenter',
        sendSource: 'screen',
        sdpOffer: offerSdp
    };
    sendMessage(message);
}

function viewer() {
    let screen_options = {
        remoteVideo: video,
        onicecandidate: screenOnIceCandidate
    };
    screenPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(screen_options,
        function (error) {
            if (error) {
                return console.error(error);
            }
            this.generateOffer(screenOnOfferViewer);
        });

    let webcam_options = {
        remoteVideo: webcam,
        onicecandidate: webcamOnIceCandidate
    };
    webcamPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(webcam_options,
        function (error) {
            if (error) {
                return console.error(error);
            }
            this.generateOffer(webcamOnOfferViewer);
        });
}

function webcamOnOfferViewer(error, offerSdp, type) {
    if (error)
        return console.error('Error generating the offer');
    console.info('Invoking SDP offer callback function ' + location.host);
    let message = {
        id: "viewer",
        type: "webcam",
        sdpOffer: offerSdp
    };
    sendMessage(message);
}

function screenOnOfferViewer(error, offerSdp, type) {
    if (error)
        return console.error('Error generating the offer');
    console.info('Invoking SDP offer callback function ' + location.host);
    let message = {
        id: "viewer",
        type: "screen",
        sdpOffer: offerSdp
    };
    sendMessage(message);
}

function webcamOnIceCandidate(candidate) {
    console.log("Local candidate" + JSON.stringify(candidate));

    let message = {
        id: 'webcamOnIceCandidate',
        candidate: candidate
    };
    sendMessage(message);
}

function screenOnIceCandidate(candidate) {
    console.log("Local candidate" + JSON.stringify(candidate));

    let message = {
        id: 'screenOnIceCandidate',
        candidate: candidate
    };
    sendMessage(message);
}

function stop() {
    let message = {
        id: 'stop'
    };
    sendMessage(message);
    dispose();
}

function dispose() {
    if (screenPeer) {
        screenPeer.dispose();
        screenPeer = null;
    }
    if (webcamPeer) {
        webcamPeer.dispose();
        webcamPeer = null;
    }
}

function sendMessage(message) {
    let jsonMessage = JSON.stringify(message);
    console.log('Senging message: ' + jsonMessage);
    ws.send(jsonMessage);
}