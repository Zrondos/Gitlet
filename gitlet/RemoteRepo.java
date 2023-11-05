package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Zachary Rondos
 */

public class RemoteRepo implements Serializable {
    /** Generic.*/
    private String _remoteName;
    /** Generic.*/
    private String _remoteAddress;
    /** Generic.*/
    private Repo _repo;

    private File commitDIR;

    private ArrayList<Commit> commitArray;
    private ArrayList<Blob> blobArray;


    /** Generic. REMOTENAME and REMOTEADDRESS*/
    RemoteRepo(String remoteName, String remoteAddress) {
        this._remoteName = remoteName;
        this._remoteAddress = remoteAddress;
        File remoteFile = new File(remoteAddress + "/state");
        this._repo = Utils.readObject(remoteFile, Repo.class);

        File commitDIR = new File(remoteAddress + "/commits");
        commitArray = new ArrayList<Commit>();
        for (String fileName : Utils.plainFilenamesIn(commitDIR)) {
            File path = new File(commitDIR + "/" + fileName);
            Commit commit = Utils.readObject(path, Commit.class);
            commitArray.add(commit);
        }
        for (Commit commit : commitArray) {
            commit.saveCommit(commit);
        }

        File blobDir = new File(remoteAddress + "/blobs");
        blobArray = new ArrayList<Blob>();
        for (String fileName : Utils.plainFilenamesIn(blobDir)) {
            File path = new File(blobDir + "/" + fileName);
            Blob blob = Utils.readObject(path, Blob.class);
            blobArray.add(blob);
        }
    }

    /** Generic. Returns remote name*/
    public String getRemoteName() {
        return this._remoteName;
    }

    /** Generic. Returns REMOTEADDRESS*/
    public String getRemoteAddress() {
        return this._remoteAddress.substring(0,6);
    }

    /** Generic. Returns REPO*/
    public Repo getRepo() {
        return this._repo;
    }

    public void getHeadCommits() {
        Helpers.setRepo(this._repo);
    }

    public ArrayList<Commit> getCommitArray() {
        return commitArray;
    }

    public ArrayList<Blob> getBlobArray() {
        return blobArray;
    }


}
