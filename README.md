

# HerbrandEquivalenceJB
---
This repository is the implementation of algorithm given in **A fix-point characterization of Herbrand equivalence of expressions in data flow frameworks** by _Jasine Babu_, _K. Murali Krishnan_ and _Vineeth Paleri_.

## Pre-requisites
---
- Get **JDK (Java Development Kit)** in your computer. Check if you have JDK in your computer using `<$ java -version>` command.
- Get **log4j** API for logging. Follow the following Steps:
    - Goto [log4j](http://logging.apache.org/log4j/) website.
    - Download apache-log4j-x.x.x.tar.gz from the specified site.
    - Unzip and untar the downloaded file in /usr/local/ directory as follows:
        ```sh
        $ gunzip apache-log4j-1.2.17.tar.gz
        $ tar -xvf apache-log4j-1.2.17.tar
        ```
    - Now set the PATH and CLASSPATH appropriately.
        ```sh
        $ pwd
        /usr/local/apache-log4j-1.2.17
        $ export CLASSPATH=$CLASSPATH:/usr/local/apache-log4j-1.2.17/log4j-1.2.17.jar
        $ export PATH=$PATH:/usr/local/apache-log4j-1.2.17/
        ```

## How to use
---
1. Clone this repository in your computer.
2. Extract the files. Note: make sure that the directory in which you are extracting has a write permission.
3. Go inside the _**HerbrandEquivalenceJB**_ directory.
5. Put your input files inside the _**inputFiles**_ directory.
6. Go to the _**src**_ directory and run the following commands.
    ```sh
    $ pwd
    /home/<username>/HerbrandEquivalenceJB/src
    $ javac herbrandEquivalence.java
    $ java herbrandEquivalence
    ```
7. Program will generate/update the following files/folders.
    - _**debug**_ folder which contain the debug files. Debugging data of a input file will append to the existing debugging file. There are two types of files:
        - **level 0**- Less detailed debugging files. Showing what are the outputs.
        - **level 1**- More detailed debugging files. Showing in which all functions it went with their outputs and results.
    - _**redundantExpression**_ folder will contain the expressions which are redundant to compute because they are already computed beforehead. Each input file will contain its own redundant expression file named as <input file name>.re.txt.
    - _**transferedCode**_ folder contain the modified input files which will run as same as input files other than the redundant expression need not be computed again. Each input file will contain its own transfered code file named as <input file name>.tc.txt.

