package main.file.client;


import static main.file.constant.ClientConstant.BASE_URL_V2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import main.file.constant.ClientConstant;
import main.file.util.Base64EncoderDecoder;
import main.file.util.MultipartUtility;


public class ApiCaller {
	private String baseUrl;
	private String encodedAuthString;

	public void makeCall(String operationType, String operationInput, String apiVersion, String user, String password) {
		if (apiVersion == null) {
			 baseUrl = ClientConstant.BASE_URL_V1;
		} else if (apiVersion.equals("v1")) {
			baseUrl = ClientConstant.BASE_URL_V1;
		} else if (apiVersion.equals("v2")) {//Authentication required for v2 endpoints only
			baseUrl = BASE_URL_V2;
			if (user == null || password == null) {
				System.out.println("Mentioned version call requires authentication, kindly input credentials");
				return;
			}
			encodedAuthString = Base64EncoderDecoder.encode(user + ":" + password);
		} else {
			baseUrl = ClientConstant.BASE_URL_V1;
		}
		switch (operationType) {
			case ClientConstant.LIST_FILES:
				getListOfFiles();
				break;
			case ClientConstant.UPLOAD_FILE:
				uploadFile(operationInput);
				break;
			case ClientConstant.DOWNLOAD_FILE:
				downloadFile(operationInput);
				break;
			case ClientConstant.DELETE_FILE:
				deleteFile(operationInput);
				break;
			case ClientConstant.SEARCH_FILES:
				searchFiles(operationInput);
				break;
			default:
				System.out.println("This call is not supported yet");
				break;
		}
	}

	private void searchFiles(String operationInput) {
		try {
			URL searchUrl = new URL(BASE_URL_V2+File.separator +operationInput);
			HttpURLConnection urlConnection = (HttpURLConnection) searchUrl.openConnection();
			urlConnection.setRequestMethod("GET");
			consumeResponse(urlConnection, 200);
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	private void uploadFile(String filePath) {
		try {
			URL uploadUrl = new URL(baseUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) uploadUrl.openConnection();
			urlConnection.setRequestMethod("POST");
			if (encodedAuthString != null) {
				urlConnection.setRequestProperty("Authorization", "Basic " + encodedAuthString);
			}
			new MultipartUtility().makeMultiPartCall(urlConnection, filePath);
			consumeResponse(urlConnection, 201);
			System.out.println("Location: "+urlConnection.getHeaderField("Location"));
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	private void downloadFile(String fileId) {
		try {
			URL uploadUrl = new URL(baseUrl+File.separator +fileId);
			HttpURLConnection urlConnection = (HttpURLConnection) uploadUrl.openConnection();
			if (urlConnection.getResponseCode() == 200) {
				String targetFile = System.getProperty("user.dir") + File.separator + Base64EncoderDecoder.decode(fileId);;
				FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
				byte[] buf = new byte[8 * 1024 * 1024];
				int length;
				while ((length = urlConnection.getInputStream().read(buf)) != -1) {
					fileOutputStream.write(buf, 0, length);
				}
			}
			consumeResponse(urlConnection, 200);
			System.out.println("Content-Type: "+urlConnection.getHeaderField("Content-Type"));
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	private void getListOfFiles() {
		try {

			URL listFilesUrl = new URL(baseUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) listFilesUrl.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Content-Type", "application/json");
			consumeResponse(urlConnection, 200);
		} catch (IOException ioException) {
			System.out.println("IOException occurred while contacting server.");
		}
	}

	private void deleteFile(String fileId) {
		try {
			URL deleteUrl = new URL(baseUrl+File.separator +fileId);
			HttpURLConnection urlConnection = (HttpURLConnection) deleteUrl.openConnection();

			urlConnection.setRequestMethod("DELETE");
			if (encodedAuthString != null) {
				urlConnection.setRequestProperty("Authorization", "Basic " + encodedAuthString);
			}
			consumeResponse(urlConnection, 204);
		} catch (IOException ioException) {
			System.out.println("IOException occurred while contacting server.");
		}
	}

	public void consumeResponse(HttpURLConnection urlConnection, int expectedStatus) {
		Scanner httpResponseBodyScanner = null;
		ByteArrayOutputStream response = new ByteArrayOutputStream();
		try {
			if (urlConnection.getResponseCode() == expectedStatus) {
				httpResponseBodyScanner = new Scanner(urlConnection.getInputStream());
			} else {
				httpResponseBodyScanner = new Scanner(urlConnection.getErrorStream());
			}
			while (httpResponseBodyScanner.hasNextLine()) {
				response.write(httpResponseBodyScanner.nextLine().getBytes());
			}
			System.out.println(">>Response from server");

			System.out.println("Response Code : "+urlConnection.getResponseCode() + "\nResponse Message : " + response.toString());

			response.close();
			httpResponseBodyScanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeResources(response, httpResponseBodyScanner);
		}
	}

	private void closeResources(ByteArrayOutputStream response, Scanner httpResponseBodyScanner) {
		if (response != null) {
			try {
				response.close();
			} catch (IOException ioe) {
				System.out.println("Error while closing response body stream");
			}
		}
		if (httpResponseBodyScanner != null) {
			httpResponseBodyScanner.close();
		}
	}
}
