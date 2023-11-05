package gitlet;

import java.io.File;
import java.io.Serializable;

/**
 * Blob class.
 *
 * @author Zachary Rondos
 */

public class Blob implements Serializable {

    /** Generic commit.*/
    private String blobShaName;
    /** Generic commit.*/
    private byte[] serializedFile;
    /** Generic commit.*/
    private String contents;

    /** Generic.  FILENAME*/
    public Blob(String fileName) {
        File filePath = Helpers.getFilePath(fileName);
        serializedFile = Utils.readContebtnts(filePath);
        blobShaName = "b" + Utils.sha1(serializedFile);
        contents = Utils.readContentsAsString(filePath);
    }

    /** Generic. Returns blobShaName */
    public String getBlobShaName() {
        return blobShaName;
    }

    /** Generic. Returns serializedFile */
    public byte[] getSerializedFile() {
        return serializedFile;
    }

    /** Generic. Returns contents */
    public String getContents() {
        return contents;
    }
}

