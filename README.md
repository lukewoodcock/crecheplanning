# crocos

## Goal
The aim of this application is to provide a tool that will help with the organisation and more specifically the distribution of 'shifts' between resources. In the context of Les Crocos this means distributing 'guards' among families.

### Simple example
If in one week there are 5 days, each day has 3 shifts of equal duration, and there are 3 resources, the assignment of shifts between resources should be such that each resource has 5 shifts

## Getting started
This is an scala [play](https://www.playframework.com/) application and using java 1.8

Install sbt (scala build tool) to build and run the application. You can find documentation [here](https://www.scala-sbt.org/). For more info [this](https://alvinalexander.com/scala/sbt-how-to-compile-run-package-scala-project) is useful

I'm using TDD so once you've installed sbt and cloned the project, go to the root folder of the repo and run `sbt test`

If you want to run just one test suite you can run `sbt "testOnly MyTestName"`

## Project status
* Using sbt test to build API

I was not expecting to show this to anyone so I've not had a chance to tidy the dev
* Lots of commented code
* Some overlap in tests

### TODO
* Read write csv
* Define shifts (guards) used by Les Crocos
* Parameterize weekly requirements
* API for adding restrictions
* Routes
* Front
* And I'm sure there's more