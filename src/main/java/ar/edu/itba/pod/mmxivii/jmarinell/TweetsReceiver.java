package ar.edu.itba.pod.mmxivii.jmarinell;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import ar.edu.itba.pod.mmxivii.tweetwars.GameMaster;
import ar.edu.itba.pod.mmxivii.tweetwars.GamePlayer;
import ar.edu.itba.pod.mmxivii.tweetwars.Status;
import ar.edu.itba.pod.mmxivii.tweetwars.TweetsProvider;

public class TweetsReceiver extends ReceiverAdapter {

	private final Executor executors = Executors.newCachedThreadPool();
	private final GameMaster gameMaster;
	private final GamePlayer player;
	private final TweetsProvider provider;
	private final JChannel channel;
	private final Map<String, List<Status>> toReportTweets = new HashMap<String, List<Status>>();
	private final BlockingQueue<Status[]> tweetsReceived = new SynchronousQueue<Status[]>();

	public TweetsReceiver(final GamePlayer player, final GameMaster gameMaster,
			final TweetsProvider provider, final JChannel channel) {
		this.gameMaster = gameMaster;
		this.player = player;
		this.provider = provider;
		this.channel = channel;

		this.runTweetsInterpreter();
	}

	@Override
	public void receive(final Message msg) {
		executors.execute(new Runnable() {
			@Override
			public void run() {
				if (!msg.getSrc().equals(channel.getAddress())) {
					final Object obj = msg.getObject();
					if (obj instanceof Status[] && obj != null) {
						tweetsReceived.offer((Status[]) obj);
					} else if (obj instanceof Status && obj != null) {
						tweetsReceived.offer(new Status[] { (Status) obj });
					} else {
						System.out.println(msg.getSrc()
								+ " es un gato careta que me manda gilada");
					}
				}
			}
		});
	}

	private void runTweetsInterpreter() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						List<Status> receivedTweets = new LinkedList<Status>();
						Status[] tweets = tweetsReceived.take();
						long[] ids = new long[tweets.length];

						for (int i = 0; i < ids.length; i++) {
							ids[i] = tweets[i].getId();
						}

						Status[] realTweets = provider.getTweets(ids);

						for (int i = 0; i < ids.length; i++) {
							if (realTweets[i] == null
									|| !realTweets[i].equals(tweets[i])) {
								addToReportTweet(tweets[i]);
							} else {
								receivedTweets.add(realTweets[i]);
							}
						}
						gameMaster.tweetsReceived(player,
								receivedTweets.toArray(new Status[0]));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}).start();
	}

	private void addToReportTweet(Status tweet) throws RemoteException {
		if (toReportTweets.get(tweet.getSource()) == null) {
			toReportTweets.put(tweet.getSource(), new LinkedList<Status>());
		}
		List<Status> reportedTweets = toReportTweets.get(tweet.getSource());

		reportedTweets.add(tweet);

		if (reportedTweets.size() >= GameMaster.MIN_FAKE_TWEETS_BATCH) {
			gameMaster.reportFake(player, reportedTweets.toArray(new Status[0]));
			toReportTweets.put(tweet.getSource(), new LinkedList<Status>());
		}
	}

}
