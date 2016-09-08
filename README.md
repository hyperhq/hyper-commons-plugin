#Deprecated
Moved to https://github.com/jenkinsci/hyper-commons-plugin

# hyper-commons-plugin
=======================

[![Build Status](https://travis-ci.org/hyperhq/hyper-commons-plugin.svg?branch=master)](https://travis-ci.org/hyperhq/hyper-commons-plugin)

This plugin provides common functionality for integrating Jenkins with Hyper_.

<!-- TOC depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Features](#features)
- [Use plugin](#use-plugin)
	- [Prerequisites](#prerequisites)
	- [Install plugin manually](#install-plugin-by-manually)
	- [Config plugin](#config-plugin)
	- [Install hypercli](#install-hypercli)
- [Build plugin](#build-plugin)
	- [Prerequisites](#prerequisites)
	- [Compile](#compile)
	- [Test](#test)
	- [Package](#package)
	- [Install](#install)

<!-- /TOC -->

# Features
Plugin currently supports the following features:

- Install hyper cli
- Set Hyper_ credentials
- Test connection to your account.


# Use plugin

## Prerequisites

- Jenkins
- hypercommon.hpi
- Hyper_ credential

## Install plugin manually

open Jenkins Web UI in web browser

get pre-build `hyper-commons.hpi`

```
Manage Jenkins -> Manage Plugins -> Advanced -> Upload Plugin
```

## Config plugin

login https://console.hyper.sh to receive a `Hyper_ credential`.

```
Manage Jenkins -> Configure System -> Hyper Config
```
![](images/config-plugin.PNG)

## Install hypercli

```
Manage Jenkins -> Configure System ->Install hypercli
```

![](images/install-hypercli.PNG)


# Build plugin

## Prerequisites

- java 1.8+
- maven 3+

## Compile
```
$ mvn compile
```

## Test

compile + test

```
$ mvn test
```

## Package

> **output**: target/hypercommon.hpi

compile + test + package

```
$ mvn package

//skip test
$ mvn package -DskipTests
```

## Install

> **target**: ~/.m2/repository/sh/hyper/plugins/hyper-commons/0.1-SNAPSHOT/hyper-commons-0.1-SNAPSHOT.hpi

compile + test + package + install

```
$ mvn install

//skip test
$ mvn install -DskipTests
