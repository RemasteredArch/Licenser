/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright © 2024 RemasteredArch
 *
 * Licenser is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Licenser is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Licenser. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package net.remasteredarch.licenser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Licenser {
	// input
	private static File inputPath;
	private static boolean onlyList;
	private static boolean isDryRun;
	private static boolean actOnHidden;

	// generated
	private static Stack<File> files = new Stack<>();
	private static ArrayList<Item> items = new ArrayList<>();
	private static ArrayList<Author> authorList; // list of authors and their aliases for author deduplication

	// configuration
	private final static String reset = "\033[0m";
	private final static String bold = "\033[1m";
	private final static String italic = "\033[3m";
	private final static String faint = "\033[90m"; // gray text

	private final static String version = "v0.1";
	private final static int outputPadding = 100; // width of file path column in file authors list output
	private static String[] codeFileExtensions = { ".java" };
	private static char rangeChar = '-';
	private static File copyrightNoticeTemplate = new File("/home/arch/dev/Licenser/templates/java.txt");

	public static void main(String[] args) {
		parseOptions(args);

		loadAuthorList();

		checkForGit(inputPath);

		getFileList(inputPath);

		getItems();

		printItems();
	}

	private static void printItems() {
		for (Item item : items) {
			String authors = "";
			for (String author : item.authors.keySet()) {
				authors += author + " " + item.authors.get(author) + faint + ", " + reset;
			}
			authors = authors.substring(0, authors.length() - reset.length() - ", ".length());

			String output = String.format("%-" + outputPadding + "s", item.originalFile) // does have the side effect of
																																										// affecting characters in
																																										// filenames, but it's worth it
					.replace(' ', '.')
					.replace(".", faint + '.' + reset)
					.replace("/", faint + '/' + reset)
					+ authors
					+ reset;

			System.out.println(output);
		}
	}

	private static void getItems() {
		for (File file : files) {
			ArrayList<Commit> commits = getGitLog(file);
			HashMap<String, String> authors = getAuthors(commits);
			items.add(new Item(file, authors));
		}
	}

	private static HashMap<String, String> getAuthors(ArrayList<Commit> commits) {
		HashMap<String, ArrayList<String>> authors = new HashMap<>();

		for (Commit commit : commits) {
			String author = getCanonicalAuthorName(commit.authorName);
			String year = commit.commitYear;

			if (authors.get(author) == null) {
				ArrayList<String> years = new ArrayList<>();
				years.add(year);
				authors.put(author, years);
			} else {
				ArrayList<String> years = authors.get(author);
				years.add(year);
				authors.put(author, years);
			}
		}

		HashMap<String, String> authorsWithYearRange = new HashMap<>();

		for (String author : authors.keySet()) {
			ArrayList<String> years = authors.get(author);
			String yearRange = years.get(0) + rangeChar + years.get(years.size() - 1);
			authorsWithYearRange.put(author, yearRange);
		}

		return authorsWithYearRange;
	}

	private static String getCanonicalAuthorName(String authorName) {
		for (Author canonicalAuthor : authorList) {
			for (String alternateName : canonicalAuthor.names) {
				if (authorName.equals(alternateName))
					authorName = canonicalAuthor.canonicalName;
			}
		}

		return authorName;
	}

	private static void loadAuthorList() {
		authorList = new ArrayList<>();

		Author jaxy = new Author("Jaxydog", "jaxydog");
		authorList.add(jaxy);

		Author ice = new Author("Icepenguin", "Ice", "IcePenguin1");
		authorList.add(ice);
	}

	private static ArrayList<Commit> getGitLog(File file) {
		ArrayList<Commit> commits = new ArrayList<>();

		String directory = file.isFile() ? getDirectory(file) : file.toString();
		String[] command = { "git", "-C", directory, "log", "--author-date-order", "--reverse", "--date=short",
				"--pretty=format:%an\n%as", file.getAbsolutePath() }; // .getCanonicalPath() may be better

		try {
			Process git = new ProcessBuilder(command).start();
			try {
				git.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			BufferedReader stdout = git.inputReader(); // this is limited 8192 chars, or about 300 commits!!
			while (stdout.ready()) { // until empty, parse ouput
				String authorName = stdout.readLine(); // e.g. RemasteredArch
				String commitYear = stdout.readLine().substring(0, 4); // e.g. 2024-02-12 -> 2024
				Commit commit = new Commit(authorName, commitYear);
				commits.add(commit);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return commits;
	}

	private static boolean checkExtension(File file) {
		for (String extension : codeFileExtensions) {
			int fileLength = file.toString().length();
			int extensionLength = extension.length();
			String inputExtension = file.toString().substring(fileLength - extensionLength, fileLength);
			if (inputExtension.equals(extension))
				return true;
		}
		return false;
	}

	private static void getFileList(File file) {
		if (file.isFile()) {
			if (checkExtension(file))
				files.push(file);
			return;
		}

		for (File item : file.listFiles()) {
			if (!item.isHidden() || actOnHidden) {
				if (file.isDirectory()) {
					getFileList(item);
				} else if (checkExtension(file)) {
					files.push(item);
				}
			}
		}
	}

	private static void checkForGit(File file) {
		String directory = file.isFile() ? getDirectory(file) : file.toString();
		String[] command = {
				"git", "-C", directory, "ls-files", "--error-unmatch", file.getAbsolutePath() // .getCanonicalPath() may be
																																											// better
		};
		try {
			Process git = new ProcessBuilder(command).start();
			try {
				git.waitFor(); // there has got to be a better way than this
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int exitValue = git.exitValue();
			if (exitValue == 0) // if project is tracked by git
				return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		String object = file.isFile() ? "file" : "directory"; // if project is not tracked by git or git is not installed
		System.out.println("This " + object
				+ " is inaccessible, does not exist, or is not tracked by git; or git is not installed. Licenser is exclusively designed for use on git repositories.");
		System.exit(1); // is this the best way to do this?
	}

	private static String getDirectory(File file) {
		if (file.isDirectory())
			throw new IllegalArgumentException();

		String path = file.toString();
		return path.substring(0, path.length() - file.getName().length());
	}

	private static void parseOptions(String[] args) {
		if (args.length == 0)
			helpMessage();

		for (int i = 0; i < args.length; i++) { // could probably be done better with continue;
			String arg = args[i];
			if (arg.equals("--help") || arg.equals("-h")) {
				helpMessage();
			}
			if (arg.equals("--version") || arg.equals("-v")) {
				System.out.println(version);
				System.exit(0); // this might be the *wrong* way to do it
			}
			if (arg.equals("--hidden")) {
				actOnHidden = true;
			}
			if (i == 0) {
				inputPath = new File(arg);
			} else if (arg.equals("--dry-run") || arg.equals("-d")) { // why is this checked seperately?
				isDryRun = true;
			}
		}
	}

	private static void helpMessage() {
		System.out.println(bold + "Licenser " + italic + version + ":" + reset
				+ " Recursively apply a license and copyright header to an existing codebase for GPL compliance");
		System.out.println(bold + "\nUsage: " + reset + "java Licenser.java " + italic + "[path] [options]" + reset);
		System.out.println(bold + "\nPath:" + reset);
		System.out.println("  Provide a directory to recursively apply to all files within the given directory");
		System.out.println("  Provide a file to only apply to the given file");
		System.out.println("  Licenser only works on single files or directories, it is not designed for glob expansion");
		System.out.println(bold + "\nOptions:" + reset);
		System.out.println("  -h | --help     " + italic + "Displays this help message" + reset);
		System.out.println("  -v | --version  " + italic + "Displays the version of the program" + reset);
		System.out.println("  -l | --list     " + italic + "Only lists contributors" + reset);
		System.out.println("  -d | --dry-run  " + italic
				+ "Only applies edits to temporary directory, does not overwrite project" + reset);
		System.out.println("\n\nLicenser is work in progress software. " + bold + "Use at your own risk!" + reset);
		System.out.println(italic
				+ "\n\nLicenser is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.");
		System.out.println(
				"\nLicenser is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.");
		System.out.println(
				"\nYou should have received a copy of the GNU General Public License along with Licenser. If not, see <https://www.gnu.org/licenses/>."
						+ reset);
		System.out.println("\n\nThis Licenser has spider powers 🕷️ 🕸️"); // spiders 🕷️ 🕸️
		System.exit(0);
	}
}

class Item {
	public File originalFile;
	// public File tempFile;
	public HashMap<String, String> authors;

	public Item(File originalFile, HashMap<String, String> authors) {
		this.originalFile = originalFile;
		// this.tempFile = tempFile;
		this.authors = authors;
	}
}

class Commit {
	public String authorName;
	public String commitYear;

	public Commit(String authorName, String commitYear) {
		this.authorName = authorName;
		this.commitYear = commitYear;
	}

	@Override
	public String toString() {
		return authorName + ", " + commitYear;
	}
}
