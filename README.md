# Sonar Quality Gates Plugin
Jenkins plugin that fails the build if the predefined sonar quality gates are not green.

#### Sonarqube supported versions

* Sonar 5.6
* Sonar 6.x+

#### Usage 

1. In `Manage Jenkins -> Configure System -> Quality Gates - Sonarqube` add yours sonar configuration.
    * If token is specified, the parameters account login and account password will be ignored.

    ![Plugin Configuration](docs/img/01-sonar-config.PNG)

2. In jenkins job add a `Post-build Actions -> Quality Gates Sonarqube Plugin` and set the sonar instance, if you have multiple sonar configurations, and `Project key` .

    ![Job Configuration](docs/img/02-post-build.png)

#### Issues

- [Issues](https://issues.jenkins-ci.org/issues/?jql=project%20%3D%20JENKINS%20AND%20status%20in%20(Open%2C%20%22In%20Progress%22%2C%20Reopened%2C%20%22In%20Review%22)%20AND%20component%20%3D%20sonar-quality-gates-plugin)
_______
###### _Forked from https://github.com/jenkinsci/quality-gates-plugin_
