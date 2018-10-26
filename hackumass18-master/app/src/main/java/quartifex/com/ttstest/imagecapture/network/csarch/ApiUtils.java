package quartifex.com.ttstest.imagecapture.network.csarch;

public class ApiUtils {
	public static final String BASE_URL = "http://ac1ac0bf.ngrok.io/";

	public static ResponseService getResponseService() {
		return RetrofitClient.getClient(BASE_URL)
				.create(ResponseService.class);
	}
}
