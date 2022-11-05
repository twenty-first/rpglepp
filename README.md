rpglepp
=======
Preprocessor for the ILE RPG language
-------------------------------------
![build](https://github.com/twentyfirst-org/rpglepp/actions/workflows/build.yml/badge.svg)

`rpglepp` is a preprocessor for the [ILE RPG language](https://www.ibm.com/docs/en/i/7.5?topic=rpg-ile-reference),
also known as RPG IV. It is written in Java with the help of the [ANTLR parser generator](https://www.antlr.org/).
The main use case for `rpglepp` is to handle conditionals and perform copybook inclusion in order to
obtain the exact source that a proper RPG parser would process. This may be saved to a file or fed to
such a parser.

Originally `rpglepp` was developed to be used in conjunction with the [`rpgleparser`](https://github.com/rpgleparser/rpgleparser)
ILE RPG parser, but the project has since diverged. The `rpgleparser-compatible` branch tracks the
last version that followed `rpgleparser`, but is not being actively maintained.

`rpglepp` is available from the Maven Central repository.
