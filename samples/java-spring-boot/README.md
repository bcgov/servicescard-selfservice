# BCSC Sample Client
## What is this ?
A sample Java web application that demonstrates how a client could integrate with BC Services Card login services. You can build it, run the [BC Services Card Self-Service app](https://selfservice-prod.pathfinder.gov.bc.ca) to get a client id and secret, plug some values into a properties file and log into the application using a BC Services card virtual card (which you get from the app). Note that this is currently ONLY available to BC BPS (Broader Public Sector)  clients with either an IDIR or BC Services Card login (if you don't know what these are this app probably isn't for you :-) ) 

This application is based on Spring Boot, Spring Security including OAuth2,OIDC and the Nimbus Jose JWT libraries and the Thymeleaf framework for MVC / UI templating.  



 Most of the parameters you need to integrate with the BC Services Card test environment are in the file **application.properties**.  
 
 ## Getting Started
 
 1. Build the Java app by running Maven on the pom.xml. The pom expects Java 1.8, but you can probably use higher versions too, just change the pom. 
 This will generate a file sample-client.jar in the target folder.
1.  Rename **application.properties.sample** to **application.properties**
 1. Run through the [BC Services Card Self-Service app](https://selfservice-prod.pathfinder.gov.bc.ca) to get your Client ID and Secret. You can use the [help resource  on the site ](https://selfservice-prod.pathfinder.gov.bc.ca/help/help-dev)to give you more info about parameters you need to set, however there are some special values to use for this sample app : 

 
    - Redirect URI : You need to have "http://localhost:8080/login/oauth2/code/bcsc-test" as one of the options for the redirect or callback uri. (this is what this sample app uses) and it needs to be registered (you can have up to 10 values registered).
     
     - Secure JWT : It is recommended to set this ONLY after getting the sample app working as Signed JWT  (you can easily change it later in the self-service app). To facilitiate encryption of the returned JWT we have provided a JWKS which the identity provider uses to encrypt the returned JWT. This value must be entered into the JSON Web Key Set URL field
     "https://services-card-jwks-demo.pathfinder.gov.bc.ca/". Also because the jwks uses encryption algorithm RSA-OAEP this value must also be set in the Encryption Algorithm field.

 
 1. Plug the client ID and client secret returned in the app into the values

     ```spring.security.oauth2.client.registration.bcsc.client-id```
    ```spring.security.oauth2.client.registration.bcsc.client-secret```

    in the **application.properties** file

 ## Running the sample app

 1. In a command /shell window navigate to the project root
 Run 

      ```java -jar ./target/sample-client.jar```

1. Run the sample app from a browser window by entering url 

   ```http://localhost:8080```

1. Login to BC Services Card with a Virtual Card test account which you will have obtained from the self-service application. You should get a "Successful Login " page 

1. If successful, you will be redirected to the sample app in your browser and you can see a list of all the attributes  returned from the BC Services Card test system for that test account by clicking on the plus sign 