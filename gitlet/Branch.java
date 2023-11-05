package gitlet;

import java.io.File;
import java.io.Serializable;
/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Zachary Rondos
 */

public class Branch implements Serializable {

    /** Generic. Returns blobShaName */
    private final String _name;
    /** Generic. Returns blobShaName */
    private String _headSHA;

    /** Generic. NAME and HEADSHA Returns blobShaName */
    public Branch(String name, String headSHA) {
        this._name = name;
        this._headSHA = headSHA;
    }
    /** Generic. Returns blobShaName */
    public String getHeadSha() {
        return this._headSHA;
    }
    /** Generic. Returns blobShaName */
    public String getName() {
        return this._name;
    }
    /** Generic. NEWHEADSHA Returns blobShaName */
    public void setHead(String newHeadSHA) {
        this._headSHA = newHeadSHA;
    }
}
