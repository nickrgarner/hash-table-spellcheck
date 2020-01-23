import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Main class for Spellcheck program. Defines main method and static functions
 * for checking the spelling of the text file. Input words are scanned from file
 * and compared to entry at expected index in dictionary hash table. Prints
 * words with no match and ends execution with stats of program run.
 * 
 * @author Nick Garner, nrgarner
 *
 */
public class Spellcheck {

	/** Running tally of input words with no hash table match found */
	static int misspelled = 0;

	/**
	 * Program entry point. Creates a hash table and fills it with words from the
	 * dictionary file then checks words from the text file against it.
	 * 
	 * @param args Array of command line arguments.
	 */
	public static void main(String[] args) {
		// Check for proper number of args
		if (args.length != 2) {
			usage();
			System.exit(1);
		}

		// Create hashTable, fill with dictionary from args[0]
		Spellcheck mainChecker = new Spellcheck();
		HashTable hashTbl = mainChecker.new HashTable();
		File dictFile = new File(args[0]);
		try {
			Scanner dictInput = new Scanner(dictFile);
			hashTbl.readWords(dictInput);
			dictInput.close();
		} catch (FileNotFoundException e) {
			System.out.println("Error: Cannot open dictionary file as given.");
			System.exit(1);
		}

		// Check text for words missing from hash table
		long wordsChecked = checkText(args[1], hashTbl);

		// Print out stats
		System.out.println("Words in Dictionary: " + hashTbl.dictLength);
		System.out.println("Words in Text File: " + wordsChecked);
		System.out.println("Misspelled words: " + misspelled);
		System.out.println("Total probes: " + hashTbl.totalProbes);
		System.out.printf("Average probes per word: %.3f\n", ((double) hashTbl.totalProbes / wordsChecked));
		System.out.printf("Average probes per lookup: %.3f\n", ((double) hashTbl.totalProbes / hashTbl.totalLookups));
	}

	/**
	 * Prints proper command-line syntax for running Spellcheck program if improper
	 * number of args is given
	 */
	public static void usage() {
		System.out.println("usage: java Spellcheck.java <dictionary-file> <text-file>");
	}

	/**
	 * Main method for checking spelling on the input text. Takes filename passed in
	 * from main along with the filled hashTable and calls helper functions to check
	 * for misspelled words.
	 * 
	 * @param filename
	 * @param hashTbl
	 * @return Total number of words from input file checked
	 */
	public static long checkText(String filename, HashTable hashTbl) {
		long wordCount = 0;

		// Open file and setup Scanner
		File text = new File(filename);
		Scanner input = null;

		try {
			input = new Scanner(text);
		} catch (FileNotFoundException e) {
			System.out.println("Error: Cannot open input file as given.");
			System.exit(1);
		}

		// Use empty string delimiter to parse by char
		input.useDelimiter("");

		while (input.hasNext()) {
			// Eat punctuation until next letter or apostrophe
			char current = input.next().charAt(0);
			while (!Character.isLetter(current)) {
				if (!input.hasNext()) {
					break;
				}
				current = input.next().charAt(0);
			}

			// Build word until hitting punctuation
			String word = "";
			while (Character.isLetter(current) || current == '\'') {
				word += current;
				if (!input.hasNext()) {
					break;
				}
				current = input.next().charAt(0);
			}
			
			// Lookup word in HashTable, print out if missing
			if (word.length() != 0) {
				checkWord(word, hashTbl);
				wordCount++;
			}
		}

		// Close Scanner and return wordCount
		input.close();
		return wordCount;
	}

	/**
	 * Checks if the given word is in the hash table. If not, performs various
	 * permutations to check for alternate forms of the word. If no matches are
	 * found, the word is printed to output as a potentially misspelled word.
	 * 
	 * @param word    Word to look for in hash table.
	 * @param hashTbl Hash table to perform lookup in.
	 */
	public static void checkWord(String word, HashTable hashTbl) {
		int len = word.length();
		String wordLower = word.toLowerCase();

		if (hashTbl.lookup(word)) {
			return;
		} else if (Character.isUpperCase(word.charAt(0))) {
			if (hashTbl.lookup(wordLower)) {
				return;
			}
		} else if (word.charAt(len - 1) == 's') {
			if (len >= 2 && word.charAt(len - 2) == '\'') {
				String wordNoPossessive = wordLower.substring(0, len - 2);
				if (hashTbl.lookup(wordNoPossessive)) {
					return;
				}
			} else if (len >= 2 && word.charAt(len - 2) == 'e') {
				String wordNoES = wordLower.substring(0, len - 2);
				if (hashTbl.lookup(wordNoES)) {
					return;
				}
			} else {
				String wordNoS = wordLower.substring(0, len - 1);
				if (hashTbl.lookup(wordNoS)) {
					return;
				}
			}
		} else if (len >= 2 && word.charAt(len - 2) == 'e') {
			if (word.charAt(len - 1) == 'd' || word.charAt(len - 1) == 'r') {
				String wordNoED = wordLower.substring(0, len - 2);
				if (hashTbl.lookup(wordNoED)) {
					return;
				} else {
					String wordNoD = wordLower.substring(0, len - 1);
					if (hashTbl.lookup(wordNoD)) {
						return;
					}
				}
			}
		} else if (len >= 3 && word.substring(len - 3).equals("ing")) {
			String wordNoING = wordLower.substring(0, len - 3);
			if (hashTbl.lookup(wordNoING)) {
				return;
			} else {
				wordNoING += 'e';
				if (hashTbl.lookup(wordNoING)) {
					return;
				}
			}
		} else if (len >= 2 && word.substring(len - 2).equals("ly")) {
			String wordNoLY = wordLower.substring(0, len - 2);
			if (hashTbl.lookup(wordNoLY)) {
				return;
			}
		}

		// Word is not in table according to spellcheck rules
		// Print word to output, increment misspelled words
		System.out.println(word);
		misspelled++;
	}

