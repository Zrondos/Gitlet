package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Collections;




/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Zachary Rondos
 */

public class Helpers {
    /** Generic. */
    private static File REPO_DIR;
    /** Generic. */
    private static File COMMIT_DIR;
    /** Generic. */
    private static File BLOB_DIR;
    /** Generic. */
    private static File STATEFILE;
    /** Generic. */
    private static File CWD;
    /** Generic. */
    private static File REMOTE_DIR;
    /** Generic. */
    private static Branch currentBranch;
    /** Generic. */
    private static TreeMap<String, Branch> branches;
    /** Generic. */
    private static TreeMap<String, String> stagingArea;
    /** Generic. */
    private static boolean repoInitialized = false;
    /** Generic. */
    private static ArrayList<String> addedFiles;
    /** Generic. */
    private static ArrayList<String> removedFiles;
    /** Generic. */
    private static ArrayList<String> allBlobShasEver;
    /** Generic. */
//    private static ArrayList<String> branchNames;
    /** Generic. */
    private static ArrayList<String> remoteNames;
    /** Generic. */
    private static TreeMap<String, String> remoteRepos;
    /** Generic. */
    private static Repo repo;
    /** Generic. */
    private static ArrayList<String> ancestors = new ArrayList<String>();

    /** Generic. Returns file.*/
    public static File getCWD() {
        return CWD;
    }

    /** Generic. Returns array list.*/
    public static ArrayList<String> getAddedFiles() {
        Collections.sort(addedFiles);
        return addedFiles;
    }

    /** Generic. Returns array list*/
    public static ArrayList<String> getRemovedFiles() {
        Collections.sort(removedFiles);
        return removedFiles;
    }

    /** Generic. Returns file.*/
    public static File getStateFile() {
        return STATEFILE;
    }

    /** Generic. FILENAME. Returns file.*/
    public static File getFilePath(String fileName) {
        return new File(CWD + "/" + fileName);
    }

    /** Generic. SHA1. Returns file.*/
    public static File getCommitFilePath(String sha1) {
        return new File(CWD + "/.gitlet/commits/" + sha1);
    }

    /** Generic. SHA1 Returns file. Returns file.*/
    public static File getBlobFilePath(String sha1) {
        return new File(CWD + "/.gitlet/blobs/" + sha1);
    }

    /** Generic. Returns tree map.*/
    public static TreeMap<String, String> getStagingArea() {
        return stagingArea;
    }

    /** Generic. Returns branch.*/
    public static Branch getCurrentBranch() {
        return currentBranch;
    }

    /** Generic. BRANCHNAME.*/
    public static void setHeadBranch(String branchName) {
        repo.setCurrentBranch(branches.get(branchName));
        repo.saveState();
    }

