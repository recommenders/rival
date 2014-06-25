# RiVal

[![Build Status](https://travis-ci.org/recommenders/rival.png?branch=master)](https://travis-ci.org/recommenders/rival)

RiVal is a toolkit for data splitting and evaluation of recommender systems. This file contains information on how to work with the RiVal code and how to compile RiVal.
For more information about RiVal and the documentation, visit the RiVal [website](http://rival.recommenders.net) or [wiki][].
If you have not used RiVal before, do check out the [Getting Started][] guide.

[website]: http://rival.recommenders.net
[wiki]: http://github.com/recommenders/rival/wiki/
[Getting Started]: http://github.com/recommenders/rival/wiki/GettingStarted

RiVal is made available under the GNU Lesser General Public License
(LGPL), version 2.1 or later.

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
  
We'll be very happy if you want to contribute to RiVal. If you are not sure on what to work on, have a look a the current tickets [Trac](/../../issues/).

If you want to contribute, to it in the form of GitHub pull requests. To do this:

1. Fork RiVal [rival](/../../) on GitHub
2. Push your changes to your fork
3. Submit a pull request via the GitHub Web interface

Note that all your contributions will be licensed under RiVal's copyright licences (LGPL).

## Mailing list

To subscribe to the rival mailing list, send an email to rival-requests@recommenders.net with the subject "subscribe".
