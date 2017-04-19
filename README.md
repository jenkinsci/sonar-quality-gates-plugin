[![Build Status](https://travis-ci.org/arkanjoms/qa-plugin-sonar.svg?branch=master)](https://travis-ci.org/arkanjoms/qa-plugin-sonar)

# Quality Gates Plugin - Sonarqube 6.1+
Jenkins plugin that fails the build if the predefined sonar quality gates are not green.


#### Main difference from fork

* Expects completion of background task to check quality gate status.
* Adding parameters to limit the number of repetitions while waiting for the quality gate check.


Fork from https://github.com/jenkinsci/quality-gates-plugin
