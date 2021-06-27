# Supplemental source code for *Contrapuntal composition and autonomous style development of organum motets by using AntsOMG*

## Prerequisite

* JDK 16
* [Apache Maven](https://maven.apache.org/) 3.6.3

## Build

```java
mvn clean install
```

## A Quick Run in Test Mode

You can get a quick first glance of the outcome music with the following command under the root folder. With the `TEST` flag the organum motet composer will skip the elaborate style model development process.

```java
mvn verify -Dexec.args="TEST"
```

The generated music files in `.musicxml` format can be found in a fixed folder named `/ORGANUM.MOTET_TEST` under `/_projects` folder and can be viewed with music notation softwares like [MuseScore](https://musescore.org/) or online viewers like [Open Sheet Music Display](https://opensheetmusicdisplay.github.io/demo/). Please be noticed that if you execute the command again with `TEST` flag, the new result will overwrite the previous one.

## A Full Run Based On Specified Cantus Firmus Collections

The following command will produce ten Organum Motets per composer by default, with full style model development process.

```java
mvn verify -Dexec.args="CF=GENERATE_CF_21-14-35-417558700"
```

This will take much longer time to finish. The result will be stored in a folder named with prefix `ORGANUM.MOTET` under `/_projects` folder.

You can also specify your own cantus firmus folder. Just put it under `/_cf `folder and specify the cantus firmus folder name with the `CF` parameter above.

Refer to our previous work [_AntsOMG: A Framework Aiming to Automate Creativity and Intelligent Behavior with a Showcase on Cantus Firmus Composition and Style Development_](https://www.mdpi.com/2079-9292/9/8/1212) for more information about the generation of cantus firmus.
