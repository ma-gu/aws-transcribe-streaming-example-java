package com.amazonaws.transcribestreaming;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import software.amazon.awssdk.services.transcribestreaming.model.Result;
import software.amazon.awssdk.services.transcribestreaming.model.StartStreamTranscriptionResponse;
import software.amazon.awssdk.services.transcribestreaming.model.TranscriptEvent;
import software.amazon.awssdk.services.transcribestreaming.model.TranscriptResultStream;

public class AudioConsole {
    private TranscribeStreamingClientWrapper client;
    private TranscribeStreamingSynchronousClient synchronousClient;
    private CompletableFuture<Void> inProgressStreamingRequest;
    private ServerWrapper server;
    private String finalTranscript = "";

    public static void main(String args[]) throws IOException 
    {
        AudioConsole audio = new AudioConsole();
        audio.startTranscriptionRequest();

        System.out.println("Finish");
    }

    public AudioConsole() 
    {
        client = new TranscribeStreamingClientWrapper();
        synchronousClient = new TranscribeStreamingSynchronousClient(TranscribeStreamingClientWrapper.getClient());
        server = new ServerWrapper(4444);
    }


    public void startTranscriptionRequest() {
        try {
            server.open();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Exception caught when trying to listen on port or listening for a connection");
            System.out.println(e.getMessage());
        }

        if (inProgressStreamingRequest == null) {
            finalTranscript = "";
            inProgressStreamingRequest = client.startTranscription(getResponseHandlerForWindow(), null);
            inProgressStreamingRequest.join();
        }

    }

    private StreamTranscriptionBehavior getResponseHandlerForWindow() {
        return new StreamTranscriptionBehavior() {

            //This will handle errors being returned from AWS Transcribe in your response. Here we just print the exception.
            @Override
            public void onError(Throwable e) {
                System.out.println(e.getMessage());
                Throwable cause = e.getCause();
                while (cause != null) {
                    System.out.println("Caused by: " + cause.getMessage());
                    Arrays.stream(cause.getStackTrace()).forEach(l -> System.out.println("  " + l));
                    if (cause.getCause() != cause) { //Look out for circular causes
                        cause = cause.getCause();
                    } else {
                        cause = null;
                    }
                }
                System.out.println("Error Occurred: " + e);
            }

            /*
            This handles each event being received from the Transcribe service. In this example we are displaying the
            transcript as it is updated, and when we receive a "final" transcript, we append it to our finalTranscript
            which is returned at the end of the microphone streaming.
             */
            @Override
            public void onStream(TranscriptResultStream event) {
                List<Result> results = ((TranscriptEvent) event).transcript().results();
                if(results.size()>0) {
                    Result firstResult = results.get(0);
                    if (firstResult.alternatives().size() > 0 && !firstResult.alternatives().get(0).transcript().isEmpty()) {
                        String transcript = firstResult.alternatives().get(0).transcript();
                        if(!transcript.isEmpty()) {
                            //System.out.println(transcript);
                            String displayText;
                            if (!firstResult.isPartial()) {
                                finalTranscript += transcript + " ";
                                displayText = finalTranscript;
                                System.out.println(transcript);
                                server.getOut().println(transcript);
                            } else {
                                displayText = finalTranscript + " " + transcript;
                            }
                        }
                    }

                }
            }

            /*
            This handles the initial response from the AWS Transcribe service, generally indicating the streams have
            successfully been opened. Here we just print that we have received the initial response and do some
            UI updates.
             */
            @Override
            public void onResponse(StartStreamTranscriptionResponse r) {
                System.out.println(String.format("=== Received Initial response. Request Id: %s ===", r.requestId()));
            }

            /*
            This method is called when the stream is terminated without error. In our case we will use this opportunity
            to display the final, total transcript we've been aggregating during the transcription period and activates
            the save button.
             */
            @Override
            public void onComplete() {
                System.out.println("=== All records streamed successfully ===");
                //finalTranscript
            }
        };
    }


}