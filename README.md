General File Utility
===

***Build***
*  gradle uberjar

***Execution***
*  java -jar "build/libs/GeneralFileUtility-1.0.jar"

***Usage***

**Directory Scan**

A specified directory is processed and all files at and beneath this directory are analyzed for likeness.  The intention here is to identify duplicate files.  The message digest is computed lazily for performance reasons so it will not appear for each file and may show `Not Necessary`.  Multiple directories can be processed sequentially and the results are joined together.  When finished with the current scope, click `Clear` to reset the working set of files under analysis.

![](https://s3-us-west-2.amazonaws.com/paduana-photos/general.file.utility/directory.scan.png)

**Duplicate Files**

The duplicate files tab shows the results of the directory scans since the last `Clear`.  The top table shows a unique collection of message digests with one file per digest chosen at random to give a sense of what the file is.  (It's not "random", but there is no known preference or natural ordering of the duplicate files, so one is chosen arbitrarily.)

When selecting a unique message digest, all file instances will appear in the table below.  This identifies each file that has a duplicate somewhere within the current search scope.  From here, a right click will allow `Open`, `Browse`, `Delete`, and `Convert to Hard Link` functionality.  The hard link functionality currently only works on Windows and will generate this command:  `cmd /C mklink /H "<path>" "<alternativePath>"` for each row selected.  It is possible to select every file in the bottom table, and create a hard link for all of them.  This would result in a scenario where the data are stored in just one location on the disk but file handles exist in many locations.

![](https://s3-us-west-2.amazonaws.com/paduana-photos/general.file.utility/duplicate.files.png)

**Smart Merge**

The duplicate file finder is good, but it can be tedious to select one duplicate file at a time to perform deletes or create hard links.  It is sometimes much easier to take one directory and move it to another sans duplicates starting fresh.  For instance, if you've scraped a website and have 10,000 images saved, you may end up with hundreds of duplicates.  With the Smart Merge feature you can take your source directory and copy it to a new directory and end up with only unique files.  Full paths will be respected and new subfolders will be created in the destination directory.  The only issue, however, is that just one file from a collection of duplicates is chosen arbitrarily for the move.  The others are left behind.  This may be fine depending on the usage, but the behavior should be known in advance.

![](https://s3-us-west-2.amazonaws.com/paduana-photos/general.file.utility/smart.merge.png)

**Bulk Rename**

The Bulk Rename tool works recursively within a directory and files can be included or excluded with a regular expression.  Files can be further excluded with a `Skip` or `Enable` attribute per file.  There are multiple editor strategies for renaming, including `Trim`, `Replace`, `Date-ify`, and `Series Editor`.  Select a directory, edit the `Regex Match` if desired, select an `Editor`, supply the necessary values in the right pane, select `Preview`, and then select `Execute` if the `Expected Output` all appears to be correct.

*Trim*

This strategy will delete a specified number of characters from the left or right side of the filename.  If the specified number exceeds the length of the filename, the last remaining character will be preserved for a final length of one not including the extension.

![](https://s3-us-west-2.amazonaws.com/paduana-photos/general.file.utility/rename.trim.png)

*Replace*

This strategy will replace all occurrences of a specified value with a different specified value.

![](https://s3-us-west-2.amazonaws.com/paduana-photos/general.file.utility/rename.replace.png)

*Date-ify*

This strategy will change the filename to a `SimpleDateFormat` representation of the file's `lastModified` attribute.  The format can be changed as desired.  There will also be appended a numerical identifier extracted from the original filename to attempt to preserve ordering.  There is at least one particular use case for this.  DSLR cameras can take many photos per second, but sometimes the timestamp is not updated with greater resolution than one second.  This will result in multiple files with the same name.  The solution is to provide a discriminator value, which can easily be derived from filenames such as `IMG_6723.JPG`.  The `6723` portion is appended to the end of the `SimpleDateFormat` value.  There are plans to make this optional.

![](https://s3-us-west-2.amazonaws.com/paduana-photos/general.file.utility/rename.dateify.png)

*Series Editor*

This strategy will attempt to organize a collection of media files based on show name, season/episode value, episode name, and resolution.  This editor is very specific to media files such as television shows.  All additional junk will be removed from the name.  A search is performed to find the correct season and episode numbers, but it may not always be correct.  It is best to proofread the `Expected Output` before clicking `Execute`.  If a change is desired, simply edit the value under the `Expected Output` column and it will apply to the final filename.

If the other files do not match this type, the suggested filename will not be correct.  It is best to `Skip` these files by selecting them and right clicking as seen below:

![]https://s3-us-west-2.amazonaws.com/paduana-photos/general.file.utility/rename.show.skip.png)

Or by altering the `Regex Match` to only include the correct files as seen below:

![](https://s3-us-west-2.amazonaws.com/paduana-photos/general.file.utility/rename.show.valid.png)

**Binary Operations**

Input is currently accepted by typing or pasting hexadecimal values into the bottom text area.  The characters and digits will automatically be spaced and cleaned of non-hex characters.  For instance, pasting `48656c6c6f2c2074686973206973207468652062696e61727920656469746f72` will result in 

`4865 6c6c 6f2c 2074 6869 7320 6973 2074`<br>
`6865 2062 696e 6172 7920 6564 6974 6f72`

The human-readable interpretation of these values will appear in the right pane.  The counter will appear in the left pane.  The exact character length, bit length, and byte length will appear in the status bar at the bottom of the window.

A variety of message digests, longitudinal parity calculations, numerical representations, and encoding representations will appear in the top table with each character typed.  This is intended to be used for small data frames for analysis and debugging.  Any portion of the contents can be sub-selected for specific analysis as seen in the last image.

![](https://s3-us-west-2.amazonaws.com/paduana-photos/general.file.utility/binary.operations.png)

![](https://s3-us-west-2.amazonaws.com/paduana-photos/general.file.utility/binary.operations.subselect.png)
