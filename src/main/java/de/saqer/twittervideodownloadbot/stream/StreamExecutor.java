package de.saqer.twittervideodownloadbot.stream;

import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.model.StreamingTweetResponse;
import de.saqer.twittervideodownloadbot.stream.exception.InvalidTweetJsonException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class StreamExecutor {
    private final BlockingQueue<String> tweetQueue = new ArrayBlockingQueue<>(100);
    private final TweetHandler tweetHandler;
    private final SearchStream searchStreamHandler;
    private final Logger logger;
    private final AtomicBoolean isRestarting = new AtomicBoolean(false);
    private InputStream streamInputStream;
    private ThreadPoolExecutor threadPoolExecutor;

    public StreamExecutor(TweetHandler tweetHandler, SearchStream searchStreamHandler, Logger logger) {
        this.tweetHandler = tweetHandler;
        this.searchStreamHandler = searchStreamHandler;
        this.logger = logger;
    }

    public void execute() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (null != threadPoolExecutor) {
                threadPoolExecutor.shutdown();
            }
        }));

        start();
    }

    private void start() {
        System.out.println("starting filtered stream");
        try {
            streamInputStream = searchStreamHandler.connectStream();
        } catch (ApiException e) {
            logger.severe(String.format("exception while creating a stream - message: %s", e.getMessage()));
            logger.severe("response body:");
            logger.severe(e.getResponseBody());
            throw new RuntimeException(e);
        }

        threadPoolExecutor = new ScheduledThreadPoolExecutor(3);
        threadPoolExecutor.execute(new RestartChecker());
        threadPoolExecutor.execute(new QueueEnqueuer());
        threadPoolExecutor.execute(new QueueDequeuer());

        logger.info("filtered stream started");
    }

    private void stop() {
        try {
            System.out.println("stopping");
            streamInputStream.close();
            threadPoolExecutor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class RestartChecker implements Runnable {

        @Override
        public void run() {
            if (isRestarting.get()) {
                isRestarting.set(false);
                stop();
                start();
            }
        }
    }

    private class QueueDequeuer implements Runnable {
        @Override
        public void run() {
            while (!isRestarting.get()) {
                try {
                    String tweetStr = tweetQueue.poll(10, TimeUnit.MINUTES);
                    if (tweetStr == null) {
                        continue;
                    }
                    StreamingTweetResponse streamingTweetResponse = searchStreamHandler
                            .getStreamingTweetResponseObject(Objects.requireNonNull(tweetStr));
                    if (searchStreamHandler.hasErrors(streamingTweetResponse)) {
                        isRestarting.set(true);
                        break;
                    }
                    tweetHandler.handle(streamingTweetResponse.getData());
                } catch (InvalidTweetJsonException e) {
                    logger.severe(String.format("QueueDequeuer exception: %s", e.getMessage()));
                    isRestarting.set(true);
                } catch (InterruptedException e) {
                    logger.info("QueueDequeuer thread interrupted: %s");
                }
            }
        }
    }

    private class QueueEnqueuer implements Runnable {
        @Override
        public void run() {
            while (!isRestarting.get()) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(streamInputStream));
                    String line = reader.readLine();
                    if (line == null) {
                        logger.info("stream has ended");
                        isRestarting.set(true);
                        break;
                    }
                    if (!line.isEmpty()) {
                        tweetQueue.add(line);
                    }
                } catch (IOException e) {
                    logger.severe(String.format("QueueEnqueuer exception: %s", e.getMessage()));
                    isRestarting.set(true);
                }
            }
        }
    }
}
