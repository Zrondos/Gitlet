package gitlet;

import java.io.File;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Zachary Rondos
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    /**
     * Generic.
     */
    private static File CWD = new File(System.getProperty("user.dir"));
    /**
     * Generic.
     */
    private static File REPO_DIR = new File(CWD + "/.gitlet");
    /**
     * Generic.
     */
    private static File stateFile = new File(CWD + "/.gitlet/state");

    /**
     * Generic. ARGS
     */
    public static void main(String... args) {
        Repo repo = null;

        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (!REPO_DIR.exists() && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
        } else {
            if (!args[0].equals("init")) {
                repo = Utils.readObject(stateFile, Repo.class);
                repo.setUpHelpers();
            }
            switch (args[0]) {
                case "init":
                    if (!REPO_DIR.exists())
                        new Repo();
            } else{
                System.out.println("A Gitlet version-control"
                        + " system already "
                        + "exists in the current directory.");
            }
            break;
            case "add":
                if (ErrorChecker.checkAdd(args)) {
                    Commands.add(args[1]);
                    repo.saveState();
                } else {
                    System.out.println("File does not exist.");
                }
                break;
            case "commit":
                if (ErrorChecker.checkCommit(args)) {
                    Commands.commit(args[1], repo);
                }
                break;
            case "checkout":
                if (args.length == 2) {
                    Commands.checkoutBranch(args[1]);
                }
                if (args.length == 3) {
                    Commands.checkoutFile(args[2]);
                }
                if (args.length == 4) {
                    String commitID = args[1];
                    String fileName = args[3];
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                    }
                    Commands.checkoutFileFromCommit("c" + commitID, fileName);
                }
                break;

            default:
                main2(args, repo);

        }
    }

}

    /**
     * Generic. ARGS and REPO
     */
    public static void main2(String[] args, Repo repo) {
        switch (args[0]) {
            case "log":
                Commit c = Helpers.getHeadCommit();
                Commands.log(c);
                break;
            case "global-log":
                Commands.globalLog();
                break;
            case "find":
                Commands.find(args);
                break;
            case "rm":
                Commands.remove(args);
                repo.saveState();
                break;
            case "rm-branch":
                Commands.removeBranch(args);
                break;
            case "status":
                Commands.status();
                break;
            case "branch":
                Commands.branch(args);
                break;
            case "reset":
                Commands.reset(args);
                break;
            case "merge":
                Commands.merge(args);
                break;
            case "add-remote":
                Commands.addRemote(args);
                break;
            case "rm-remote":
                Commands.removeRemote(args);
                break;
            case "fetch":
                Commands.fetch(args);
                break;
            default:
                System.out.println("No command with that name exists.");
        }
        repo.saveState();
    }
}
