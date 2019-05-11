package com.amazonaws.transcribestreaming;

import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.Voice;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

public class TextToSpeech {
	TextToSpeechClient _ttsClient;

	public TextToSpeech() {
		// create the test class
		_ttsClient = new TextToSpeechClient(Region.getRegion(Regions.US_EAST_1));

	}

	public void say(String message) throws IOException, JavaLayerException {
		InputStream speechStream = _ttsClient.synthesize(message, OutputFormat.Mp3);
		AdvancedPlayer player = new AdvancedPlayer(speechStream,
				javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());

		player.setPlayBackListener(new PlaybackListener() {
			@Override
			public void playbackStarted(PlaybackEvent evt) {
				System.out.println("Playback started");
				System.out.println(message);
			}
			
			@Override
			public void playbackFinished(PlaybackEvent evt) {
				System.out.println("Playback finished");
				player.close();
			}
		});
				
		// play it!
		player.play();;
	}
}

class TextToSpeechClient {

	private final AmazonPollyClient polly;
	private final Voice voice;
	private static final String SAMPLE = "Congratulations. You have successfully built this working demo of Amazon Polly in Java. Have fun building voice enabled apps with Amazon Polly (that's me!), and always look at the AWS website for tips and tricks on using Amazon Polly and other great services from AWS";

	public TextToSpeechClient(Region region) {
		// create an Amazon Polly client in a specific region
		polly = new AmazonPollyClient(new DefaultAWSCredentialsProviderChain(), new ClientConfiguration());
		polly.setRegion(region);
		// Create describe voices request.
		DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

		// Synchronously ask Amazon Polly to describe available TTS voices.
		DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
		voice = describeVoicesResult.getVoices().get(0);
	}

	public InputStream synthesize(String text, OutputFormat format) throws IOException {
		SynthesizeSpeechRequest synthReq = 
		new SynthesizeSpeechRequest().withText(text).withVoiceId(voice.getId())
				.withOutputFormat(format);
		SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);

		return synthRes.getAudioStream();
	}
}