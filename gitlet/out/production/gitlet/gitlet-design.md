# Gitlet Design Document

**Zachary Rondos**:

## Classes and Data Structures
Main
Fields
-Static final CWD
-Static Repository: Constructs Repository object
Functions
-Main Method: takes in args, then calls command class process method


Repository
Called by init
Sets up persistence

Contains commit tree strings
	Contains list of all Blob strings
	Head Commit - String
	List of Branch names and commit ID and where they start
	
Function
		findShaPath(Blob) - takes in string of blob and returns string of path to file/
		FindShaPath(Commit) - takes in id of commit and returns string of path to commit 
		FindShaContents(Blob) - takes in string of blob and returns contents of Blob
		FindShaContents(Commit) - takes in string of committ and returns contents of commit

Branch
-name (default is master)
-Branch Lead: String of SHA identifier
Blob
Constructor
-Sha-1 ID
- File url
  getter: getSHA
  getter: getURL
  Committs? List of commits pointing to this blob
  Override: toString: prints out Blob information


Commit

Constructor
Instance Variables
Date/Time
Message
String: Sha-1
String: Parent Commit
Blobs: Array w/ Sha-1 Strings of Blobs
Stage: list of files in staging area = Stage.getCommits
Methods
Override: toString - Creates String representation of commit w/ commit id, time, and message.
getter: parent
getter: blobs
getSha
getMessage
getTime

CommandClass
Takes input args from Main class
Fields
Repository

Methods
Main - calls appropriate method for all possible commands. If not valid command, print error
init - initializes .gitlet repository in CWD. Calls commit w/ currentTime and message: "initial commit"
add - Adds file to staging area (Stage Class)
commit - Creates Commit Object w/ time, message, Array w/ String of Blob Shas.  Clears Staging Area Object, String name of Parent ID
remove - Calls Stage remove method w/ file name
log - Traverses commit branch, calling .toString of commit class
global log
find - Traverses list of all commit objects, and prints out ID of commits w/ that message
status - Prints out List of Branches from Repository w/ asterix next to current head
check out - Changes current head of repository.  3 Cases based on input argument length
branch - creates new branch object
rm-branch - removes branch from repository list of branches
reset - Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch's head to that commit node. See the intro for an example of what happens to the head pointer after using reset.
merge -
Process Method
Checks length of args: If a user doesn't input any arguments, print the message Please enter a command. and exit.
Checks if repository != null after init case checked: Not in an initialized Gitlet directory
Checks for Errors for each command before passing args to that command: Incorrect operands
Now calls the proper command
Default: Print Error: No command with that name

Error Messages
Checked in Process method of CommandClass

Stage
Field: List of Commits
Methods:
addFile: checks if file is allowed to be added to stage.  Adds to list of commits if allowed.
getCommits - prints out list of commits in current stage
clear: clears staging area after commit is made




## Algorithms

Read-Modify-Write

Reset

Merge




## Persistence





