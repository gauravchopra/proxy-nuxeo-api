This project  is a created to put a rest api (proxy layer) in front of Nuxeo product. This is done to abstract the 
product usable API's and reduce complexity for downstream systems to consume and push the documents related data to the product.

Nuxeo is a product which stores documents, the API's provided in this project can be checked after running the server.

**Tech Stack used** - Spring boot, Java 11, docker, buildpacks

**Components:**

Gateway 
Service Registry
Proxy layer(having API's)


**Sequence of running:**
Registry server (by running Spring boot main class)
Proxy layer
Gateway

**How to run without containerization?**

* First run EurekaApplication class in proxy-registry - this will bring up service discovery components, eureka server can be accessed at http://localhost:8761/

* Then run ProxyLayerApplication class in proxy-layer module, this will bring up rest API's component,
  all the API's exposed can be seen at Swagger page url - http://localhost:8181/smart-doc/swagger-ui/index.html

* Then run GatewayProxyLayerApplication in gateway to access API's and eureka via gateway


Docker and buildpacks are used to containerize the application, proxy server and registry will be hidden when the docker
compose is brought up, all access will be via Gateway.

**Following Softwares are required to run this locally:**

Java 11


**For Containerization we need following:**

Docker Desktop – Docker will provide our pseudo-production environment. 
We'll use it to hide our services in a private network.

Cloud Native Buildpacks – We'll use Cloud Native Buildpacks to build Docker container images for us. 
Buildpacks embody several DevOps best practices, including hardened open-source operating systems and free to use OpenJDK distributions.