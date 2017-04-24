[![Build Status](https://travis-ci.org/arkanjoms/sonar-quality-gates.svg?branch=master)](https://travis-ci.org/arkanjoms/sonar-quality-gates)

# Sonar Quality Gates Plugin
Jenkins plugin that fails the build if the predefined sonar quality gates are not green.

#### Sonarqube suportted versions

* Sonar 5.6
* Sonar 6.x+

#### Main difference from fork

* Expects completion of background task to check quality gate status.
* Adding parameters to limit the number of repetitions while waiting for the quality gate check.


Fork from https://github.com/jenkinsci/quality-gates-plugin
