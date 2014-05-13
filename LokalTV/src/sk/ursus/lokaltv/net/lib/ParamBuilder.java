package sk.ursus.lokaltv.net.lib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ParamBuilder {

	private HashMap<String, String> mParams;

	public ParamBuilder() {
		mParams = new HashMap<String, String>();
	}

	public ParamBuilder addParam(String param, String value) {
		mParams.put(param, value);
		return this;
	}

	public String build() {
		StringBuilder sb = new StringBuilder();
		Iterator<Entry<String, String>> iterator = mParams.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();

			sb.append(param.getKey())
					.append('=')
					.append(param.getValue());

			if (iterator.hasNext()) {
				sb.append('&');
			}
		}

		return sb.toString();
	}
}
