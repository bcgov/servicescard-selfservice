# BCSC Sample Client
## What is this ?
A sample Java web application that demonstrates how a client could integrate with BC Services Card login services. You can build it, run the [BC Services Card Self-Service app](https://selfservice-prod.pathfinder.gov.bc.ca) to get a client id and secret, plug some values into a properties file and log into the application using a BC Services card virtual card (which you get from the app) 

This application is based on Spring Boot, Spring Security including OAuth2,OIDC and the Nimbus Jose JWT libraries and the Thymeleaf framework for MVC / UI templating.  

 Most of the parameters you need to integrate with the BC Services Card test environment are in the file application.properties.  
 
 ## Getting Started
 
 1. Build the Java app by running Maven on the pom.xml. The pom expects Java 1.8, but you can probably use higher versions too, just change the pom. 
 This will generate a file sample-client.jar in the target folder.
1.  Rename application.properties.sample to application.properties
 1. Run through the [BC Services Card Self-Service app](https://selfservice-prod.pathfinder.gov.bc.ca) to get your Client ID and Secret. The help details on the site will tell you what parameters you need to set. (NB: For now don't use encryption on the JWT ) 
 1. Plug the client ID and client secret returned in the app into the values

     ```spring.security.oauth2.client.registration.bcsc.client-id```
 ```spring.security.oauth2.client.registration.bcsc.client-secret```

 ## Running the sample app

 1. In a command /shell window navigate to the project root
 Run 

      ```java -jar ./target/sample-client.jar```

1. Run the sample app from a browser window by entering url 

   ```http://localhost:8080```

1. Login to BC Services Card with a Virtual Card test account which you will have obtained from the self-service application. You should get a "Successful Login " page 

1. You can see a list of all the attributes  returned from the BC Services Card test system for that test account by clicking on the plus sign 