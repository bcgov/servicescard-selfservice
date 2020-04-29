require('dotenv').config();

var request = require('request');
var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var session = require('express-session');

// Use Passport with OpenId Connect strategy to
// authenticate users with OneLogin
var passport = require('passport')
//var BCSCStrategy = require('passport-openidconnect').Strategy

var index = require('./routes/index');
var users = require('./routes/users');

// requirements for openid-client, as well as reading in the key we generated
const fs = require('fs'); // used to read in the key file
const jose = require('node-jose'); // used to parse the keystore
const Issuer = require('openid-client').Issuer;
const Strategy = require('openid-client').Strategy;

// filename for the keys
const jwk_file = "./jwks-private.json";

// OpenID Connect provider url for discovery
const oidc_discover_url = "https://idtest.gov.bc.ca/login/.well-known/openid-configuration";

// Params for creating the oidc client
const client_params = {
        client_id: process.env.OIDC_CLIENT_ID,
        client_secret : process.env.OIDC_CLIENT_SECRET,
        //id_token_encrypted_response_alg : "RSA1_5",
        id_token_encrypted_response_alg : "RS256",
        id_token_encrypted_response_enc: "A256GCM",
        userinfo_encrypted_response_alg:"RS256",
        userinfo_encrypted_response_enc: "A256GCM"

};

// Params for the OIDC Passport Strategy
const strat_params = {
        redirect_uri: process.env.OIDC_REDIRECT_URI,
        scope: 'openid profile email phone address',
        response: ['userinfo']
};



var  oidc_client=null;
// load keystore and set up openid-client
var jwk_json = JSON.stringify(JSON.parse(fs.readFileSync(jwk_file, "utf-8")));
// load the keystore. returns a Promise
console.log('jwk_json', jwk_json);
let load_keystore =  new jose.JWK.asKeyStore(jwk_json);

// discover the Issuer. returns a Promise
//   you can also set this up manually, see the project documentation
let discover_issuer = Issuer.discover(oidc_discover_url);

// Create a client, and use it set up a Strategy for passport to use
// since we need both the Issuer and the keystore, we'll use Promise.all()
Promise.all([load_keystore, discover_issuer])
        .then(([ks, myIssuer]) => {
                console.log("Found Issuer: ", myIssuer);
                console.log('jwks', ks.toJSON(true));

                oidc_client = new myIssuer.Client(client_params, ks.toJSON(true));
                //const oidc_client = new myIssuer.Client(client_params, ks);
                //console.log("Created client: ", oidc_client);

                // create a strategy along with the function that processes the results
                passport.use('oidc', new Strategy({client: oidc_client, params: strat_params, passReqToCallback :true }, (req, tokenset, userinfo, done) => {
                  // we're just loging out the tokens. Don't do this in production
                  console.log('tokenset', tokenset);
                  //console.log('access_token',tokenset.access_token );
                  //console.log('id_token', tokenset.id_token);
                  //console.log('claims', tokenset.claims);
                  console.log('userinfo', userinfo);
                  //console.log('Req :', req);

                  req.session.accessToken = tokenset.access_token;

                  // to do anything, we need to return a user. 
                  // if you are storing information in your application this would use some middlewhere and a database
                  // the call would typically look like
                  // User.findOrCreate(userinfo.sub, userinfo, (err, user) => { done(err, user); });
                  // we'll just pass along the userinfo object as a simple 'user' object
                  return done(null, userinfo);
          }));
  }) // close off the .then()
  .catch((err) => {console.log("Error in OIDC setup", err);});


//  acr_values: 'onelogin:nist:level:1:re-auth'

// Configure the OpenId Connect Strategy
// with credentials obtained from BCSC self service app
/* passport.use(new BCSCStrategy({
  issuer: process.env.OIDC_BASE_URI,
  clientID: process.env.OIDC_CLIENT_ID,
  clientSecret: process.env.OIDC_CLIENT_SECRET,
  authorizationURL: `https://idtest.gov.bc.ca/login/oidc/authorize/`,
  userInfoURL: `${process.env.OIDC_BASE_URI}userinfo`,
  tokenURL: `${process.env.OIDC_BASE_URI}token`,
  callbackURL: process.env.OIDC_REDIRECT_URI,
  passReqToCallback: true
},
function(req, issuer, userId, profile, accessToken, refreshToken, params, cb) {

  console.log('issuer:', issuer);
  console.log('userId:', userId);
  console.log('accessToken:', accessToken);
  console.log('refreshToken:', refreshToken);
  console.log('profile:', profile);
  console.log('params:', params);
 // console.log('cb:', cb);

  req.session.accessToken = accessToken;

  return cb(null, profile);
})); */

passport.serializeUser(function(user, done) {
  console.log ("In serialize user" + user );
  done(null, user);
});

passport.deserializeUser(function(obj, done) {
  console.log ("In de-serialize user" + obj );
  done(null, obj);
});

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

// Passport requires session to persist the authentication
// so were using express-session for this example
app.use(session({
  secret: 'secret squirrel',
  resave: false,
  saveUninitialized: true
}))

// Initialize Passport
app.use(passport.initialize());
app.use(passport.session());

// Middleware for checking if a user has been authenticated
// via Passport and BCSC  OpenId Connect
function checkAuthentication(req,res,next){
  if(req.isAuthenticated()){
      next();
  } else{
      res.redirect("/");
  }
}

app.use('/', index);
// Only allow authenticated users to access the /users route
app.use('/users', checkAuthentication, users);

// Initiates an authentication request with BCSC
// The user will be redirect to BCSC and once authenticated
// they will be returned to the callback handler below
app.get('/login', passport.authenticate('oidc', {
  successReturnToOrRedirect: "/",
  scope: 'openid profile address email'
}));

// Callback handler that BCSC will redirect back to
// after successfully authenticating the user
app.get('/oauth/callback', passport.authenticate('oidc', 
  {successRedirect:'/profile', failureRedirect:'/'}));


// Destroy both the local session and
// revoke the access_token at BCSC
app.get('/logout', function(req, res){

  request.post(`"https://idtest.gov.bc.ca/oauth2/revoke"`, {
    'form':{
      'client_id': process.env.OIDC_CLIENT_ID,
      'client_secret': process.env.OIDC_CLIENT_SECRET,
      'token': req.session.accessToken,
      'token_type_hint': 'access_token'
    }
  },function(err, respose, body){

    console.log('Session Revoked at BCSC');
    res.redirect('/');

  });
});

app.get('/profile', function (req,res) {
  oidc_client.userinfo (req.session.accessToken)
  .then ( (userinfo)=>{res.render('profile', { title: 'Profile', user: userinfo } )}   )
  .catch((err) => {console.log("Error getting userinfo", err)})
});


// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
