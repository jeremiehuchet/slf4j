
slf4j-itinerennes
==================

about 
-----

This project is a fork of [SLF4J](https://github.com/qos-ch/slf4j). Only module _slf4j-android_ have been modified.

[![Build Status](https://travis-ci.org/kops/slf4j.png?branch=master)](https://travis-ci.org/kops/slf4j)

features added from slf4j-android to slf4j-itinerennes
------------------------------------------------------

This fork aims to provide more flexibility on log level configuration by introducing a configuration file. 

Restriction to 23 characted have been taken off so tag names doesn't have to be compressed/truncated when using the library with Android > 2.1

how to use it ?
---------------

Use a file `assets/slf4j.properties` to configure log levels.

```
root = WARN
com.groupId.artifactId = DEBUG
com.groupId.artifactId.package.BusinessImpl = TRACE
org.library.tools = INFO
```
