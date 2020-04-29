var request = require('request');
var express = require('express');
var router = express.Router();
var jwt = require ('jsonwebtoken');
var jwksClient=require ('jwks-rsa');
var client=jwksClient({
  jwksUri: 'https://idtest.gov.bc.ca/oauth2/jwk'
});

function getKey(header, callback){
  client.getSigningKey(header.kid, function(err, key) {
    var signingKey = key.publicKey || key.rsaPublicKey;
    console.log ("Signing key : " + signingKey)
    callback(null, signingKey);
  });
}


/*
  ALL OF THE ROUTES IN THIS PAGE REQUIRE AN AUTHENTICATED USER
*/

/* GET users listing. */
router.get('/', function(req, res, next) {

  console.log(req.user)

  res.render('users', {
    title: 'Users',
    user: req.user
  });
});

function render (res, userinfo){
  console.log ("Userinfo :" + userinfo );
  res.render('profile', {
    title: 'Profile',
    user: userinfo 
  });

}

/* GET the profile of the current authenticated user */
router.get('/profile', function(req, res, next) {
  oidc_client.userinfo (req.session.accessToken)
  .then ( (userinfo)=>{ render (res,userinfo)}  )
  .catch((err) => {console.log("Error getting userinfo", err);}); 

  /*request.get(
    'https://idtest.gov.bc.ca/oauth2/userinfo',   // For EU instances use https://openid-connect-eu.onelogin.com/oidc/me
    {
    'auth': {
      'bearer': req.session.accessToken
    }
  } ,function(err, response, body){
    console.log('User Info')
    console.log("Raw :" + body);
    try {
      var userinfo=JSON.parse (body);
      console.log ("Looks like JSON return" );
      render (res, userinfo);
    }
    catch (e){
      jwt.verify (body ,getKey,{json : true, complete: true},function (err, decoded){
        console.log ("Decoded JWT" ) ;
        console.log (decoded.header);
        render(res,decoded.payload);

    })

    } 
   // var decoded=jwt.decode(body,{json : true, complete: true});
    

  });*/
});

module.exports = router;
