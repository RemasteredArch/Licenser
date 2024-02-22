# Licenser

Recursively apply a license and copyright header to an existing codebase for GPL compliance

Licenser is built and tested for:
* OpenJDK 21, Gradle 8.6, and git 2.25.1 on Ubuntu 20.04
* OpenJDK 21, Gradle 8.6, and git 2.34.1 on Ubuntu 22.04

It may work on other versions, but this has not been tested at all

Licenser is work in progress software. **Use at your own risk!**

## Running

Within the project's root directory, run:

```
gradle run
```
To supply flags or an input file/directory, add the usual arguments (as dictated by the help dialogue that appears when no arguments are present) within the double quotes in:
```
gradle run --args=""
```

## License

Licenser is licensed under the GNU General Public License version 3, or (at your option) any later version. See [COPYING.md](./COPYING.md) for more details.
