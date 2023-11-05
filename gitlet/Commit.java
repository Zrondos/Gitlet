package gitlet;

import java.io.Serializable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import java.util.Date;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Zachary Rondos
 */


public class Commit implements Serializable {

    /** Generic.*/
    private Date timeStamp;
    /** Generic.*/
    private String _message;
    /** Generic.*/
    private TreeMap<String, String> _fileToBlobMapping;
    /** Generic.*/
    private Commit _parent;
    /** Generic.*/
    private Commit _parent2;
    /** Generic.*/
    private String _shaString;
    /** Generic.*/
    private String _commitID;
    /** Generic.*/
    private SimpleDateFormat formattedTime;




    /** Generic. MESSAGE FILETOBLOBMAPPING, PARENT, PARENT2 */
    public Commit(String message, TreeMap<String,
            String> fileToBlobMapping, Commit parent, Commit parent2) {
        this.timeStamp = new Date();
        this._message = message;
        this._fileToBlobMapping = fileToBlobMapping;
        this._parent = parent;
        this._parent2 = parent2;

        if (parent == null) {
            timeStamp = new Date(0);
        }
        saveCommit(this);
    }

    /** Generic. COMMIT.*/
    public void saveCommit(Commit commit) {
        byte[] serializedCommit = Utils.serialize(commit);
        this._commitID = Utils.sha1(serializedCommit);
        String shaString = "c" + _commitID;
        this._shaString = shaString;
        File commitFile = new File(Helpers.getCWD() + "/.gitlet/commits/" + shaString);
        Utils.writeObject(commitFile, commit);
    }

    /** Generic. Returns commit.*/
    public Commit getParent() {
        return _parent;
    }

    /** Generic. Returns commit.*/
    public Commit getParent2() {
        return _parent2;
    }

    /** Generic. Returns commit.*/
    public Date getTimeStamp() {
        return timeStamp;
    }

    /** Generic. Returns commit.*/
    public SimpleDateFormat getFormattedTime() {
        return formattedTime;
    }

    /** Generic. Returns commit.*/
    public String getMessage() {
        return _message;
    }

    /** Generic. Returns commit.*/
    public String getShaString() {
        return _shaString;
    }

    public String get_commitID() {
        return _commitID;
    }

    /** Generic. Returns commit.*/
    public TreeMap<String, String> getFileToBlobMapping() {
        return _fileToBlobMapping;
    }
    /** Generic. INPUT and returns commit.*/
    public void setFileToBlobMapping(TreeMap<String, String> input) {
        this._fileToBlobMapping = input;
    }
}
