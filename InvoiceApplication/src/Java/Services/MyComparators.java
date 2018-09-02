package Java.Services;

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MyComparators {

	public static Comparator<File> yearFolderComparator() {
		return new Comparator<File>() {
			public int compare(File f1, File f2) {
				int val1 = Integer.parseInt(f1.getName());
				int val2 = Integer.parseInt(f2.getName());
				if (val1 > val2) {
					return 1;
				} else if (val2 > val1) {
					return -1;
				} else {
					return 0;
				}
			}
		};
	}

	public static Comparator<File> monthFolderComparator() {
		return new Comparator<File>() {
			public int compare(File f1, File f2) {
				String[] monthStrings = DateFormatSymbols.getInstance().getMonths();
				List<String> monthList = Arrays.asList(monthStrings);
				int val1 = monthList.indexOf(f1.getName());
				int val2 = monthList.indexOf(f2.getName());
				if (val1 > val2) {
					return 1;
				} else if (val2 > val1) {
					return -1;
				} else {
					return 0;
				}
			}
		};
	}

	public static Comparator<File> invoiceFolderComparator() {
		return new Comparator<File>() {
			public int compare(File f1, File f2) {
				int val1 = Integer.parseInt(f1.getName().substring(4).split("-")[0]);
				int val2 = Integer.parseInt(f2.getName().substring(4).split("-")[0]);
				if (val1 > val2) {
					return 1;
				} else if (val2 > val1) {
					return -1;
				} else {
					return 0;
				}
			}
		};
	}
}