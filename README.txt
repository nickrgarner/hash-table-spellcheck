usage: java Spellcheck.java <dictionary-file> <text-file>

- Program takes dictionary and input files from command line args and prints to stdout
- Tested successfully with all test files on Windows CMD, Cygwin, and remote.eos.ncsu.edu server.

Implementation Details
- Hash Function: Additive Prime XOR - Successive input bytes are multiplied by a prime number and
	XOR'd with the existing hash value. Function posted by Basile Starynkevitch via StackOverflow.com:
	https://stackoverflow.com/questions/8317508/hash-function-for-a-string
	I experimented with close to a dozen different hash function styles from both within and without
	the lecture notes and this function had the best performance by far in terms of reducing collisions.

- Compression Function: Golden Ratio - Chosen to allow for flexible m value and it seemed to provide the
	best performance in terms of balance and collision reduction.

- Collision Strategy: Separate Chaining - Chosen to simplify project implementation to allow for focusing
	on finding the best performing hash function possible.

- m Value: 28669 - Using the Golden Ratio compression method gave me the flexibility to use any value of
	m, so I followed the rule of thumb from class that m should be ~14% larger than n for best results.
	For added insurance, I made m a prime number to help minimize collisions.