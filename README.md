+++
date = 2018-07-30
publishDate = 2018-08-04
title = "Intro to Finagle services with Scala and Gradle."
description = "Finagle lets you develop and deploy services easily."
toc = true
categories = ["scala","twitter","finagle"]
tags = ["functional","scala","services","demo"]
+++
# Standing up HTTP/RPC services with Twitter/Finagle API

## Why Finagle

[Finagle](https://twitter.github.io/finagle/) is a Service building kit that lets developers engage RPC/Async components with ease. Although Finagle is adept in cloud environments - this intro help us to discover -
we will show how to deploy our finagle service to a local machine sans cloud orchestration.
Think of Finagle as a platform for delivering bi-directionsal asynchronous function executions.

### JVM Functions in Scala

Because we are using [Scala](https://scala.org), and Scala is functional.
Finagle is written in Scala, and works best with applications also written in Scala. You can 
prefer to choose ordinary Java or some other JVM language.  The caveats there are in type inferences and how to ferret the specific types you need to expoe as your RPC construction.
Make sure to read [The following link](http://writing_scala_apps_with_java).

## Build tools

Lets discuss a topic thats probably going to either make or break your decision to use Finagle.  We will be using SBT to start.  Later we will switch to something like Gradle - because I find a lot of Java Developers prefer Gradle in their own build tool-boxes as well.

### SBT

### Maybe Gradle

## Code

### A Simple HTTP Service

### A Simple HTTP Client

## Conclusion & Links

### Finagle Introduction

* https://blog.twitter.com/engineering/en_us/a/2011/finagle-a-protocol-agnostic-rpc-system.html

### Backpressure (Warning: MATH, LOTS OF MATH)

* http://anrg.usc.edu/www/papers/IPSN10_Moeller_Sridharan_Krishnamachari_Gnawali.pdf

### Another False Math Proof

* https://www.math.hmc.edu/funfacts/ffiles/10001.1-8.shtml

### Services for scala.

*
