'use-strict'

const functions = require('firebase-functions');
const admin     = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.cloud_function = functions.firestore.document("Users/{user_id}/Notifications/{notification_id}").onWrite((change,context)=> {

    const user_id         = context.params.user_id;
    const notification_id = context.params.notification_id;
};