	/**
	 * Class defines functions for hashing and storing dictionary words to be
	 * checked while parsing an input for potential misspellings. Hash function uses
	 * prime numbers and bit XOR to reduce collisions as much as possible. Golden
	 * Ratio method is used for compression to allow for arbitrary array size and
	 * Separate Chaining is employed for collision resolution.
	 * 
	 * @author Nick Garner, nrgarner
	 *
	 */
	private class HashTable {

		/** Size of hash table, 2^15 for quick computation */
		private static final int M = 28669;

		/** Phi value for golden ratio */
		private double phi = (1 + Math.sqrt(5)) / 2;
		private double phiInverse = phi - 1;

		/** Array to hold dictionary entries */
		private Word dictionary[];

		/** Running tally of total probes into the hash table */
		private long totalProbes;

		/** Tally of total lookup calls */
		private long totalLookups;

		/** Tally of total words in dictionary */
		private int dictLength;

		public HashTable() {
			dictionary = new Word[M];
			totalProbes = 0;
			dictLength = 0;
			totalLookups = 0;
		}

		/**
		 * Function utilizing primes and bit XOR operation to calculate non-compressed
		 * hash value for the given word.
		 * 
		 * Credit for function to Basile Starynkevitch via StackOverflow.com - see
		 * README
		 * 
		 * @param word Word to calculate the hash value for
		 * @return Hash value for the given word
		 */
		public long getHashVal(String word) {
			long A = 54059;
			long B = 76963;
			long C = 86969;
			long first = 37;
			long hash = first;
			
			for (int i = 0; i < word.length(); i++) {
				hash = ((hash * A) ^ (word.charAt(i) * B)) << 5;
			}
			
			return hash % C;
		}

		/**
		 * Compression function using the Golden Ratio to convert the given word's hash
		 * value to a position in the hash array.
		 * 
		 * @param word Word to get the hash position for.
		 * @return Hash position for the given word.
		 */
		public int compressHash(String word) {
			long hash = getHashVal(word);
			int index = (int) Math.floor(M * (hash * phiInverse - Math.floor(hash * phiInverse)));
			return index;
		}

		/**
		 * Adds the given word to the proper index in the hash table. If a collision is
		 * detected, adds as part of a linked chain of words occupying the same index.
		 * 
		 * @param word Word to add to hash table.
		 */
		public void addWord(String word) {
			int index = compressHash(word);
			if (dictionary[index] != null) {
				// Collision, start chain
				Word current = dictionary[index];
				while (current.next != null) {
					current = current.next;
				}
				current.next = new Word(word);
				dictLength++;
			} else {
				dictionary[index] = new Word(word);
				dictLength++;
			}
		}

		/**
		 * Takes a scanner from main parsing a dictionary file and calls addWord to put
		 * each word in the hash table.
		 * 
		 * @param input Scanner object parsing the dictionary file.
		 */
		public void readWords(Scanner input) {
			while (input.hasNext()) {
				addWord(input.next());
			}
		}

		/**
		 * Checks the dictionary for the given word based on its calculated hash value.
		 * Returns true for a match, false otherwise. Every string comparison increments
		 * the totalProbes counter.
		 * 
		 * @param word Word to search the hash table for.
		 * @return True for match, False otherwise.
		 */
		public boolean lookup(String word) {
			// Increment tally of lookups
			this.totalLookups++;

			// Hash and compress for index, check for match
			int hashVal = compressHash(word);

			// Return false if array at hashVal is null
			if (dictionary[hashVal] == null) {
				totalProbes++;
				return false;
			} else {
				// Return true for match
				if (word.equals(dictionary[hashVal].key)) {
					totalProbes++;
					return true;
				} else {
					// Check collision chain
					totalProbes++;
					Word current = dictionary[hashVal];
					while (current.next != null) {
						current = current.next;
						totalProbes++;
						if (word.equals(current.key)) {
							return true;
						}
					}
					return false;
				}
			}
		}

		/**
		 * Class defines word object to be used in the hash table to compare input words
		 * to. Word has a String type key and a next pointer to Word that is used to
		 * build a linked list of words at the same index in the event of collisions.
		 * 
		 * @author Nick Garner, nrgarner
		 *
		 */
		private class Word {

			/** String this Word object contains */
			private String key;

			/** Pointer to next Word. Null by default, used only for collision resolution */
			private Word next;

			/**
			 * Creates a new Word object with the given key String. Next is defaulted to
			 * null and only changed if a collision occurs during HashTable.addWord().
			 * 
			 * @param key The word String to hold in this Word object.
			 */
			public Word(String key) {
				this.key = key;
				this.next = null;
			}
		}
	}
}
