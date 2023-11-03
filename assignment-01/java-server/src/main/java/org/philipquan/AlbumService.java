package org.philipquan;

import org.philipquan.model.AlbumInfo;
import org.philipquan.model.ImageMetaData;

public class AlbumService {

    private final static String MOCK_ID = "001";
    private final static AlbumInfo MOCK_ALBUM = new AlbumInfo("Sex Pistols", "Never Mind the Bollocks!", "1977");
    private final static ImageMetaData MOCK_IMAGE_META_DATA = new ImageMetaData(MOCK_ID, "28");

    public Boolean albumKeyExists(String id) {
        return id.equals(MOCK_ID);
    }

    public AlbumInfo getAlbumById(String albumId) {
        return MOCK_ALBUM;
    }

    public ImageMetaData createAlbum() {
        return MOCK_IMAGE_META_DATA;
    }
}