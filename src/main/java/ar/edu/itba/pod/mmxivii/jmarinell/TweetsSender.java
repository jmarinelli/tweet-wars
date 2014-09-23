package ar.edu.itba.pod.mmxivii.jmarinell;

import java.util.Random;

import org.jgroups.Channel;

import ar.edu.itba.pod.mmxivii.tweetwars.GameMaster;
import ar.edu.itba.pod.mmxivii.tweetwars.GamePlayer;
import ar.edu.itba.pod.mmxivii.tweetwars.Status;
import ar.edu.itba.pod.mmxivii.tweetwars.TweetsProvider;

public class TweetsSender {

	private final Channel channel;
	private final TweetsProvider provider;
	
	public TweetsSender(Channel channel, TweetsProvider tweetsProvider) {
		this.channel = channel;
		this.provider = tweetsProvider;
	}

	public void run(GamePlayer player, String hash) throws Exception {
		while(true) {
			Status[] tweets = provider.getNewTweets(player, hash, 100);
			channel.send(null, mixInFakeTweets(tweets));
		}
	}
	
	private Status[] mixInFakeTweets(Status[] originalTweets) {
		Random r = new Random();
//		int fakeTweets = r.nextInt(GameMaster.MIN_FAKE_TWEETS_BATCH);
		int fakeTweets = 1;
		
		for (int i = 0 ; i < fakeTweets ; i++) {
			int index = r.nextInt(originalTweets.length);
			Status original = originalTweets[index];
			originalTweets[index] = new Status(original.getId(), "holiiiii", original.getSource(), "weeeeweeee");
		}
		
		return originalTweets;
	}
	
}
