package quartifex.com.ttstest.imagecapture.network.datamodels;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ResponseObject {

	@SerializedName("emotion")
	@Expose
	private List<Emotion> emotion = null;
	@SerializedName("object")
	@Expose
	private List<CustomObject> object = null;

	public List<Emotion> getEmotion() {
		return emotion;
	}

	public void setEmotion(List<Emotion> emotion) {
		this.emotion = emotion;
	}

	public List<CustomObject> getObject() {
		return object;
	}

	public void setObject(List<CustomObject> object) {
		this.object = object;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder=new StringBuilder();
		for(CustomObject c:getObject()){
			stringBuilder.append(c.toString()).append(" ");
		}
		return stringBuilder.toString();
	}
}