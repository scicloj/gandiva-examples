# Gandiva Examples

Trying [Arrow](https://arrow.apache.org) with [Gandiva](https://www.dremio.com/announcing-gandiva-initiative-for-apache-arrow/) from Clojure.

## Usage

To use this project, you will have to build Arrow and Gandiva for use in the JVM:

1. [Build Arrow](https://github.com/apache/arrow/blob/master/docs/source/developers/cpp.rst) with the flags `-DARROW_GANDIVA=ON` and `-DARROW_GANDIVA_JAVA=ON`.
2. Build Arrow for the JVM, [including Gandiva](https://github.com/apache/arrow/tree/master/java#building-and-running-tests-for-gandiva-optional). Note that the `-Dgandiva.cpp.build.dir` paramter should be set to the path containing the build results of gandiva. It should include the files `gandiva_jni.*` and `libgandiva_jni.*`.

As a result, you will have the Arrow-related JARs at your local Maven repository (usually the `~/.m2` directory). Check their versions (see their filenames), and make sure that the dependencies in the [project.clj](./project.clj) of this project ask for the same versions.

## License

Copyright © 2019 Scicloj

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
