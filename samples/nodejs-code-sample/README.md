# BC Services Card  OpenId Connect Authorization Code Flow Sample

The sample is an [Express.js](https://expressjs.com/) app that uses
[Passport.js](http://www.passportjs.org/) and the [Passport-OpenIdConnect](https://github.com/jaredhanson/passport-openidconnect)
module for managing user authentication.

The sample tries to keep everything as simple as possible so only
implements
* Login - redirecting users to BC Services Card for authentication
* Logout - destroying the local session and revoking the token at BC Services Card
* User Info - fetching profile information from BC Services Card

## Setup

Note that this is currently ONLY available to BC BPS (Broader Public Sector)  clients with either an IDIR or BC Services Card login (if you don't know what these are this app probably isn't for you :-) ) 

In order to run this sample you need to setup a BC Services Card integration project using the [BC Services Card Self-Service Application](https://sso-prod.pathfinder.gov.bc.ca/). When you work through this application you will be required to enter one or more redirect URLs, ensure that you add the following as one of them, it is used by this sample app :

http://127.0.0.1:3000/oauth/callback

When you have completed the registration process in the BC Services Card Self Service app, you will get a key, secret and some test accounts. You will need these plus the above redirect url to setup and test the sample code. 

Once you have the key, secret, redirect url and test accounts, you configure the sample app by :


1. Cloning this repo to your local desktop
2. Copying `.env.sample` to `.env` 
3. Updating  **OIDC_CLIENT_ID** and **OIDC_CLIENT_SECRET** in `.env` to the values you obtained from the BC Services Card Self Service app


## Run
This sample uses an express app running on nodejs.

From the command line run
```
> npm install
> npm start
```

### Local testing
By default these samples will run on `http://localhost:3000`.
