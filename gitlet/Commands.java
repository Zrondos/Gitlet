package gitlet;

import java.io.File;
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

public class Commands {

    /**
     * Generic. FILENAME
     */
    public static void add(String fileName) {

        TreeMap<String, String> stagingArea = Helpers.getStagingArea();
        if (stagingArea.containsKey(fileName)) {
            String newSha = Helpers.filePathToSha(fileName);
            if (newSha.equals(stagingArea.get(fileName))) {
                System.out.println("Nothing changed");
                return;
            }
        }

        Blob blob = new Blob(fileName);
        String blobString = blob.getBlobShaName();

        Commit headCommit = Helpers.getHeadCommit();
        if (headCommit.getFileToBlobMapping().containsKey(fileName)) {
            if (headCommit.getFileToBlobMapping().
                    get(fileName).equals(blobString)) {
                Helpers.getRemovedFiles().remove(fileName);
                return;
            }
        }

        stagingArea.put(fileName, blobString);

        Utils.writeObject(Helpers.getBlobFilePath(blobString), blob);
        Helpers.getAddedFiles().add(fileName);
        Helpers.getRemovedFiles().remove(fileName);
    }

    /**
     * Generic. MESSAGE and REPO.
     */
    public static void commit(String message, Repo repo) {
        TreeMap<String, String> stagingArea = Helpers.getStagingArea();
        Commit parent = Helpers.getHeadCommit();
        Commit newCommit = new Commit(message, stagingArea, parent, null);
        repo.getCurrentBranch().setHead(newCommit.getShaString());
        repo.setAddedFiles(new ArrayList<String>());
        repo.setRemovedFiles(new ArrayList<String>());
        if (newCommit.getParent() == null) {
            newCommit.getParent().setFileToBlobMapping
                (new TreeMap<String, String>());
        } else {
            newCommit.getParent().setFileToBlobMapping(repo.getStagingArea());
            Helpers.setStagingArea(newCommit.getParent()
                    .getFileToBlobMapping());
        }
        repo.saveState();
    }

    /**
     * Generic. CURRENTCOMMIT
     */
    public static void log(Commit currentCommit) {
        String output = "";
        while (currentCommit != null) {
            output += Helpers.getMetaData(currentCommit);
            currentCommit = currentCommit.getParent();
        }
        int length = output.length();
        output = output.substring(0, length - 1);
        System.out.println(output);
    }

    /**
     * Generic. FILENAME
     */
    public static void checkoutFile(String fileName) {
        Commit headCommit = Helpers.getHeadCommit();
        if (!headCommit.getFileToBlobMapping().containsKey(fileName)) {
            System.out.println("File does not exist in that commit");
        }
        String replacementBlobSha =
                headCommit.getFileToBlobMapping().get(fileName);
        File blobFile = Helpers.getBlobFilePath(replacementBlobSha);
        Blob newBlob = Utils.readObject(blobFile, Blob.class);
        File fileToReplace = new File(Helpers.getCWD()
                + "/" + fileName);
        Utils.writeContents(fileToReplace, newBlob.getSerializedFile());
    }

