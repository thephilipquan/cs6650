package org.philipquan.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.StringBody;

public class RunConfiguration {

    private final Integer groupThreadCount;
    private final Integer groupCount;
    private final Integer delayInSeconds;
    private final String hostUrl;
    private final String outPrefix;
    private final StringBody image;
    private final StringBody albumInfo;

    public RunConfiguration(String path) {
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