package main.file.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.file.Files;

public class MultipartUtility {
	private static final String LINE_FEED = "\r\n";
	private static final String charset = "UTF-8";

	public void makeMultiPartCall(HttpURLConnection connection, String uploadFile)
			throws IOException {
		File binaryFile = new File(uploadFile);
		connection.setDoOutput(true);
		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		try (
				OutputStream output = connection.getOutputStream();
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
		) {
			// Send binary file.
			writer.append("--").append(boundary).append(LINE_FEED);
			writer.append("Content-Disposition: form-data; name=\"data\"; filename=\"").append(binaryFile.getName()).append("\"").append(LINE_FEED);
			writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(binaryFile.getName())).append(LINE_FEED);
			writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
			writer.append(LINE_FEED).flush();
			Files.copy(binaryFile.toPath(), output);
			output.flush(); // Important before continuing with writer!
			writer.append(LINE_FEED).flush(); // CRLF is important! It indicates end of boundary.
			// End of multipart/form-data.
			writer.append("--").append(boundary).append("--").append(LINE_FEED).flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}