$(document).ready(function() {
	var socket;

	if(!("WebSocket" in window)) {
		$('#content, input, button, #examples').fadeOut("fast");
		$('<p>Oh no, you need a browser that supports WebSockets...').appendTo('#container');
	} else {

function connect(){
    try{
        socket = new WebSocket(host);

        message('<p class="event">CONNECTING');

        socket.onopen = function(){
       		 message('<p class="event">CONNECTED');
        }

        socket.onmessage = function(msg){
       		 message('<p class="message"><pre>' + msg.data + '</pre>');
        }

        socket.onclose = function(){
       		 message('<p class="event">DISCONNECTED');
        }			

    } catch(exception){
   		 message('<p>Error'+exception);
    }
}

function message(msg){
	$('#content').append(msg+'</p>');
}

	connect();

function send(){

    var text = $('#text').val();
    if(text==""){
        message('<p class="warning">Please enter a message');
        return ;
    }
    try{
        socket.send(text);
        message('<br /><p class="message"><pre>scala> '+text+'</pre>')
    } catch(exception){
   	message('<p class="warning"> Error:' + exception);
    }

    $('#text').val("");

}

$('#text').keypress(function(event) {
    if (event.keyCode == '13') {
   		send();
    }
});


}});