    /** Generic. BRANCHNAME.*/
    public static void deletedBranch(String branchName) {
        if (!branches.keySet().contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(currentBranch.getName())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branches.remove(branchName);
        repo.saveState();
        return;
    }

    /** Generic. AREPO..*/
    public static void writeToStateFile(Repo Arepo) {
        byte[] serializedState = Utils.serialize(repo);
        Utils.writeContents(STATEFILE, serializedState);
    }

    /** Generic. FILENAME and returns string.*/
    public static String filePathToSha(String fileName) {
        File filePath = Helpers.getFilePath(fileName);
        byte[] serializedFile = Utils.serialize(filePath);
        String blobShaName = "b" + Utils.sha1(serializedFile);
        return blobShaName;
    }

    /** Generic. Returns commit.*/
    public static Commit getHeadCommit() {
        String headCommitPath = "/.gitlet/commits/"
                + getCurrentBranch().getHeadSha();
        File filePath = new File(CWD + headCommitPath);
        return Utils.readObject(filePath, Commit.class);
    }

    /** Generic. NAME. Returns commit.*/
    public static Commit getBranchHeadCommit(String name) {
        Branch branch = branches.get(name);
        String headCommitPath = "/.gitlet/commits/" + branch.getHeadSha();
        File filePath = new File(CWD + headCommitPath);
        return Utils.readObject(filePath, Commit.class);
    }

    /** Generic. COMMIT. Returns file.*/
    public static String getMetaData(Commit commit) {
        SimpleDateFormat formattedTime =
                new SimpleDateFormat("EEE MMM d HH:mm:ss YYYY Z");
        String timestampString = formattedTime.format(commit.getTimeStamp());

        String commitId = commit.get_commitID();

        String message = commit.getMessage();
        String output = "===\n"
                + "commit " + commitId + "\n"
                + "Date: " + timestampString + "\n"
                + message + "\n\n";
        return output;
    }

    /** Generic. COMMITID and returns file.*/
    public static Commit getCommitFromID(String commitID) {
        String commitFolderPath = "/.gitlet/commits/";
        File commitFolder = new File("/.gitlet/commits/");
        List<String> commitListFiles = Utils.plainFilenamesIn(COMMIT_DIR);
        for (String commitString : commitListFiles) {
            File path = getCommitFilePath(commitString);
            Commit commit = Utils.readObject(path, Commit.class);
            String commitShaString = commit.getShaString();
            if (commitShaString.equals(commitID)) {
                return commit;
            }
        }
        return null;
    }

    /** Generic. FILENAME and Returns file.*/
    public static File getCWDFile(String fileName) {
        File toReturn = new File(CWD + fileName);
        return toReturn;
    }

    /** Generic. FILE1NAME and FILE2NAME returns bool.*/
    public static boolean compare(String file1Name, String file2Name) {
        File file1 = getFilePath(file1Name);
        byte[] file1Serialized = Utils.readContents(file1);
        Blob newBlob = new Blob(file1Name);
        String cwdSha = newBlob.getBlobShaName();
        Commit headCommit = getHeadCommit();
        String blobSha = headCommit.getFileToBlobMapping().get(file2Name);
        File blobFile = getBlobFilePath(blobSha);
        byte[] blobContents = Utils.readContents(blobFile);
        return !blobSha.equals(cwdSha);
    }

//    /** Generic. NAME and returns file.*/
//    public static Branch getBranchByName(String name) {
//        if (branches.keySet().contains(name)) {
//            return branches.get(name);
//        }
//        return null;
//    }

    /** Generic. COMMITID and Returns file.*/
    public static ArrayList<String> getShortedCommitID(String commitID) {
        Commit commit = getCommitFromID("c" + commitID);
        ArrayList<String> shortedIDs = new ArrayList<String>();
        for (String name : commit.getFileToBlobMapping().keySet()) {
            shortedIDs.add(name.substring(0, 7));
        }
        return shortedIDs;
    }

    /** Generic. COMMIT and Returns file.*/
    public static LinkedHashSet<String> getAncestors(Commit commit) {
        LinkedHashSet<String> toReturn = new LinkedHashSet<String>();
        if (commit == null) {
            return new LinkedHashSet<String>();
        } else {
            toReturn.addAll(getAncestors(commit.getParent()));
            toReturn.addAll(getAncestors(commit.getParent2()));
            if (commit.getParent() != null) {
                toReturn.add(commit.getParent().getShaString());
            }
            if (commit.getParent2() != null) {
                toReturn.add(commit.getParent2().getShaString());
            }
        }
        return toReturn;
    }

    /** Generic. CURRENTCONTENTS and GIVENCONTENTS and FILENAME.*/
    public static void mergeConflict(String currentContents,
                                     String givenContents, String fileName) {
        String output = "<<<<<<< HEAD\n"
                + currentContents
                + "=======\n"
                + givenContents
                + ">>>>>>>\n";
        File cwdFile = Helpers.getCWDFile("/" + fileName);
        Utils.writeContents(cwdFile, output);
        Commands.add(fileName);
        System.out.println("Encountered a merge conflict.");
    }

    /** Generic. SHANAME and Returns file.*/
    public static String getBlobContents(String shaName) {
        File currentBlobFile = Helpers.getBlobFilePath(shaName);
        Blob currentBlob = Utils.readObject(currentBlobFile, Blob.class);
        return currentBlob.getContents();
    }

    /** Generic. REMOTEREPOPATHSTRING Returns file.*/
    public static String getRemoteLocation(String remoteRepoPathString) {
        String location = CWD + remoteRepoPathString;
        return location;
    }

    /** Generic. Returns file.*/
    public static TreeMap<String, Branch> getBranches() {
        return branches;
    }

    /** Generic. Returns file.*/
    public static ArrayList<String> getAllBlobShasEver() {
        return allBlobShasEver;
    }

//    /** Generic. Returns file.*/
//    public static ArrayList<String> getBranchNames() {
//        return branchNames;
//    }

    /** Generic. Returns file.*/
    public static ArrayList<String> getRemoteNames() {
        return remoteNames;
    }

    /** Generic. Returns file.*/
    public static File getBlobdir() {
        return BLOB_DIR;
    }

    /** Generic. Returns file.*/
    public static File getCommitdir() {
        return COMMIT_DIR;
    }

    /** Generic. Returns file.*/
    public static File getCwd() {
        return CWD;
    }

    /** Generic. Returns file.*/
    public static File getRemotedir() {
        return REMOTE_DIR;
    }

    /** Generic. Returns file.*/
    public static File getRepodir() {
        return REPO_DIR;
    }

    /** Generic. Returns file.*/
    public static File getStatefile() {
        return STATEFILE;
    }

    /** Generic. Returns file.*/
    public static ArrayList<String> getAncestors() {
        return ancestors;
    }

    /** Generic. Returns file.*/
    public static Repo getRepo() {
        return repo;
    }

    /** Generic. Returns file.*/
    public static TreeMap<String, String> getRemoteRepos() {
        return remoteRepos;
    }

    /** Generic. INPUT.*/
    public static void setStagingArea(TreeMap<String, String> input) {
        stagingArea = input;
    }

    /** Generic. CWD.*/
    public static void setCwd(File cwd) {
        CWD = cwd;
    }

    /** Generic. REPOINITIALIZEDD.*/
    public static void setRepoInitialized(boolean repoInitializedd) {
        Helpers.repoInitialized = repoInitializedd;
    }

    /** Generic. ADDEDFILESS.*/
    public static void setAddedFiles(ArrayList<String> addedFiless) {
        Helpers.addedFiles = addedFiless;
    }

    /** Generic. ALLBLOBSHASEVERR.*/
    public static void setAllBlobShasEver(ArrayList<String> allBlobShasEverr) {
        Helpers.allBlobShasEver = allBlobShasEverr;
    }

    /** Generic. BLOBDIR.*/
    public static void setBlobdir(File blobdir) {
        BLOB_DIR = blobdir;
    }

    /** Generic.  ANCESTORSS.*/
    public static void setAncestors(ArrayList<String> ancestorss) {
        Helpers.ancestors = ancestorss;
    }

    /** Generic. BRANCHESS.*/
    public static void setBranches(TreeMap<String, Branch> branches) {
        Helpers.branches = branches;
    }

    /** Generic. COMMITDIR.*/
    public static void setCommitdir(File commitdir) {
        COMMIT_DIR = commitdir;
    }

    /** Generic. CURRENTBRANCHH.*/
    public static void setCurrentBranch(Branch currentBranch) {
        Helpers.currentBranch = currentBranch;
    }

//    /** Generic. BRANCHNAMESS.*/
//    public static void setBranchNames(ArrayList<String> branchNamess) {
//        Helpers.branchNames = branchNamess;
//    }

    /** Generic. REMOTEDIR.*/
    public static void setRemotedir(File remotedir) {
        REMOTE_DIR = remotedir;
    }

    /** Generic. REMOTENAMESS.*/
    public static void setRemoteNames(ArrayList<String> remoteNames) {
        Helpers.remoteNames = remoteNames;
    }

    /** Generic. REMOVEDFILESS.*/
    public static void setRemovedFiles(ArrayList<String> removedFiless) {
        Helpers.removedFiles = removedFiless;
    }

    /** Generic. REPODIR.*/
    public static void setRepodir(File repodir) {
        REPO_DIR = repodir;
    }

    /** Generic. STATEFILE.*/
    public static void setStatefile(File statefile) {
        STATEFILE = statefile;
    }

    /** Generic. REMOTEREPOSS.*/
    public static void setRemoteRepos(TreeMap<String, String> remoteReposs) {
        Helpers.remoteRepos = remoteReposs;
    }

    /** Generic. R.*/
    public static void setRepo(Repo repo) {
        Helpers.repo = repo;
    }
}