    /**
     * Generic. COMMITID and FILENAME
     */
    public static void checkoutFileFromCommit(String commitID,
                                              String fileName) {
        if (commitID.length() <= 9) {
            for (String commitFileName
                    : Utils.plainFilenamesIn(Helpers.getCommitdir())) {
                String sub = commitFileName.substring(0, 9);
                if (sub.equals(commitID)) {
                    commitID = commitFileName;
                    break;
                }
            }
        }

        Commit commitToSearch = Helpers.getCommitFromID(commitID);

        if (commitToSearch == null) {
            System.out.println("No commit with that id exists");
            return;
        }

        if (commitToSearch.getFileToBlobMapping().containsKey(fileName)) {
            String replacementBlobSha =
                    commitToSearch.getFileToBlobMapping().get(fileName);
            File blobFile = Helpers.getBlobFilePath(replacementBlobSha);
            Blob newBlob = Utils.readObject(blobFile, Blob.class);
            File fileToReplace = new File(Helpers.getCWD()
                    + "/" + fileName);
            Utils.writeContents(fileToReplace, newBlob.getSerializedFile());
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    /**
     * Generic. Has BRANCHNAME
     */
    public static void checkoutBranch(String branchName) {

        if (Helpers.getCurrentBranch().getName().equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        if (!Helpers.getBranches().containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }

        Commit branchHead = Helpers.getBranchHeadCommit(branchName);
        TreeMap<String, String> currentStagingArea =
                Helpers.getStagingArea();

        for (String fileName
                : Utils.plainFilenamesIn
                (Helpers.getCWD())) {
            if (!currentStagingArea.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }

//        if (Helpers.getRepo().getRemoteBranchNames().contains(branchName)) {
//            File cwd = Helpers.getCWD();
//            Helpers.setCwd(new File(cwd + "/" +Helpers.getRepo().getBranchToHome().get(branchName)));
//            String repoName = Helpers.getRepo().getBranchToRemote().get(branchName);
//
//
//        }

        Helpers.setStagingArea(branchHead.getFileToBlobMapping());
        Helpers.getRepo().setStagingArea
            (branchHead.getFileToBlobMapping());
        TreeMap<String, String> x = Helpers.getRepo().getStagingArea();
        TreeMap<String, String> y = branchHead.getFileToBlobMapping();
        for (String fileName : branchHead.getFileToBlobMapping().keySet()) {
            File cwdFile = Helpers.getCWDFile("/" + fileName);
            String replacementBlobSha =
                    branchHead.getFileToBlobMapping().get(fileName);
            File blobFile = Helpers.getBlobFilePath(replacementBlobSha);
            Blob newBlob = Utils.readObject(blobFile, Blob.class);
            Utils.writeContents(cwdFile, newBlob.getSerializedFile());
        }
        List<String> cWDList = Utils.plainFilenamesIn(Helpers.getCWD());
        for (String fileName : cWDList) {
            if (!branchHead.getFileToBlobMapping().containsKey(fileName)) {
                Utils.restrictedDelete(Helpers.getFilePath(fileName));
            }
        }
        Helpers.setHeadBranch(branchName);
        Helpers.getRepo().
                setCurrentBranch(Helpers.getBranches().get(branchName));
        Helpers.getRepo().saveState();
    }

    /**
     * Generic.
     */
    public static void globalLog() {
        String output = "";
        for (String fileSha
                : Utils.plainFilenamesIn(Helpers.getCommitdir())) {
            Commit commit = Helpers.getCommitFromID(fileSha);
            output += Helpers.getMetaData(commit);
        }
        int length = output.length();
        output = output.substring(0, length - 1);
        System.out.println(output);
    }

    /**
     * Generic. Has ARGS.
     */
    public static void find(String[] args) {
        String inputMessage = args[1];
        String output = "";
        for (String fileSha
                : Utils.plainFilenamesIn(Helpers.getCommitdir())) {
            Commit commit = Helpers.getCommitFromID(fileSha);
            if (commit.getMessage().equals(inputMessage)) {
                String toAdd = commit.getShaString().substring(1);
                output += toAdd + "\n";
            }
        }
        if (output.equals("")) {
            System.out.println("Found no commit with that message.");
        } else {
            System.out.println(output);
        }
    }

    /**
     * Generic. Has ARGS
     */
    public static void remove(String[] args) {
        String fileName = args[1];
        TreeMap<String, String> stagingArea = Helpers.getStagingArea();
        Commit headCommit = Helpers.getHeadCommit();
        boolean inStagingArea = stagingArea.containsKey(fileName);
        boolean tracked =
                headCommit.getFileToBlobMapping()
                        .containsKey(fileName);
        if (!inStagingArea && !tracked) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (!tracked && inStagingArea) {
            stagingArea.remove(fileName);
            Helpers.getAddedFiles().remove(fileName);
            return;
        }
        if (inStagingArea) {
            stagingArea.remove(fileName);
        }

        if (tracked) {
            File filePath = Helpers.getFilePath(fileName);
            Utils.restrictedDelete(filePath);
        }
        Helpers.getRemovedFiles().add(fileName);
        Helpers.getAddedFiles().remove(fileName);

    }

    /**
     * Generic. Has ARGS
     */
    public static void removeBranch(String[] args) {
        String branchName = args[1];
        Helpers.deletedBranch(branchName);
        Helpers.getBranches().remove(branchName);
        Helpers.getRepo().saveState();
    }

    /**
     * Generic.
     */
    public static void status() {
        List<String> cWDFiles =
                Utils.plainFilenamesIn
                        (Helpers.getCWD());
        String output = "=== Branches === \n";
        String currentBranch = Helpers.getCurrentBranch().getName();
        output += "*" + currentBranch + "\n";
        for (String branchName : Helpers.getBranches().keySet()) {
            if (!branchName.equals(currentBranch)) {
                output += branchName + "\n";
            }
        }
        output += "\n";

        output += "=== Staged Files === \n";
        for (String fileName : Helpers.getAddedFiles()) {
            output += fileName + "\n";
        }
        output += "\n";

        output += "=== Removed Files === \n";
        for (String fileName : Helpers.getRemovedFiles()) {
            output += fileName + "\n";
        }
        output += "\n";

        output += "=== Modifications Not Staged For Commit === \n";

        output += getModifications();


        TreeMap<String, String> stagingArea = Helpers.getStagingArea();

        Commit headCommit = Helpers.getHeadCommit();


        output += '\n';
        output += "=== Untracked Files === \n";
        for (String fileName : cWDFiles) {
            boolean staged = Helpers.getStagingArea()
                    .containsKey(fileName);
            boolean tracked = Helpers.getHeadCommit()
                    .getFileToBlobMapping().containsKey(fileName);
            if (!staged && !tracked) {
                output += fileName + "\n";
            }
        }

        System.out.println(output);
    }

    /**
     * Generic. Has ARGS
     */
    public static void branch(String[] args) {
        String name = args[1];
        if (Helpers.getBranches().containsKey(name)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        String commitSha = Helpers.getCurrentBranch().getHeadSha();
        Branch newBranch = new Branch(name, commitSha);
        Helpers.getBranches().put(name, newBranch);
//        Helpers.getBranchNames().add(name);
        Helpers.getRepo().saveState();
    }

    /**
     * Generic. Has ARGS
     */
    public static void reset(String[] args) {
        String commitID = "c" + args[1];

        if (Helpers.getCommitFromID(commitID) == null) {
            System.out.println("No commit with that id exists.");
            return;
        }

        List<String> fileNames =
                Utils.plainFilenamesIn
                        (Helpers.getCWD());
        Commit currentCommit = Helpers.getHeadCommit();
        TreeMap<String, String> currentStagingArea =
                Helpers.getStagingArea();
        TreeMap<String, String> fileToBlob =
                currentCommit.getFileToBlobMapping();

        for (String fileName : fileNames) {
            if (!currentStagingArea.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }

        Commit resetCommit = Helpers.getCommitFromID(commitID);
        fileToBlob = resetCommit.getFileToBlobMapping();

        for (String fileName : fileNames) {
            if (!fileToBlob.containsKey(fileName)) {
                Utils.restrictedDelete(Helpers.getFilePath(fileName));
            } else {
                Commands.checkoutFileFromCommit(commitID, fileName);
            }
        }
        for (String fileName : fileToBlob.keySet()) {
            Commands.checkoutFileFromCommit(commitID, fileName);
        }
        Helpers.getRepo().setAddedFiles(new ArrayList<String>());
        Helpers.getRepo().setRemovedFiles(new ArrayList<String>());
        Branch currentBranch = Helpers.getCurrentBranch();
        currentBranch.setHead(commitID);
        Helpers.setStagingArea(resetCommit.getFileToBlobMapping());
        Helpers.getRepo().
                setStagingArea(
                        resetCommit.getFileToBlobMapping());

        Helpers.getRepo().saveState();

    }

    /**
     * Helper the returns the modifications.
     */
    public static String getModifications() {

        boolean tracked = false;
        boolean changedInCWD = false;
        boolean staged = false;
        boolean inCWD = false;
        List<String> cwdFileList =
                Utils.plainFilenamesIn
                        (Helpers.getCWD());
        ArrayList<String> toPrint = new ArrayList<String>();

        for (String fileName : cwdFileList) {
            if (Helpers.getHeadCommit().getFileToBlobMapping()
                    .containsKey(fileName)) {
                tracked = true;
                changedInCWD = Helpers.compare
                        (fileName, fileName);
            }
            staged = Helpers.getStagingArea().containsKey(fileName);

            if (tracked && changedInCWD && staged) {
                String outputString = fileName + " (modified)\n";
                toPrint.add(outputString);
            } else if (staged && changedInCWD) {
                String outputString = fileName + " (modified)\n";
                toPrint.add(outputString);
            }
        }
        tracked = false;
        changedInCWD = false;
        staged = false;
        inCWD = false;

        for (String fileName : Helpers.getStagingArea().keySet()) {
            if (Helpers.getStagingArea().containsKey(fileName)) {
                staged = true;
            }
            if (cwdFileList.contains(fileName)) {
                inCWD = true;
            }
            if (Helpers.getHeadCommit()
                    .getFileToBlobMapping().containsKey(fileName)) {
                tracked = true;
            }
            if (staged && !inCWD) {
                String output = fileName + " (deleted)\n";
                toPrint.add(output);
            } else if (staged && tracked && !inCWD) {
                String output = fileName + " (deleted)\n";
                toPrint.add(output);
            }
        }
        Collections.sort(toPrint);
        String output = "";
        for (String line : toPrint) {
            output += line;
        }
        return output;
    }

    /**
     * Generic. Has ARGS
     */
    public static void merge(String[] args) {
        Commit splitPoint = null;
        String givenBranch = args[1];

        if (!Helpers.getBranches().containsKey(givenBranch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        Branch headBranch = Helpers.getCurrentBranch();
        Commit currentBranchCommit = Helpers.
                getBranchHeadCommit(headBranch.getName());

        Commit givenBranchCommit = Helpers.
                getBranchHeadCommit(givenBranch);

        if (checkForUntracked()) {
            return;
        }

        if (Helpers.getAddedFiles().size() != 0
                || Helpers.getRemovedFiles().size() != 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (givenBranch.equals(Helpers.getCurrentBranch().getName())) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }


        LinkedHashSet<String> currentBranchAncestors =
                new LinkedHashSet<String>();
        LinkedHashSet<String> givenBranchAncestors =
                new LinkedHashSet<String>();
        Commit currentParentCommit = currentBranchCommit;
        Commit givenParentCommit = givenBranchCommit;

        currentBranchAncestors = Helpers.
                getAncestors(currentParentCommit);
        currentBranchAncestors.add(currentBranchCommit.getShaString());
        givenBranchAncestors = Helpers.getAncestors(givenParentCommit);
        givenBranchAncestors.add(givenBranchCommit.getShaString());
        for (String shaString : currentBranchAncestors) {
            if (givenBranchAncestors.contains(shaString)) {
//                shaString = shaString.substring(1);
                splitPoint = Helpers.getCommitFromID(shaString);
            }
        }

        ArrayList<TreeMap<String, String>> allFiles =
                merge2(splitPoint, currentBranchCommit,
                        givenBranchCommit, givenBranch);
        if (allFiles == null) {
            return;
        }
        mergeFinal(allFiles, givenBranchCommit, givenBranch);


    }

    /**
     * Generic. ALLFILES GIVENBRANCHCOMMIT GIVENBRANCH
     */
    public static void mergeFinal(ArrayList<TreeMap<String, String>>
                                          allFiles,
                                  Commit givenBranchCommit,
                                  String givenBranch) {

        TreeMap<String, String> splitPointFiles = allFiles.get(0);
        TreeMap<String, String> currentBranchFiles = allFiles.get(1);
        TreeMap<String, String> givenBranchFiles = allFiles.get(2);


        for (String fileName : givenBranchFiles.keySet()) {
            if (currentBranchFiles.containsKey(fileName)
                    && !splitPointFiles.containsKey(fileName)) {
                if (!currentBranchFiles.get(fileName)
                        .equals(givenBranchFiles.get(fileName))) {
                    String currentBlobString = currentBranchFiles.get(fileName);
                    String givenBlobString = givenBranchFiles.get(fileName);
                    String currentBlobContents = Helpers.
                            getBlobContents(currentBlobString);
                    String givenBlobContents = Helpers.
                            getBlobContents(givenBlobString);
                    Helpers.mergeConflict(currentBlobContents,
                            givenBlobContents, fileName);
                }
            } else if (!splitPointFiles.containsKey(fileName)) {
                checkoutFileFromCommit(givenBranchCommit.
                        getShaString(), fileName);
                Helpers.getStagingArea().
                        put(fileName, givenBranchFiles.get(fileName));
                Helpers.getRepo().getStagingArea().
                        put(fileName, givenBranchFiles.get(fileName));
                Helpers.getRepo().getAddedFiles().add(fileName);
            }
        }


        String commitMessage = "Merged " + givenBranch
                + " into " + Helpers.getCurrentBranch().getName()
                + ".";
        Commit commit = new Commit(commitMessage,
                Helpers.getStagingArea(),
                Helpers.getHeadCommit(), givenBranchCommit);
        Helpers.getRepo().setAddedFiles(new ArrayList<String>());
        Helpers.getRepo().setRemovedFiles(new ArrayList<String>());
        Helpers.getCurrentBranch().setHead(commit.getShaString());
        Helpers.getRepo().saveState();
    }

    /**
     * Generic. Returns bool.
     */
    public static boolean checkForUntracked() {
        for (String fileName
                : Utils.plainFilenamesIn(Helpers.getCWD())) {
            boolean staged = Helpers.getStagingArea()
                    .containsKey(fileName);
            boolean tracked = Helpers.getHeadCommit()
                    .getFileToBlobMapping().containsKey(fileName);
            if (!staged && !tracked) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return true;
            }
        }
        return false;
    }

    /**
     * Generic.  SPLITPOINT CURRENTBRANCHCOMMIT
     * GIVENBRANCHCOMMIT GIVENBRANCH RETURNS TREEMAP
     */
    public static ArrayList<TreeMap<String, String>>
        merge2(Commit splitPoint, Commit currentBranchCommit,
           Commit givenBranchCommit, String givenBranch) {
        if (splitPoint.getShaString().
                equals(givenBranchCommit.getShaString())) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
        }

        if (splitPoint.getShaString().
                equals(currentBranchCommit.getShaString())) {
            checkoutBranch(givenBranch);
            System.out.println("Current branch fast-forwarded.");
            return null;
        }

        TreeMap<String, String> splitPointFiles =
                splitPoint.getFileToBlobMapping();
        TreeMap<String, String> currentBranchFiles =
                currentBranchCommit.getFileToBlobMapping();
        TreeMap<String, String> givenBranchFiles =
                givenBranchCommit.getFileToBlobMapping();

        ArrayList<TreeMap<String, String>> allFiles = new
                ArrayList<TreeMap<String, String>>() {
            {
                add(splitPointFiles);
                add(currentBranchFiles);
                add(givenBranchFiles);
            }
        };
        for (String fileName : splitPointFiles.keySet()) {
            merge3(splitPoint, givenBranch, currentBranchCommit,
                    givenBranchCommit,
                    allFiles, fileName);
        }
        return allFiles;
    }

    /**
     * Generic.  SPLITPOINT CURRENTBRANCHCOMMITT GIVENBRANCHCOMMITT
     * SPLITPOINTFILESS CURRENTBRANCHFILESS GIVENBRANCHCOMMIT
     * GIVENBRANCHFILESS GIVENBRANCH CURRENTBRANCHCOMMIT ALLFILES FILENAME
     */
    public static void merge3(Commit splitPoint, String givenBranch,
                              Commit currentBranchCommitt,
                              Commit givenBranchCommit,
                              ArrayList<TreeMap<String, String>>
                                      allFiles, String fileName) {
        TreeMap<String, String> splitPointFiles = allFiles.get(0);
        TreeMap<String, String> currentBranchFiles = allFiles.get(1);
        TreeMap<String, String> givenBranchFiles = allFiles.get(2);

        boolean[] conditions = getConditions(currentBranchFiles,
                givenBranchFiles,
                givenBranchCommit, splitPointFiles, fileName);

        boolean condition1 = conditions[0];
        boolean condition2 = conditions[1];
        boolean condition3 = conditions[2];
        String currentContents = "";
        String givenContents = "";
        if (condition1) {
            String currentBlobString = currentBranchFiles.get(fileName);
            File currentBlobFile =
                    Helpers.getBlobFilePath(currentBlobString);
            Blob currentBlob =
                    Utils.readObject(currentBlobFile, Blob.class);
            currentContents = currentBlob.getContents();

            String givenBlobString = givenBranchFiles.get(fileName);
            File givenBlobFile =
                    Helpers.getBlobFilePath(givenBlobString);
            Blob givenBlob =
                    Utils.readObject(givenBlobFile, Blob.class);
            givenContents = givenBlob.getContents();
        } else if (condition2) {
            String currentBlobString = currentBranchFiles.get(fileName);
            File currentBlobFile =
                    Helpers.getBlobFilePath(currentBlobString);
            Blob currentBlob =
                    Utils.readObject(currentBlobFile, Blob.class);
            currentContents = currentBlob.getContents();
        } else if (condition3) {
            String givenBlobString = givenBranchFiles.get(fileName);
            File givenBlobFile =
                    Helpers.getBlobFilePath(givenBlobString);
            Blob givenBlob =
                    Utils.readObject(givenBlobFile, Blob.class);
            givenContents = givenBlob.getContents();

        }
        boolean[] boolArray = new boolean[]
            {condition1, condition2, condition3};

        String[] givenBranchandFileName =
                new String[]{givenBranch, fileName};

        merge4(splitPointFiles, givenBranchFiles,
                currentBranchFiles, givenBranchCommit,
                givenBranchandFileName, boolArray,
                currentContents, givenContents);
    }

    /**
     * Generic. Has CURRENTBRANCHFILES GIVENBRANCHFILES
     * GIVENBRANCHCOMMIT SPLITPOINTFILES
     * FILENAME. Returns bool.
     */
    public static boolean[] getConditions(TreeMap<String, String>
                                                  currentBranchFiles,
                                          TreeMap<String, String>
                                                  givenBranchFiles,
                                          Commit givenBranchCommit,
                                          TreeMap<String, String>
                                                  splitPointFiles,
                                          String fileName) {
        boolean inCurrent = false;
        boolean inGiven = false;
        boolean currentModified = false;
        boolean givenModified = false;
        boolean currentEqualsGiven = false;
        if (currentBranchFiles.containsKey(fileName)) {
            inCurrent = true;
            if (!splitPointFiles.get(fileName).
                    equals(currentBranchFiles.get(fileName))) {
                currentModified = true;
            }
        }
        if (givenBranchFiles.containsKey(fileName)) {
            inGiven = true;
            if (!splitPointFiles.get(fileName).
                    equals(givenBranchFiles.get(fileName))) {
                givenModified = true;
            }
        }
        if (inGiven && inCurrent) {
            currentEqualsGiven = givenBranchFiles.get(fileName)
                .equals(currentBranchFiles.get(fileName));
        }

        if (givenModified && !currentModified) {
            checkoutFileFromCommit(givenBranchCommit.
                    getShaString(), fileName);
        }

        if (!currentModified && !inGiven) {
            Helpers.getRepo().getStagingArea().remove(fileName);
            Helpers.getRemovedFiles().add(fileName);
            Utils.restrictedDelete(Helpers.getFilePath(fileName));
        }

        boolean condition1 = currentModified && givenModified
                && !currentEqualsGiven;
        boolean condition2 = currentModified && !inGiven;
        boolean condition3 = givenModified && !inCurrent;
        return new boolean[]{condition1, condition2, condition3};
    }


    /**
     * Generic.  SPLITPOINTFILES GIVENBRANCHFILES
     * CURRENTBRANCHFILES GIVENBRANCHCOMMIT
     * GIVENBRANCHCOMMIT GIVENBRANCHANDFILENAME
     * BOOLARRAY CURRENTCONTENTS GIVENCONTENTS
     */
    public static void merge4(TreeMap<String, String> splitPointFiles,
                              TreeMap<String, String> givenBranchFiles,
                              TreeMap<String, String> currentBranchFiles,
                              Commit givenBranchCommit,
                              String[] givenBranchandFileName,
                              boolean[] boolArray,
                              String currentContents,
                              String givenContents) {
        String givenBranch = givenBranchandFileName[0];
        String fileNamee = givenBranchandFileName[1];
        boolean condition1 = boolArray[0];
        boolean condition2 = boolArray[1];
        boolean condition3 = boolArray[2];
        if (condition1 || condition2 || condition3) {
            String output = "<<<<<<< HEAD\n"
                    + currentContents
                    + "=======\n"
                    + givenContents
                    + ">>>>>>>\n";
            File cwdFile = Helpers.getCWDFile("/" + fileNamee);
            Utils.writeContents(cwdFile, output);
            Commands.add(fileNamee);
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * Generic. Has ARGS
     */
    public static void addRemote(String[] args) {
        String remoteName = args[1];
        String remoteAddress = args[2];
        if (Helpers.getRemoteNames().contains(remoteName)) {
            System.out.println("A remote with that name already exists.");
        }
        RemoteRepo newRemote = new RemoteRepo(remoteName, remoteAddress);
        Helpers.getRepo().getRemoteNames().add(remoteName);
        Helpers.getRepo().getRemoteRepos()
                .put(remoteName, remoteAddress);
        Helpers.getRepo().saveState();
    }

    /**
     * Generic. Has ARGS
     */
    public static void removeRemote(String[] args) {
        String remoteName = args[1];
        if (!Helpers.getRemoteNames().contains(remoteName)) {
            System.out.println("A remote with that name does not exist.");
        }
    }

    /**
     * Generic. Has ARGS
     */
    public static void push(String[] args) {
        String remoteName = args[1];
        String remoteBranch = args[2];
    }

    /**
     * Generic. Has ARGS
     */
    public static void fetch(String[] args) {
        String remoteName = args[1];
        String remoteBranch = args[2];
        Repo current = Helpers.getRepo();
        RemoteRepo remote = new RemoteRepo(remoteName,
                Helpers.getRemoteRepos()
                        .get(remoteName));

//        Repo remoteRepo = remote.getRepo();
//        if (!Helpers.getRepo().getBranchNames().contains(remoteName + "/" + remoteBranch)) {
//            Helpers.getRepo().getBranchNames()
//                    .add(remoteName + "/" + remoteBranch);
//            current.getBranchNames().add(remoteName + "/" + remoteBranch);
//            Helpers.setRepo(remoteRepo);
//            Helpers.getRepo().getBranchToRemote().put(remoteName + "/" + remoteBranch, remoteName);
//            Branch newBranch = new Branch(remoteName + "/" + remoteBranch, Helpers.getHeadCommit().getShaString());
//            Helpers.setRepo(current);
//            current.getBranches().put(remoteName, newBranch);
//            Helpers.getBranches().add(newBranch);
//            current.getRemoteBranchNames().add(newBranch.getName());
//            Helpers.setRepo(remoteRepo);
//        }

//        current.getBranchToHome().put(remoteName + "/" + remoteBranch, remote.getRemoteAddress() );

        ArrayList<Commit> commitArray = remote.getCommitArray();
        for (Commit commit : commitArray) {
            byte[] serializedCommit = Utils.serialize(commit);
            String shaString = "/c" + Utils.sha1(serializedCommit);
            File commitFile = new File(Helpers.getCWD()
                    + "/.gitlet/commits" + shaString);
            Utils.writeObject(commitFile, commit);
        }

        ArrayList<Blob> blobArray = remote.getBlobArray();
        for (Blob blob : blobArray) {
            File blobFile = new File(Helpers.getCWD()
                    + "/.gitlet/blobs" + blob.getBlobShaName());
            Utils.writeObject(blobFile, blob.getSerializedFile());
        }
        current.saveState();

        return;
    }

    /**
     * Generic. Has ARGS
     */
    public static void pull(String[] args) {
        String remoteName = args[1];
        String remoteBranch = args[2];
    }


}


