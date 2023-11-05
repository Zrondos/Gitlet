package gitlet;
import java.io.File;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Zachary Rondos
 */

public class ErrorChecker {

    /** Generic. ARGS. Returns bool. */

    public static boolean checkAdd(String[] args) {
        File toAdd = Helpers.getFilePath(args[1]);
        return toAdd.exists();
    }

    /** Generic. ARGS. Returns bool. */
    public static boolean checkCommit(String[] args) {
        if (Helpers.getAddedFiles().size() == 0
                && Helpers.getRemovedFiles().size() == 0) {
            System.out.println("No changes added to the commit.");
            return false;
        }
        if (args.length < 2 || args[1].length() == 0) {
            System.out.println("Please enter a commit message.");
            return false;
        }
        Commit headCommit = Helpers.getHeadCommit();
        if (headCommit.getFileToBlobMapping()
                .equals(Helpers.getStagingArea())) {
            System.out.println("Nothing to commit");
            return false;
        }
        return true;
    }

    /** Generic. ARGS and REPO. Returns bool. */
    public static boolean checkCheckoutBranch(String[] args, Repo repo) {
        String name = args[1];
        return repo.getBranches().containsKey(name);
    }
}
