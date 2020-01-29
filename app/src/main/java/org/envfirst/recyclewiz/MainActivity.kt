package org.envfirst.recyclewiz

import android.graphics.BitmapFactory
import android.os.Bundle
import android.system.Os
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.bassaer.chatmessageview.model.ChatUser
import com.github.bassaer.chatmessageview.model.Message
import com.google.cloud.dialogflow.v2.QueryInput
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.SessionsClient
import com.google.cloud.dialogflow.v2.TextInput
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.util.UUID

class MainActivity : AppCompatActivity() {

    var sessionsClient: SessionsClient? = null
    var session: SessionName? = null

    //ML Kits code for image labeling
    fun predict(v: View) {
        Log.i("Info", "I am testing the android")
        val options =
            FirebaseVisionCloudImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.4f)
                .build()

        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.banada);
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val detector = FirebaseVision.getInstance().getOnDeviceImageLabeler()

        detector.processImage(image)
            .addOnSuccessListener {
                Log.e("Start Logging", "starting")
                for (i in it) {
                    Log.i("Label Name", i.text + i.confidence.toString())
                    Toast.makeText(this, i.text + i.confidence.toString(), Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {

                Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            }
    }

    //detect Intent from user input
    @Throws(Exception::class)
    fun detectIntentTexts(clientText: String) {
        doAsync {
            val languageCode = "en-US"
            if (sessionsClient == null) {
                //Hacking for allow call cloud api from andorid
                var sessionId: String = UUID.randomUUID().toString()
                var res = resources.openRawResource(R.raw.key)
                val inputString = res.bufferedReader().use { it.readText() }
                File(applicationContext.filesDir, "key.json").writeText(inputString)
                Os.setenv(
                    "GOOGLE_APPLICATION_CREDENTIALS",
                    applicationContext.filesDir.toString() + "/key.json",
                    true
                )
                sessionsClient = SessionsClient.create()
                session = SessionName.of(getString(R.string.project_id), sessionId)
            }

            val textInput = TextInput.newBuilder().setText(clientText).setLanguageCode(languageCode)
            val queryInput: QueryInput = QueryInput.newBuilder().setText(textInput).build()
            val response = sessionsClient?.detectIntent(session, queryInput)
            val queryResult = response?.queryResult
            System.out.println("====================");
            System.out.format("Query Text: '%s'\n", queryResult?.getQueryText());
            var result: String = ""
            if (queryResult != null) {
                var intent = queryResult.getIntent().getDisplayName()
                if (intent.equals("weight", true)) {
                    var target = queryResult.parameters.fields["unit-weight-name"]?.stringValue
                    var source =
                        queryResult.parameters.fields["unit-weight"]?.structValue?.fields?.get("unit")
                            ?.stringValue
                    var amount =
                        queryResult.parameters.getFieldsOrThrow("unit-weight")?.structValue?.getFieldsOrThrow("amount")
                            ?.numberValue.toString()
                    var weightConvert = WeightConvert()
                    //test queryResult.parameters.getFieldsOrDefault("unit-weight-name","")
                    result = weightConvert.convert(source!!, target!!, amount!!)
                } else {
                    result = queryResult?.getFulfillmentText()
                }
            };


            uiThread {
                showAgentResponse(result)
            }
        }


    }

    fun showAgentResponse(text: String) {
        var agent = ChatUser(
            2, "Agent",
            BitmapFactory.decodeResource(
                resources,
                R.drawable.ic_account_circle
            )
        )
        my_chat_view.send(
            Message.Builder()
                .setRight(true)
                .setUser(agent)
                .setText(text)
                .build()
        )
        my_chat_view.inputText = ""
    }

    fun waitUserSendMessage() {
        var human = ChatUser(
            1, "Client",
            BitmapFactory.decodeResource(
                resources,
                R.drawable.ic_account_circle
            )
        )
        my_chat_view.setOnClickSendButtonListener(
            View.OnClickListener {
                my_chat_view.send(
                    Message.Builder()
                        .setUser(human)
                        .setText(my_chat_view.inputText)
                        .build()
                )
                detectIntentTexts(my_chat_view.inputText)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        waitUserSendMessage()
    }
}
