# Spring Cloud Config
The introduction of the cloud infrastructures has changed the way applications are deployed. No longer is an application assigned to a specific machine or a virtual machine. The location of a application is dynamically choosen by available resources within a cluster of machines.   
 
The dynamic nature of the deployment makes configuring the application difficult. No longer will it suffice to copy configuration files around or mount volumes with configuration files. 

Spring Cloud Config introduces an idea of a configuration server and client to help solve the problem of serving configuration data. The Spring Config Server is a simple Spring Boot application that provides configuration properties to Spring Config Clients via HTTP (key-value pairs). 

The server uses a pattern for serving application properties. The application name and profile are used to as part of a REST URL to access properties (Ex: `http://localhost:8888/$application/$profile`).

![](spring-cloud-config.png)

The server can provide configuration data from a filesystem or Git. This demo focus on using the filesystem to manage configuration data because it is easier to package up into a demo. However, applications intended for production should use Git. Git will allow the configuration data to be managed like source code. 

## Introduce a Spring Boot app

Let's introduce a Spring Boot application with a endpoint that provides a greeting. 

```package com.rseanking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ClientApplication {
	@Value("${hello.greeting:Hello}")
	private String greeting;
	
	@RequestMapping("/hello")
	@ResponseBody
	public String greeting() {
		return greeting;
	}

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}
}
```
Since the `${hello.greeting}` isn't provided the `/hello` endpoint will default to saying 'Hello' .

```
$ curl -XGET -s http://localhost:8080/hello -w '\n'
Hello
```

## Building a Configuration Server

Let's introduce a configuration server to support our new application. The configuration server will provide a different greeting for each profile.

Introduce the `spring-cloud-config-server` dependency to provide the libraries needed to quickly spin up a cloud configuration server.

```xml
<dependencies>
    ... 
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-config-server</artifactId>
    </dependency>
    ... 
</dependencies>
```

Implementation of the server couldn't be much simpler. Annotation a Spring Boot application with the `@EnableConfigServer` annotation.

```java
package com.rseanking.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
```
Configure the application

Let's update the configuration to serve configuration properties from the `configRepo` directory.   

```
server.port: 8888
spring.config.name=configserver

# Required to read from local filesytem
spring.profiles.active=native 
# Repository of configuration properties
spring.cloud.config.server.native.search-locations: classpath:configRepo/ 
```
Let's introduce configuration data for our `spring-cloud-config-client` application into `src/main/resources/configRepo`. 

```
$ echo 'hello.greeting=Hello!' > src/main/resources/configRep/pring-cloud-config-client.properties
$ echo 'hello.greeting=Hello Dev!' > src/main/resources/configRep/pring-cloud-config-client-dev.properties
```

The dev configuration for `spring-cloud-config-client` can now be accessed via `http://localhost:8888/spring-cloud-config-client/dev`. The results will provide the configuration for the default profile and the properties being overridden by the dev profile.

```
$ curl -XGET -s http://localhost:8888/spring-cloud-config-client/dev | jq
{
  "name": "spring-cloud-config-client",
  "profiles": [
    "dev"
  ],
  "label": null,
  "version": null,
  "state": null,
  "propertySources": [
    {
      "name": "classpath:configRepo/spring-cloud-config-client-dev.properties",
      "source": {
        "hello.greeting": "Hello Dev!"
      }
    },
    {
      "name": "classpath:configRepo/spring-cloud-config-client.properties",
      "source": {
        "hello.greeting": "Hello!"
      }
    }
  ]
}
```

## Update the Greeting Service

Introduce the spring The greeting service can now be updated to utilize the configuration server. 

Introduce the `spring-cloud-config-client` dependency to provide the libraries needed to update our greeting service to communicate with the configuration server.

```xml
<dependencies>
    ...
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    ...
</dependencies>
```

Introduce a `src/main/resources/bootstrap.properties`. The `bootstrap.properties` file will configure the client to communicate with the configuration server. 

```
spring.profiles.active=dev
spring.application.name=spring-cloud-config-client
spring.cloud.config.uri=http://localhost:8888
# Disable security for demo
management.security.enabled=false
```
The `spring.application.name` and `spring.profiles.active` are important as they will be the application name and profile used to build a URL to acquire the application properties from the configuration server. The `spring.cloud.config.uri` identifies the location of the configuration server.

Running the greeting service with an active profile of `dev` will produce `Hello Dev!`. 

```
$ curl -XGET -s http://localhost:8080/hello -w '\n'
Hello Dev!
```

Running the greeting service without an active profile will produce `Hello!`, because it will use the `default` profile.

```
$ curl -XGET -s http://localhost:8080/hello -w '\n'
Hello!
```