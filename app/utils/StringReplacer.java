package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;

/**
 * 
 * replace all occurrence of any words in the list to a substitution character.
 * 
 * @author Bing Ran<bing_ran@hotmail.com> <br>
 *         edited by mayan
 * 
 */
public class StringReplacer {
	Map<Character, List<String>> keyIndex = new HashMap<Character, List<String>>();
	private static final char SUB = '□';
	private final static String BAD_WORDS_FILE = "/conf/badwords.txt";
	public static StringReplacer instance = new StringReplacer(readWords());

	public String filterBadWorks(String source) {
		if (StringUtils.isNotBlank(source)) {
			source = instance.replace(source);
		}
		return source;
	}

	private static String getBadWordsFile() {
		return Play.applicationPath.getPath() + BAD_WORDS_FILE;
	}

	private static String[] readWords() {
		String[] bws = null;
		try {
			InputStreamReader fr = new InputStreamReader(new FileInputStream(
					getBadWordsFile()), "UTF-8");
			BufferedReader br = new BufferedReader(fr);
			List<String> words = new ArrayList<String>(5000);
			String line = null;
			while ((line = br.readLine()) != null) {
				words.add(line);
			}
			bws = new String[words.size()];
			words.toArray(bws);
			br.close();
		} catch (IOException e) {
			Logger.error(e, e.getMessage());
		}
		return bws;
	}

	private StringReplacer(String[] badWords) {
		Set<String> words = new HashSet<String>();
		for (String s : badWords) {
			words.add(s);
		}

		for (String s : words) {
			char c = s.charAt(0);
			List<String> list = keyIndex.get(c);
			if (list == null) {
				list = new ArrayList<String>();
			}
			list.add(s);
			keyIndex.put(c, list);
		}

		for (Character c : keyIndex.keySet()) {
			List<String> list = keyIndex.get(c);
			Collections.sort(list);
		}
	}

	private String replace(String src) {
		char[] chars = src.toCharArray();
		int i = 0;
		while (i < chars.length) {
			Character c = chars[i];
			List<String> keys = keyIndex.get(c);
			if (keys != null) {
				for (String k : keys) {
					boolean match = true;
					int j = 1;
					for (; j < k.length(); j++) {
						if (i + j > chars.length - 1) {
							match = false;
						} else if (chars[i + j] != k.charAt(j)) {
							// no match
							match = false;
							break;
						}
					}
					if (match) {
						for (int m = i; m < (i + j); m++) {
							chars[m] = SUB;
						}
						i += j - 1;
					}
				}
			}
			i++;
		}
		return new String(chars);
	}
}
