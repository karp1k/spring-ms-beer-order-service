# spring-ms-beer-order-service

check the project page! [*see*](https://github.com/users/karp1k/projects/1)

firstly, please download [beer-common](https://github.com/karp1k/spring-ms-beer-common) and do **clean-install** mvn goals

secondly, you will need to download the BOM project [ms-brewery-bom](https://github.com/karp1k/spring-ms-brewery-bom) 
and don't forget to **install** it to the local repository

Then, you will need to download [activemq-artemis](https://github.com/vromero/activemq-artemis-docker) broker to communicate services among themselves , after the MQ broker server up and running, set the correct credentials in the <i>application.properties</i>

tomcat port: 8081

related repo's:

- [beer-common](https://github.com/karp1k/spring-ms-beer-common)

- [beer-service](https://github.com/karp1k/spring-ms-beer-service)

- [beer-inventory-service](https://github.com/karp1k/spring-ms-beer-inventory-service)

- [beer-inventory-failover-service](https://github.com/karp1k/spring-ms-beer-inventory-failover-service)

- [brewery-bom](https://github.com/karp1k/spring-ms-brewery-bom)

- [brewery-eureka](https://github.com/karp1k/spring-ms-brewery-eureka)

- [brewery-gateway](https://github.com/karp1k/spring-ms-brewery-gateway)

- [brewery-config-server](https://github.com/karp1k/spring-ms-brewery-config-server)


some libs used in test scope:

- [awaitility](https://github.com/awaitility/awaitility)

- [wiremock](https://github.com/tomakehurst/wiremock)