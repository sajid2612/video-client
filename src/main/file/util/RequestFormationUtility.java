package main.file.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class RequestFormationUtility {

	public static String convertMapOfFiltersToString(Map<String, String> filters) {
		StringBuilder filterString = new StringBuilder();
		int i = 0;
		for (Map.Entry<String, String> filterEntry : filters.entrySet()) {
			filterString.append(i == 0 ? "?" : "&");
			filterString.append("filter.").append(filterEntry.getKey()).append("=").append(URLEncoder.encode(filterEntry.getValue(), StandardCharsets.UTF_8));
			i++;
		}
		return filterString.toString();
	}

}