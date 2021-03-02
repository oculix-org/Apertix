# Releasing a New Version

1. Update the `<version>` in `pom.xml`.

## Finish

1. Commit the updates to develop.
2. Merge develop into master.
3. Push to Sonatype:
	1. Read http://central.sonatype.org/pages/apache-maven.html
	2. `export GPG_TTY=$(tty)`
	3. `mvn clean deploy -P release-sign-artifacts`
	4. Check it's good at https://oss.sonatype.org/
	5. Release with `mvn nexus-staging:release`
	6. Or reset with `mvn nexus-staging:drop` 
4. `git tag vVERSION-RELEASE` (e.g. `git tag v3.0.0-1`)

