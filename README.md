# RiVal


[![Build Status](https://travis-ci.org/recommenders/rival.png?branch=master)](https://travis-ci.org/recommenders/rival)
[![Join the chat at https://gitter.im/recommenders/rival](https://badges.gitter.im/recommenders/rival.svg)](https://gitter.im/recommenders/rival?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![codecov](https://codecov.io/gh/recommenders/rival/branch/master/graph/badge.svg)](https://codecov.io/gh/recommenders/rival)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)



**RiVal** is a toolkit for data splitting and evaluation of recommender systems. This page contains information on how to work with the RiVal code and how to compile RiVal.
For more information about RiVal and the documentation, visit the RiVal [website](http://rival.recommenders.net) or [wiki][].
If you have not used RiVal before, do check out the [Getting Started][] guide.

[website]: http://rival.recommenders.net
[wiki]: http://github.com/recommenders/rival/wiki/
[Getting Started]: http://github.com/recommenders/rival/wiki/GettingStarted

RiVal is made available under Apache License, Version 2.0.

## Installation and Dependency Management

RiVal is built and deployed via [Maven][]. In order to install it, check out
this repository and run `mvn install`. This will make it available for other Maven projects as a dependency. 

[Maven]: http://maven.apache.org

## Modules

RiVal is comprised of one top-level module and four sub-modules. The top-level `rival`
module is a container module used to build the submodules and provide all needed settings
and dependencies.  The five sub-modules are as follows:

* `rival-core` -- the common data structures and similar object used throughout RiVal.
* `rival-evaluate` -- the evaluation module, contains metrics and strategies used for evaluation.
* `rival-examples` -- a module containing examples on how to use RiVal programmatically.
* `rival-recommend` -- the recommendation modeule, contains hooks to Apache Mahout and LensKit.
* `rival-split` -- the data splitting module, contains different data splitting strategies.
* `rival-package` -- a configuration module for bulding rival distributions.

## Contributing to RiVal
  
We're very happy if you want to contribute to RiVal. If you are not sure on what to work on, have a look a the current [issues](/../../issues/).

If you want to contribute, do it in the form of GitHub pull requests. To do this:


1. Find the [issue](issues) you want to fix, or create a new one describing what your contribution fixes.
1. Fork RiVal [rival](/../../) on GitHub
2. Push your changes to your fork
3. Submit a pull request via the GitHub Web interface

Note that all your contributions will be licensed under RiVal's copyright licences (Apache). If you are committing a new class, make sure to include the copyright statement at the top.

## Mailing list

To subscribe to the rival mailing list, visit and join the [rival-users](https://groups.google.com/forum/#!forum/rival-user) Google group.

## More information
* [RiVal website](http://rival.recommenders.net)
* RiVal in [research](../../wiki/Research)
* RiVal on [Maven central](http://search.maven.org/#search%7Cga%7C1%7Cnet.recommenders.rival)
