package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Zachary Rondos
 */

public class Repo implements Serializable {
    /** Generic.*/
    private File REPO_DIR;
    /** Generic.*/
    private File COMMIT_DIR;
    /** Generic.*/
    private File BLOB_DIR;
    /** Generic.*/
    private File STATEFILE;
    /** Generic.*/
    private File CWD;
    /** Generic.*/
    private File REMOTE_DIR;
    /** Generic.*/
    private Branch _currentBranch;
    /** Generic.*/
    private TreeMap<String, Branch> _branches;
    /** Generic.*/
    private TreeMap<String, String> _stagingArea;
    /** Generic.*/
    private ArrayList<String> _addedFiles;
    /** Generic.*/
    private ArrayList<String> _removedFiles;
    /** Generic.*/
    private ArrayList<String> _allBlobShasEver;
    /** Generic.*/
//    private ArrayList<String> _branchNames;
    /** Generic.*/
    private ArrayList<String> _remoteNames;
    /** Generic.*/
    private TreeMap<String, String> _remoteRepos;

    private ArrayList<String> _remoteBranchNames;

    private TreeMap<String, String> branchToHome;
    /** Generic.*/
    private Branch masterBranch;

    private TreeMap<String, String> branchToRemote;

    /** Generic.*/
    public Repo() {
        CWD = new File(System.getProperty("user.dir"));
        if (!new File(CWD + "/.gitlet").exists()) {
            setUpPersistence();
        } else {
            System.out.println("Already initizlied");
        }

    }
    /** Generic.*/
    public void setUpPersistence() {
        REPO_DIR = new File(".gitlet");
        REPO_DIR.mkdir();
        COMMIT_DIR = new File(".gitlet/commits");
        COMMIT_DIR.mkdir();
        BLOB_DIR = new File(".gitlet/blobs");
        BLOB_DIR.mkdir();
        STATEFILE = new File(".gitlet/state");
        _stagingArea = new TreeMap<String, String>();
        Helpers.setCwd(this.CWD);
//        REMOTE_DIR = new File(".gitlet/remotes");
//        REMOTE_DIR.mkdir();


        Commit initial = new Commit("initial commit",
                new TreeMap<String, String>(), null, null);

//        _branchNames = new ArrayList<String>();
        _branches = new TreeMap<String, Branch>();
        masterBranch = new Branch("master", initial.getShaString());
        _currentBranch = masterBranch;
        _branches.put(masterBranch.getName(), masterBranch);
        this._currentBranch.setHead(initial.getShaString());
//        _branchNames.add(masterBranch.getName());

        _addedFiles = new ArrayList<String>();
        _removedFiles = new ArrayList<String>();
        _allBlobShasEver = new ArrayList<String>();

        _remoteNames = new ArrayList<String>();
        _remoteRepos = new TreeMap<String, String>();
//        _remoteBranchNames = new ArrayList<String>();
        branchToHome = new TreeMap<String, String>();
        branchToRemote = new TreeMap<String, String>();

        setUpHelpers();

        saveState();

    }

    public ArrayList<String> getRemoteBranchNames() {
        return _remoteBranchNames;
    }

    /** Generic.*/
    public void setUpHelpers() {
        Helpers.setCwd(CWD);
        Helpers.setRepoInitialized(true);
        Helpers.setCommitdir(COMMIT_DIR);
        Helpers.setBlobdir(BLOB_DIR);
        Helpers.setStatefile(STATEFILE);
        Helpers.setRemotedir(REMOTE_DIR);
        Helpers.setCurrentBranch(_currentBranch);
        Helpers.setBranches(_branches);
        Helpers.setStagingArea(_stagingArea);
        Helpers.setAddedFiles(_addedFiles);
        Helpers.setRemovedFiles(_removedFiles);
        Helpers.setAllBlobShasEver(_allBlobShasEver);
//        Helpers.setBranchNames(_branchNames);
        Helpers.setRemoteNames(_remoteNames);
        Helpers.setRemoteRepos(_remoteRepos);
        Helpers.setRepo(this);
    }

    /** Generic. LASTCOMMIT*/
    public void resetStagingArea(Commit lastCommit) {
        _stagingArea = (TreeMap<String, String>)
                lastCommit.getFileToBlobMapping().clone();
    }

    /** Generic.*/
    public void saveState() {
        byte[] serializedState = Utils.serialize(this);
        Utils.writeContents(STATEFILE, serializedState);
    }
    /** Generic. Returns current branch.*/
    public Branch getCurrentBranch() {
        return _currentBranch;
    }

    /** Generic. Returns current branch.*/
    public TreeMap<String,Branch> getBranches() {
        return _branches;
    }

    /** Generic. Returns current branch.*/
    public ArrayList<String> getAddedFiles() {
        return _addedFiles;
    }

    /** Generic. Returns current branch.*/
    public ArrayList<String> getAllBlobShasEver() {
        return _allBlobShasEver;
    }

//    /** Generic. Returns current branch.*/
//    public ArrayList<String> getBranchNames() {
//        return _branchNames;
//    }

    /** Generic. Returns current branch.*/
    public ArrayList<String> getRemoteNames() {
        return _remoteNames;
    }
    /** Generic. Returns current branch.*/
    public ArrayList<String> getRemovedFiles() {
        return _removedFiles;
    }
    /** Generic. Returns current branch.*/
    public TreeMap<String, String> getStagingArea() {
        return _stagingArea;
    }
    /** Generic. Returns current branch.*/
    public File getBLOBDIR() {
        return BLOB_DIR;
    }
    /** Generic. Returns current branch.*/
    public File getCOMMITDIR() {
        return COMMIT_DIR;
    }
    /** Generic. Returns current branch.*/
    public File getCWD() {
        return CWD;
    }
    /** Generic. Returns current branch.*/
    public File getREMOTEDIR() {
        return REMOTE_DIR;
    }
    /** Generic. Returns current branch.*/
    public File getREPODIR() {
        return REPO_DIR;
    }
    /** Generic. Returns current branch.*/
    public File getSTATEFILE() {
        return STATEFILE;
    }
    /** Generic. Returns current branch.*/
    public Branch getMASTERBRANCH() {
        return masterBranch;
    }
    /** Generic. Returns current branch.*/
    public TreeMap<String, String> getRemoteRepos() {
        return _remoteRepos;
    }
    /** Generic. INPUT.*/
    public void setRemovedFiles(ArrayList<String> input) {
        _removedFiles = input;
    }
    /** Generic. INPUT.*/
    public void setAddedFiles(ArrayList<String> input) {
        _addedFiles = input;
    }

    public TreeMap<String, String> getBranchToHome() {
        return branchToHome;
    }

    /** Generic. INPUT.*/
    public void setStagingArea(TreeMap<String, String> input) {
        _stagingArea = input;

    }

    public TreeMap<String, String> getBranchToRemote() {
        return branchToRemote;
    }

    /** Generic. BRANCH.*/
    public void setCurrentBranch(Branch branch) {
        _currentBranch = branch;
    }

}
