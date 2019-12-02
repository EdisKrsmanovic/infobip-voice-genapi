
# infobip-voice-genapi

## About

Write a basic description of the project.

## Authors

Your individual and/or team names & e-mails go here.

## Usage

How to use it? APIs, GUIs, lib code snippets, etc.

## Infobip remoting library documentation

Remoting library documentation is available here: https://confluence.infobip.com/x/yX9nAQ


## Docker build

To build docker image locally and push it to remote docker registry `docker.ib-ci.com`:
```bash
$ mvn clean package -P dockerBuild -P dockerPush
```

To run docker container locally after building use:
```bash
$ docker run -it --rm -p 8080:8080 docker.ib-ci.com/infobip-voice-genapi-service:${version}
```

Check application is running:
```bash
$ curl localhost:8080/rmi/status

```

By convention app log files inside container are stored at `${SERVICE_LOG_ROOT}` folder location which defaults to `/var/log/app` if not defined otherwise.
Log files output.log and error.log are located there.
Note that DepoloymentManager sets up `SERVICE_LOG_ROOT` folder to be `/var/log/$SERVICE_NAME` when deploying via DM.


### How docker build works

Building of service docker image _is not_ enabled by default:
- activating maven profile 'dockerBuild' will build docker image locally
- activating 'dockerPush' profile will build and push docker image to docker.ib-ci.com
- to disable docker image creation regardless if profiles are active or not set `docker.skipBuild` property to `true`.
- service docker image will be created at `package` maven phase

More details aboud docker build process [can be found here](https://confluence.infobip.com/x/9odyAw). 

### Docker build customization

#### How to choose which java version is used to run dockerized application?

JVM version used to run the application is determined by base docker image used to build application docker image.

This is defined by `docker.fromImage` maven property in service pom.xml - set this property to base image of JVM runtime you need.

For example, for java 11 `docker.fromImage` property is set to `${docker.registry}/infobip-docker-java:centos-openjdk11-3.2.4`


For list of all available jvm base images checkout https://git.ib-ci.com/projects/CI/repos/infobip-docker-java/browse.

#### How to specify main class of application to run when container starts ?

If application contains only one class with main method it will be automatically detected.

In case you want to explicitly define main class of your application, i.e. in case you have multiple classes with main method) 
set `docker.mainClass` maven property in service pom.xml to your main class FQDN (note you need to use `/` and not dots), for example like this: 
`<docker.mainClass>com/infobip/myapplication/mypackage/AnotherMain</docker.mainClass>`

When container starts you can see in container logs which main class was used and if it was autodetected or not:
```bash
Resolving main class
MAIN_CLASS environment variable is null
Auto resolving main class...
Main class: com/infobip/myapplication/mypackage/Application
```


#### How to add any custom file to app docker image ?

By convention customization can be done by placing additional files in service project `docker` folder.
Contents of `docker` folder will be mapped to `/` inside docker image.

Example: say you wan to add file `readme.txt` inside image in `/etc/folder`:
- create folder `docker/etc` inside service project
- create readme.txt file and put it in `docker/etc/readme.txt`
- after image is build file readme.txt will be located at `/etc/readme.txt` inside the image


#### How to customize JVM startup parameters ?

By convention file `docker/etc/app/jvm.conf` is used to read any custom JVM parameters which will be used to run java app.

You can use bash substition to replace values with environment variables or defaults, i.e.:
- `${JVM_XMX}` will evaulate to JVM_XMX env var value or empty string if env var does not exist
- `${JMX_REMOTE_PORT:-11009}` will map to JMX_REMOTE_PORT env var value or '11009' if env var does not exist

Example `docker/etc/app/jvm.conf` which will be used to start the application:
```
-Xms${JVM_XMS}
-Xmx${JVM_XMX}
-XX:+CrashOnOutOfMemoryError
-XX:+UseG1GC
-XX:MaxGCPauseMillis=300
-Dapp.name="$SERVICE_NAME"
-Djava.rmi.server.hostname="$SERVER_NAME"
-Dcom.sun.management.jmxremote.port=${JMX_REMOTE_PORT:-11009}
-Dcom.sun.management.jmxremote.rmi.port=${JMX_REMOTE_PORT:-11009}
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.authenticate=false
-Dspring.profiles.active=${SERVICE_PROFILES_ACTIVE}
-Djava.net.preferIPv4Stack=true
-Dpython.security.respectJavaAccessibility=false
-Djava.security.krb5.conf=/etc/krb5.conf
-Djava.security.auth.login.config=/etc/app/SQLJDBCDriver.conf
${ADDITIONAL_JVM_PROPERTIES}
```


#### Where to add configuration files so DM placeholder replacement works ?

By convention all files in project folder `docker/etc/app` will be put in `/etc/app` inside image.
When application is started environment variable 'SERVICE_CONFIGURATION_ROOT' will contain path to this folder and you can use it in config files to reference this folder.

*Important:* Only files in `docker/etc/app` folder will be procesed to replace Deployment Manager placeholders and bash parameters.


