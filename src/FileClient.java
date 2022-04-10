
import java.util.Map;
import main.file.client.ApiCaller;
import main.file.constant.ClientConstant;
import main.file.util.RequestFormationUtility;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "video-store", description = "Performs file manipulation operations", mixinStandardHelpOptions = true, version = "File Client 1.0")
public class FileClient implements Runnable {
	ApiCaller apiCaller = new ApiCaller();
	@Option(names = "upload", description = "Upload a file with given name")
	private String uploadFilePath;

	@Option(names = "list", description = "List files from server")
	private boolean list;

	@Option(names = "delete", description = "Delete file")
	private String deleteFileId;

	@Option(names = "download", description = "Delete file")
	private String downloadFileId;

	@Option(names = "search", description = "Search files by filters")
	private Map<String, String> filters;

	@Option(names = {"-v"}, description = "API Version")
	String apiVersion;

	@Option(names = {"-u", "--user"}, description = "User name")
	String user;

	@Option(names = {"-p", "--password"}, description = "Passphrase", interactive = true)
	String password;

	String operationType = null;
	String operationInput = null;

	public static void main(String[] args) {
		CommandLine.run(new FileClient(), args);
	}

	@Override
	public void run() {
		prepareForCall();
		apiCaller.makeCall(operationType, operationInput, apiVersion, user, password);
	}

	private void prepareForCall() {
		if (list) {
			operationType = ClientConstant.LIST_FILES;
		} else if (uploadFilePath != null) {
			operationType = ClientConstant.UPLOAD_FILE;
			operationInput = uploadFilePath;
		} else if (deleteFileId != null) {
			operationType = ClientConstant.DELETE_FILE;
			operationInput = deleteFileId;
		} else if (downloadFileId != null) {
			operationType = ClientConstant.DOWNLOAD_FILE;
			operationInput = downloadFileId;
		} else if (!filters.isEmpty()) {
			operationType = ClientConstant.SEARCH_FILES;
			operationInput = RequestFormationUtility.convertMapOfFiltersToString(filters);
		}

	}
}
