# FORK/HACK for SonarQube tokens
The original plugin doesn't work with SonarQube tokens. This "fixes" it by hacking everything around to use GET requests with tokens instead of a POST login request.
This shouldn't be used in production. It just shows that it is possible and should be properly integrated into the original plugin.

# Sonar Quality Gates Plugin
Jenkins plugin that fails the build if the predefined sonar quality gates are not green.

#### Sonarqube suportted versions

* Sonar 5.6
* Sonar 6.x+

#### Usage 

1. In `Manage Jenkins -> Configure System -> Quality Gates - Sonarqube` add yours sonar configuration.

![Plugin Configuration](docs/img/sonar-config.png)

2. In jenkins job add a `Post-build Actions -> Quality Gates Sonarqube Plugin` and set the sonar instance, if you have multiple sonar configurations, and `Project key` .

![Job Configuration](docs/img/post-build.png)

_______
###### _Forked from https://github.com/jenkinsci/quality-gates-plugin_
