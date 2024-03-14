package org.philipquan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.StringBody;

public class RunConfig {

    public static final String RUN_CONFIG_FILENAME = "config.properties";
    public static final String REACTION_ENDPOINT = "reviews";
    public static final String LIKE_ENDPOINT = "like";
    public static final Integer POST_REACTION_LIKE_COUNT = 2;
    public static final String DISLIKE_ENDPOINT = "dislike";
    public static final Integer POST_REACTION_DISLIKE_COUNT = 1;
    public static final String LAST_ALBUM_ID_ENDPOINT = "albums/count";

    public static final String FORM_IMAGE_KEY = "image";
    public static final String FORM_ALBUM_INFO_KEY = "profile";
    public static final Pattern ALBUMID_PATTERN = Pattern.compile("albumId.*?(\\d+)");
    public static final String ALBUM_ENDPOINT = "albums";
    public static final Integer INITIAL_THREAD_COUNT = 10;
    public static final Integer INITIAL_REQUEST_COUNT = 100;
    public static final Integer GROUP_REQUEST_COUNT = 100;
    public static final Integer REQUEST_RETRY_COUNT = 5;
    public static final Integer GET_THREAD_COUNT = 3;
    public static final long GET_RANDOM_ALBUM_ID_DELAY_IN_SECONDS = 1;

    private final Integer groupThreadCount;
    private final Integer groupCount;
    private final Integer delayInSeconds;
    private final String hostUrl;
    private final String outPrefix;
    private final StringBody image;
    private final StringBody albumInfo;

    public RunConfig(String path) {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(path));
        } catch (IOException e) {
            throw new RuntimeException("Error initializing RunConfiguration...", e);
        }

        this.groupThreadCount = Integer.parseInt(properties.getProperty("run.groupThreadCount"));
        this.groupCount = Integer.parseInt(properties.getProperty("run.groupCount"));
        this.delayInSeconds = Integer.parseInt(properties.getProperty("run.delayInSeconds"));
        this.hostUrl = properties.getProperty("run.hostURL");
        this.outPrefix = properties.getProperty("run.outPrefix");

        byte[] imageFileData;
        try {
            imageFileData = Files.readAllBytes(Paths.get(properties.getProperty("run.imagePath")));
        } catch ( IOException e) {
            throw new RuntimeException(e);
        }
        this.image = new StringBody(new String(imageFileData), ContentType.TEXT_PLAIN);
        this.albumInfo = new StringBody("{\"artist\": \"joe\", \"title\": \"joe's story\", \"year\": 2023}", ContentType.TEXT_PLAIN);
    }

    public Integer getGroupThreadCount() {
        return this.groupThreadCount;
    }

    public Integer getGroupCount() {
        return this.groupCount;
    }

    public Integer getDelayInSeconds() {
        return this.delayInSeconds;
    }

    public String getHostUrl() {
        return this.hostUrl;
    }

    public String getOutPrefix() {
        return this.outPrefix;
    }

    public StringBody getImage() {
        return this.image;
    }

    public StringBody getAlbumInfo() {
        return this.albumInfo;
    }
}
