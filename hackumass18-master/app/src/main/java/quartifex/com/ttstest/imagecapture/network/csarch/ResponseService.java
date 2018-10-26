package quartifex.com.ttstest.imagecapture.network.csarch;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import quartifex.com.ttstest.imagecapture.network.datamodels.ResponseObject;
import quartifex.com.ttstest.imagecapture.network.datamodels.ResponseObject2;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ResponseService {

	@Multipart
	@POST("json")
	Call<ResponseObject> json(@Part("img\"; filename=\"pp.png\" ") RequestBody file);

	@Multipart
	@POST("json1")
	Call<ResponseObject2> json1(@Part("img\"; filename=\"pp.png\" ") RequestBody file);

}
