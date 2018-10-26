package quartifex.com.ttstest.imagecapture.network.datamodels;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Emotion {

	@SerializedName("anger")
	@Expose
	private Integer anger;
	@SerializedName("joy")
	@Expose
	private Integer joy;
	@SerializedName("sorrow")
	@Expose
	private Integer sorrow;
	@SerializedName("surprise")
	@Expose
	private Integer surprise;

	public Integer getAnger() {
		return anger;
	}

	public void setAnger(Integer anger) {
		this.anger = anger;
	}

	public Integer getJoy() {
		return joy;
	}

	public void setJoy(Integer joy) {
		this.joy = joy;
	}

	public Integer getSorrow() {
		return sorrow;
	}

	public void setSorrow(Integer sorrow) {
		this.sorrow = sorrow;
	}

	public Integer getSurprise() {
		return surprise;
	}

	public void setSurprise(Integer surprise) {
		this.surprise = surprise;
	}

}