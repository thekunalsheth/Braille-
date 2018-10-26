package quartifex.com.ttstest.imagecapture.network.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CustomObject {

	@SerializedName("bottomx")
	@Expose
	private Double bottomx;
	@SerializedName("bottomy")
	@Expose
	private Double bottomy;
	@SerializedName("label")
	@Expose
	private String label;
	@SerializedName("score")
	@Expose
	private Double score;
	@SerializedName("topx")
	@Expose
	private Double topx;
	@SerializedName("topy")
	@Expose
	private Double topy;

	public Double getBottomx() {
		return bottomx;
	}

	public void setBottomx(Double bottomx) {
		this.bottomx = bottomx;
	}

	public Double getBottomy() {
		return bottomy;
	}

	public void setBottomy(Double bottomy) {
		this.bottomy = bottomy;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Double getTopx() {
		return topx;
	}

	public void setTopx(Double topx) {
		this.topx = topx;
	}

	public Double getTopy() {
		return topy;
	}

	public void setTopy(Double topy) {
		this.topy = topy;
	}

	@Override
	public String toString() {
		return getLabel();
	}
}