package quartifex.com.ttstest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;

import quartifex.com.ttstest.imagecapture.PermissionsDelegate;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener{


	Button submitButton;
	EditText inputText;

	TextToSpeech mTTS = null;
	private final int ACTION_CHECK_TTS_EXISTS = 1000;

	private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		submitButton=findViewById(R.id.button_submit);
		inputText=findViewById(R.id.text_input);

		boolean hasCameraPermission=permissionsDelegate.hasCameraPermission();

		Intent ttsIntent=new Intent();
		ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(ttsIntent,ACTION_CHECK_TTS_EXISTS);

		if (!hasCameraPermission) {
			permissionsDelegate.requestCameraPermission();
		}


		//TODO:
		/**
		 *
		 * Add to QUEUE, async labelling so we send images one by one
		 * and as we get it we add it to the queue and keep playing as we get it
		 * rather than using flush
		 *
		 *
		 * Public webcams, maybe placed at particular locations
		 * you can ask, describe it to me and it tells you what it sees
		 * can be like a good iot skill
		 */

		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				speakOutText(inputText.getText().toString().trim(),1);
			}
		});

	}


	private void speakOutText(String text,int qMode){

		if(qMode==1){

			mTTS.speak(text,TextToSpeech.QUEUE_ADD,null,null);
		}
		else {
			mTTS.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode==ACTION_CHECK_TTS_EXISTS){


			if(resultCode==TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
				mTTS=new TextToSpeech(this,this);
			}else{
				//Not exists thus we must get and install
				Intent installIntent=new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}


	@Override
	public void onInit(int status) {

		if(status==TextToSpeech.SUCCESS){

			if(mTTS!=null){

				//Modify here to download locale specific
				//current locale is US language
				int result=mTTS.setLanguage(Locale.US);
				if(result==TextToSpeech.LANG_MISSING_DATA||
						result==TextToSpeech.LANG_NOT_SUPPORTED){

					//Vibrate since blind cant see toast
				}

			}
			else {

				//Vibrate here
			}

		}
	}


	@Override
	protected void onDestroy() {

		if(mTTS!=null){
			mTTS.stop();
			mTTS.shutdown();
		}
		super.onDestroy();
	}
}
