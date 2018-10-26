package quartifex.com.ttstest.imagecapture;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.error.CameraErrorListener;
import io.fotoapparat.exception.camera.CameraException;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import io.fotoapparat.view.CameraView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import quartifex.com.ttstest.R;
import quartifex.com.ttstest.imagecapture.network.csarch.ApiUtils;
import quartifex.com.ttstest.imagecapture.network.datamodels.CustomObject;
import quartifex.com.ttstest.imagecapture.network.datamodels.Emotion;
import quartifex.com.ttstest.imagecapture.network.datamodels.ResponseObject;
import quartifex.com.ttstest.imagecapture.network.csarch.ResponseService;
import quartifex.com.ttstest.imagecapture.network.datamodels.ResponseObject2;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static io.fotoapparat.result.transformer.ResolutionTransformersKt.scaled;
import static io.fotoapparat.selector.AspectRatioSelectorsKt.standardRatio;
import static io.fotoapparat.selector.FlashSelectorsKt.autoFlash;
import static io.fotoapparat.selector.FocusModeSelectorsKt.autoFocus;
import static io.fotoapparat.selector.FocusModeSelectorsKt.continuousFocusPicture;
import static io.fotoapparat.selector.FocusModeSelectorsKt.fixed;
import static io.fotoapparat.selector.JpegQualitySelectorsKt.highestQuality;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;
import static io.fotoapparat.selector.PreviewFpsRangeSelectorsKt.highestFps;
import static io.fotoapparat.selector.ResolutionSelectorsKt.highestResolution;
import static io.fotoapparat.selector.SelectorsKt.firstAvailable;
import static io.fotoapparat.selector.SensorSensitivitySelectorsKt.highestSensorSensitivity;
import android.app.VoiceInteractor;
import android.app.VoiceInteractor.PickOptionRequest;
import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class ImageCapture extends Activity implements TextToSpeech.OnInitListener{



	private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
	private boolean hasCameraPermission;
	private CameraView cameraView;
	private View capture;

	private String KEY_CAPTION="KEY_CAPTION";
	private String KEY_LABELS="KEY_LABELS";
	private String KEY_TOUCH="KEY_TOUCH";
	private String KEY_EMOTION="KEY_EMOTION";


	TextToSpeech mTTS = null;
	private final int ACTION_CHECK_TTS_EXISTS = 1000;

	private Fotoapparat fotoapparat;

	private static final String LOG_TAG = "FotoappartExample";

	private static final int CAPTION_CODE =1;
	private static final int LABEL_CODE =2;
	private static final int TOUCH_CODE =3;
	private static final int RETAKE_CODE=4;
	private static final int EXIT_CODE=5;

	//TODO:
	//click a picture after a few seconds and then send the result to ther server automatically

	private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");


	private String[] photoAlts={
			"picture","photo","pic","snap","image"
	};

	private String[] objectAlts={
			"things","objects","stuff"
	};


	private Map<String,Object> resultStore=new HashMap<>();

	private String[] captionOptions = {
			"What is the <PALT> about?",
			"Can you tell me what's the <PALT> about?",
			"What's the <PALT> about?",
			"What's the <PALT> about?",
			"What's the <PALT> say?",
			"What's the <PALT> about?"
	};


	private String[] labelOptions={
			"What do you see in the <PALT>?",
			"What <OALT> do you seen in <PALT>?",
			"What are the <OALT> that you see in <PALT>?",
			"What kind of <OALT> can you see in the <PALT>?",
			"What <OALT> can you see in the <PALT>?",
			"What <OALT> do you see in the <PALT>?"
	};

	private String[] touchOptions ={
			"What's this?",
			"Whats that?",
			"And this?",
			"How about this?"
	};

	private String[] retakeOptions = {
			"retake image",
			"take a photo",
			"tell me what you see now"

	};


	private String[] exitOptions = {
			"Exit"
	};



	Option captions = new Option(captionOptions[0], CAPTION_CODE);
	{
		for(int i=1;i<captionOptions.length;i++){
			for (String photoAlt : photoAlts) {
				Log.d(LOG_TAG, captionOptions[i].replace("<PALT>", photoAlt));
				captions.addSynonym(captionOptions[i].replace("<PALT>", photoAlt));
			}
		}
	}

	Option labels = new Option(labelOptions[0], LABEL_CODE);
	{
		for(int i=1;i<labelOptions.length;i++){
			for (String objectAlt : objectAlts) {
				for(String photoAlt:photoAlts){
					Log.d(LOG_TAG, labelOptions[i].replace("<OALT>", objectAlt).replace("<PALT>",photoAlt));
					labels.addSynonym(labelOptions[i].replace("<OALT>", objectAlt).replace("<PALT>",photoAlt));
				}

			}
		}
	}

	Option touches = new Option(touchOptions[0],TOUCH_CODE);
	{
		for(int i=1;i<touchOptions.length;i++){
			touches.addSynonym(touchOptions[i]);
		}
	}

	Option retakes = new Option(retakeOptions[0],RETAKE_CODE);
	{
		for(int i=1;i<retakeOptions.length;i++){
			retakes.addSynonym(retakeOptions[i]);
		}
	}

	Option exits = new Option(exitOptions[0],EXIT_CODE);

	private String PROMPT_DONE = "Done";

	private String PROMPT_CAPTION =" You can ask me \"what the image is about\"";
	private String PROMPT_LABEL =" You can ask me about the \"things that I can see in the image\"";
	private String PROMPT_TOUCH =" You can touch the screen and explore the image by asking me \"whats this?\"";
	private String PROMPT_RETAKE=" You can say \"retake image\" to click another image";
	private String PROMPT_EXIT=" You can also simply say \"exit\" to close the app";
	private String EMPTY_PROMPT="";

	private ImageView resultView;

	private Map<LocationObject,String> location2ObjectMap=new HashMap<>();

	private ResponseService mResponseService;

	boolean isVerticalFlag=false;


	/**
	 *
	 * Activity Lifecycle Methods
	 *
	 */

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_capture);
		isVerticalFlag=false;

		mResponseService= ApiUtils.getResponseService();

		cameraView = findViewById(R.id.cameraView);
		resultView=findViewById(R.id.result);
		hasCameraPermission = permissionsDelegate.hasCameraPermission();

		if(mTTS==null){
			mTTS=new TextToSpeech(this,this);
		}

		if (hasCameraPermission) {
			cameraView.setVisibility(View.VISIBLE);
		} else {
			permissionsDelegate.requestCameraPermission();
		}

		fotoapparat = createFotoapparat();





	}

	@Override
	protected void onStart() {
		super.onStart();
		if (hasCameraPermission) {
			fotoapparat.start();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(isVoiceInteraction()){
			//call this once you get response from the server
			if(mTTS==null){
				mTTS=new TextToSpeech(this,this);
			}
			isVerticalFlag=false;
			takePicture();
		}
	}


	@Override
	protected void onStop() {

		if (hasCameraPermission) {
			fotoapparat.stop();
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {

		if(mTTS!=null){
			mTTS.stop();
			mTTS.shutdown();
		}
		super.onDestroy();
	}
	/**
	 *
	 * End of Activity Lifecycle Methods
	 *
	 */




	private String LOG_LABELS="LogLabels";
	/**
	 *
	 * General Helper Methods
	 *
	 */

	private void populateMap(ResponseObject result, boolean isVertical){
		List<Emotion> emotionList;
		List<CustomObject>objectList;

//		Log.d("ObjectList",result.toString());

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Point size = new Point();
		wm.getDefaultDisplay().getRealSize(size);
		int width = size.x, height = size.y;
		if(isVertical){
			width=size.y;
			height=size.x;
		}

		Set<String> labelSet=new HashSet<>();
		try{
			if(result!=null && result.getEmotion()!=null){
				emotionList = result.getEmotion();
			}
			if(result!=null && result.getObject()!=null) {
				objectList = result.getObject();
				for(CustomObject customObject:objectList){
					LocationObject locationObject=new LocationObject(
							(int)(customObject.getTopx()*width),
							(int)(customObject.getTopy()*height),
							(int)(customObject.getBottomx()*width),
							(int)(customObject.getBottomy()*height),
							customObject.getLabel());
					location2ObjectMap.put(locationObject,customObject.getLabel());
					labelSet.add(customObject.getLabel());
					Log.d(LOG_LABELS,""+locationObject);


				}
				resultStore.put(KEY_LABELS,labelSet);

			}
		}catch (NullPointerException e){
			e.printStackTrace();
		}







	}

	private void populateMap2(ResponseObject2 result){

		if(result!=null){
			resultStore.put(KEY_CAPTION,result.getCaption());
		}

	}


	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
			hasCameraPermission = true;
			fotoapparat.start();
			cameraView.setVisibility(View.VISIBLE);
		}
	}
	/**
	 *
	 * End of Genreal helper methods
	 */




	/**
	 *
	 * Feature Methods
	 *
	 */

	private void startVoiceTrigger() {


		String INTRO_PROMPT=
				PROMPT_DONE;

		VoiceInteractor.Prompt introPrompt = new VoiceInteractor.Prompt(INTRO_PROMPT);
		getVoiceInteractor().submitRequest(new PickOptionRequest(introPrompt, new Option[]{captions,labels,touches,retakes,exits}, null) {
					@Override
					public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
						if (finished && selections.length == 1) {
							Message message = Message.obtain();
							message.obj = result;
							if(selections[0].getIndex()==0){
								caption();
							}
							else if(selections[0].getIndex()==1){
								label();
							}
							else if(selections[0].getIndex()==2){
								touch();
							}
							else if(selections[0].getIndex()==3){
								retake();
							}
							else if(selections[0].getIndex()==4){
								exit();
							}
							else {
								finish();
							}
						}
					}
				});
	}//option 0

	private void caption(){

		String prefix="This image is about ";
		speakOutText(prefix+resultStore.get(KEY_CAPTION),0);
		String INTRO_PROMPT= PROMPT_DONE;
		VoiceInteractor.Prompt introPrompt = new VoiceInteractor.Prompt(INTRO_PROMPT);
		getVoiceInteractor().submitRequest(new PickOptionRequest(introPrompt, new Option[]{labels,touches,retakes,exits}, null) {
			@Override
			public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
				if (finished && selections.length == 1) {
					Message message = Message.obtain();
					message.obj = result;
					if(selections[0].getIndex()==0){
						label();
					}
					else if(selections[0].getIndex()==1){
						touch();
					}
					else if(selections[0].getIndex()==2){
						retake();
					}
					else if(selections[0].getIndex()==3){
						exit();
					}
					else {
						finish();
					}
				}
			}
		});



	} //option 1

	private void label(){

		String prefix="I can see ";
		String suffix=" in this image";
		StringBuilder allLabelsAppended=new StringBuilder();
		Set<String>labelValues=(Set<String>)resultStore.get(KEY_LABELS);
		String[] labelArray = labelValues.toArray(new String[0]);
		for(int i=0;i<labelArray.length-1;i++){
			allLabelsAppended.append(", a ").append(labelArray[i]);
			Log.d("labelTest",labelArray[i]);
		}
		if(labelArray.length>1){
			allLabelsAppended.append("and a ").append(labelArray[labelArray.length-1]);
		}
		String postfix="";
		if(labelArray.length==0){
			postfix=" no objects that I can identify";
		}




		speakOutText(prefix+allLabelsAppended.toString()+suffix+postfix,0);
		String INTRO_PROMPT= PROMPT_DONE;
		VoiceInteractor.Prompt introPrompt = new VoiceInteractor.Prompt(INTRO_PROMPT);
		getVoiceInteractor().submitRequest(new PickOptionRequest(introPrompt, new Option[]{captions,touches,retakes,exits}, null) {
			@Override
			public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
				if (finished && selections.length == 1) {
					Message message = Message.obtain();
					message.obj = result;
					if(selections[0].getIndex()==0){
						caption();
					}
					else if(selections[0].getIndex()==1){
						touch();
					}
					else if(selections[0].getIndex()==2){
						retake();
					}
					else if(selections[0].getIndex()==3){
						exit();
					}
					else {
						finish();
					}
				}
			}
		});

	} //option 2

	@SuppressLint("ClickableViewAccessibility")
	private void touch(){

		resultView.setOnTouchListener(handleTouch);
		String INTRO_PROMPT=PROMPT_DONE;
		VoiceInteractor.Prompt introPrompt = new VoiceInteractor.Prompt(INTRO_PROMPT);
		getVoiceInteractor().submitRequest(new PickOptionRequest(introPrompt, new Option[]{captions,labels,retakes,exits}, null) {
			@Override
			public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
				if (finished && selections.length == 1) {
					Message message = Message.obtain();
					message.obj = result;
					if(selections[0].getIndex()==0){
						cameraView.setOnTouchListener(null);
						caption();
					}
					else if(selections[0].getIndex()==1){
						cameraView.setOnTouchListener(null);
						label();
					}
					else if(selections[0].getIndex()==2){
						cameraView.setOnTouchListener(null);
						retake();
					}
					else if(selections[0].getIndex()==3){
						cameraView.setOnTouchListener(null);
						exit();
					}
					else {
						//Dont Do anything if none of the commands as this means an implicit continuation
					}
				}
			}
		});



	} //option 3

	private void retake(){
		Intent intent =new Intent(ImageCapture.this,ImageCapture.class);
		startActivity(intent);
	}//option 4

	private void exit(){
		finish();
	}// option 5

	/**
	 *
	 * End of Feature Methods
	 *
	 */




	/**
	 *
	 * TTS Related Helper Methods
	 *
	 */

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


			if(resultCode==TextToSpeech.Engine.CHECK_VOICE_DATA_PASS && mTTS==null){
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

	private void resolveObjectAtLocation(int x,int y){

		for(LocationObject locationObject:location2ObjectMap.keySet()){

			if(locationObject.getLeftx()<=x && x<=locationObject.getRightx() && locationObject.getLefty()<=y && y<=locationObject.getRighty()){

				String prefix = "You are pointing to what appears to be a ";
				speakOutText(prefix+locationObject.getObjectAtLoc(),0);
				break;
			}
		}
	}

	/**
	 *
	 * End of TTS Helper Methods
	 *
	 */



	/**
	 *
	 * Photo related helper Methods
	 *
	 */

	private void requestHandler(File file, final boolean isVertical){

		RequestBody rqFile = RequestBody.create(MediaType.parse("image/jpeg"), file);


		retrofit2.Call<ResponseObject> call = mResponseService.json(rqFile);
		retrofit2.Call<ResponseObject2> call2 = mResponseService.json1(rqFile);




		call.enqueue(new retrofit2.Callback<ResponseObject>() {
			@Override
			public void onResponse(retrofit2.Call<ResponseObject> call, retrofit2.Response<ResponseObject> response) {
				ResponseObject responseObject = response.body();
				populateMap(responseObject,isVertical);
				startVoiceTrigger();
			}

			@Override
			public void onFailure(retrofit2.Call<ResponseObject> call, Throwable t) {

				Log.d(LOG_TAG,"hello");
				t.getStackTrace();
				Log.d(LOG_TAG,t.getMessage());
				Log.d(LOG_TAG,t.getLocalizedMessage());

			}
		});

		call2.enqueue(new Callback<ResponseObject2>() {
			@Override
			public void onResponse(Call<ResponseObject2> call, Response<ResponseObject2> response) {
				ResponseObject2 responseObject = response.body();
				populateMap2(responseObject);
			}

			@Override
			public void onFailure(Call<ResponseObject2> call, Throwable t) {
				Log.d(LOG_TAG,"hello2");
				t.getStackTrace();
				Log.d(LOG_TAG,t.getMessage());
				Log.d(LOG_TAG,t.getLocalizedMessage());
			}
		});

	}


	private Fotoapparat createFotoapparat() {
		return
				fotoapparat = Fotoapparat.with(this)
						.into(cameraView)
						.previewResolution(highestResolution())
						.photoResolution(highestResolution()
						)
						.focusMode(firstAvailable(
								continuousFocusPicture(),
								autoFocus(),
								fixed()
						))
						.flash(autoFlash())
						.jpegQuality(highestQuality())
						.previewFpsRange(highestFps())
						.sensorSensitivity(highestSensorSensitivity())
						.lensPosition(back())
						.previewScaleType(ScaleType.CenterCrop)
						.cameraErrorCallback(new CameraErrorListener() {
							@Override
							public void onError(CameraException e) {
								Log.d(LOG_TAG, "Something wrong with the camera");
							}
						})
						.build();
	}


	private void takePicture() {


		try {
			final PhotoResult photoResult = fotoapparat.takePicture();
			int orientation = getResources().getConfiguration().orientation;
			if(orientation == ORIENTATION_PORTRAIT){
				isVerticalFlag=true;
			}

			photoResult
					.toBitmap(scaled(0.25f))
					.whenDone(new WhenDoneListener<BitmapPhoto>() {
						@Override
						public void whenDone(@Nullable BitmapPhoto bitmapPhoto) {
							if (bitmapPhoto == null) {
								Log.e(LOG_TAG, "Couldn't capture photo.");
								return;
							}
							cameraView.setVisibility(View.GONE);
							resultView.setImageBitmap(bitmapPhoto.bitmap);
							resultView.setRotation(-bitmapPhoto.rotationDegrees);
							new FileFromJPEG().execute(new CustomPhotoResultBitmapClass(bitmapPhoto,photoResult));
						}
					});




		}catch (Exception e){
			e.printStackTrace();
		}




//		photoResult
//				.toBitmap(sc)
//				.whenDone(new WhenDoneListener<BitmapPhoto>() {
//					@Override
//					public void whenDone(@Nullable BitmapPhoto bitmapPhoto) {
//						if (bitmapPhoto == null) {
//							//TODO:raise voice error
//							return;
//						}
////						resultView.setImageBitmap(bitmapPhoto.bitmap);
////						resultView.setRotation(-bitmapPhoto.rotationDegrees);
////						capture.setVisibility(View.GONE);
//
//					}
//				});

		//TODO:send the async request




	}



	class CustomPhotoResultBitmapClass {

		BitmapPhoto bitmapPhoto;
		PhotoResult photoResult;
		public CustomPhotoResultBitmapClass(BitmapPhoto bitmapPhoto,PhotoResult photoResult){

			this.bitmapPhoto=bitmapPhoto;
			this.photoResult=photoResult;
		}

		public BitmapPhoto getBitmapPhoto() {
			return bitmapPhoto;
		}

		public PhotoResult getPhotoResult() {
			return photoResult;
		}
	}


	/**
	 *
	 * End of Photo related helper Methods
	 *
	 */



	/**
	 *
	 *  Touch Helper Method
	 */
	private View.OnTouchListener handleTouch = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			int x = (int) event.getX();
			int y = (int) event.getY();

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					Log.d("POS_LOC","x: "+x+" y: "+y);
					resolveObjectAtLocation(x,y);
					break;
//				case MotionEvent.ACTION_MOVE:
//					Log.i("TAG", "moving: (" + x + ", " + y + ")");
//					break;
//				case MotionEvent.ACTION_UP:
//					Log.i("TAG", "touched up");
//					break;
			}

			return true;
		}
	};


	class LocationObject{


		private int leftx;
		private int lefty;
		private int rightx;
		private int righty;
		private String objectAtLoc;

		LocationObject(int leftx,int lefty,int rightx,int righty,String objectAtLoc){
			this.leftx=leftx;
			this.lefty=lefty;
			this.rightx=rightx;
			this.righty=righty;
			this.objectAtLoc=objectAtLoc;
		}

		public int getLeftx() {
			return leftx;
		}



		public int getLefty() {
			return lefty;
		}



		public int getRightx() {
			return rightx;
		}



		public int getRighty() {
			return righty;
		}

		public String getObjectAtLoc(){
			return objectAtLoc;
		}

		@Override
		public String toString() {
			return String.valueOf("LeftX:"+getLeftx())+
					String.valueOf("LeftY:"+getLefty())+
					String.valueOf("RightX"+getRightx())+
					String.valueOf("RightY"+getRighty())+
					"Object: "+
					getObjectAtLoc();
		}
	}



	public class FileFromJPEG extends AsyncTask<CustomPhotoResultBitmapClass, Integer, File> {



		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected File doInBackground(CustomPhotoResultBitmapClass... params) {
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.ENGLISH).format(new Date());
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			File file = new File(Environment.getExternalStorageDirectory() + File.separator +timeStamp+ "-.jpg");
			params[0].getBitmapPhoto().bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
			try {
				FileOutputStream fo = new FileOutputStream(file);
				fo.write(bytes.toByteArray());
				fo.flush();
				fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return file;
		}


		@Override
		protected void onPostExecute(final File file) {
			requestHandler(file,isVerticalFlag);


		}
	}
}

