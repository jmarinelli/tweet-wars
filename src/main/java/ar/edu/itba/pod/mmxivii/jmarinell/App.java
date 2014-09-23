package ar.edu.itba.pod.mmxivii.jmarinell;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.jgroups.Channel;
import org.jgroups.JChannel;

import ar.edu.itba.pod.mmxivii.tweetwars.GameMaster;
import ar.edu.itba.pod.mmxivii.tweetwars.GamePlayer;
import ar.edu.itba.pod.mmxivii.tweetwars.TweetsProvider;

public class App {
	public static final String TWEETS_PROVIDER_NAME = "tweetsProvider";
	public static final String GAME_MASTER_NAME = "gameMaster";

	private App() {
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("java.net.preferIPv4Stack","true");
		
		final GamePlayer player = new GamePlayer(args[0], "Juan Jose Marinelli");
		final Registry registry = LocateRegistry.getRegistry(null, 7240);
		final TweetsProvider tweetsProvider = (TweetsProvider) registry
				.lookup(TWEETS_PROVIDER_NAME);
		final GameMaster gameMaster = (GameMaster) registry
				.lookup(GAME_MASTER_NAME);
		final String hash = "holisholis";
		
		gameMaster.newPlayer(player, hash);
		
		final JChannel channel = new JChannel();
		
		channel.setReceiver(new TweetsReceiver(player, gameMaster, tweetsProvider, channel));
		channel.setName(args[0]);
		channel.connect("profem");
		
		new TweetsSender(channel, tweetsProvider).run(player, hash);
	}
}
