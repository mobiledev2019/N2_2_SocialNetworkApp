'use-strict'

const functions = require('firebase-functions');
const admin=require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendAddedAnswerNotification=functions.firestore.document("Questions/{question_id}/Answers/{answer_id}").onWrite((change,context)=> {

    const question_id=context.params.question_id;
    const answer_id=context.params.answer_id;
  
  console.log(question_id+":"+answer_id);


return admin.firestore().collection("Questions").doc(question_id).get().then((questionqueryResult)=>{

        const question_owner_id=questionqueryResult.data().id;
        const question=questionqueryResult.data().question;
        const question_timestamp=questionqueryResult.data().timestamp;

  return admin.firestore().collection("Questions").doc(question_id).collection("Answers").doc(answer_id).get().then((answerqueryResult)=>{
    
        const answered_user_id=answerqueryResult.data().user_id;
        const answer=answerqueryResult.data().answer;
        const is_answer=answerqueryResult.data().is_answer;
        const timestamp=answerqueryResult.data().timestamp;

        const question_owner_data=admin.firestore().collection("Users").doc(question_owner_id).get();
        const answer_owner_data=admin.firestore().collection("Users").doc(answered_user_id).get();

        return Promise.all([question_owner_data,answer_owner_data]).then(result=>{

            const question_poster_name=result[0].data().name;
            const question_poster_token=result[0].data().token_id;
            const question_poster_image=result[0].data().image;
            const answer_poster_name=result[1].data().name;
            const answer_poster_token=result[1].data().token_id;
            const answer_poster_image=result[1].data().image;

           if(answer_poster_token!=question_poster_token)
           {
             
               
             if(is_answer=="yes"){
               
             const payload={
               data:{
                 question_timestamp:question_timestamp,
                 channel:"Forum",
                timestamp:timestamp,
                question_id:question_id,
                title:"Forum",
                from_image:question_poster_image,
                body:question_poster_name+" marked your answer correct for the question \""+question+"\"",
                click_action:"com.tad.asannet.TARGET_FORUM"
                 
              }
                
            };
            
            return admin.messaging().sendToDevice(answer_poster_token,payload).then(result=>{

                console.log("Notification Sent.");

            });
               
             }else{
               
               const payload={
               data:{
                  question_timestamp:question_timestamp,
                 channel:"Forum",
                timestamp:timestamp,
                question_id:question_id,
                title:"Forum",
                from_image:answer_poster_image,
                body:answer_poster_name+" answered to your question \""+question+"\"",
                click_action:"com.tad.asannet.TARGET_FORUM"
              }
                
            };
            
            return admin.messaging().sendToDevice(question_poster_token,payload).then(result=>{

                console.log("Notification Sent.");

            });
               
             }
               
            }
                        
          });
        });
    });

});

