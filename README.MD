# CMIS Connector

[![Build](https://github.com/bonitasoft/bonita-connector-cmis/actions/workflows/build.yml/badge.svg)](https://github.com/bonitasoft/bonita-connector-cmis/actions/workflows/build.yml)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=bonitasoft_bonita-connector-cmis&metric=alert_status)](https://sonarcloud.io/dashboard?id=bonitasoft_bonita-connector-cmis)
[![GitHub release](https://img.shields.io/github/v/release/bonitasoft/bonita-connector-cmis?color=blue&label=Release)](https://github.com/bonitasoft/bonita-connector-cmis/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.bonitasoft.connectors/bonita-connector-cmis.svg?label=Maven%20Central&color=orange)](https://search.maven.org/search?q=g:%22org.bonitasoft.connectors%22%20AND%20a:%22bonita-connector-cmis%22)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-yellow.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)

Enables interactions in your Bonita processes with a CMIS compatible CMS.

## Description

This connector provides the following actions:
* _Create a folder_
* _Delete a folder_
* _Upload a new document_
* _Upload a list of new documents_
* _Upload a new version of a document_
* _Download a document_
* _Delete a document_
* _Delete a version of a document_
* _List documents_

## Compatibility

| Connector | Bonita         | Java         |
|-----------|----------------|--------------|
| 4.x       | 12.0 and later | 17 and later |
| 3.x       | 7.14 to 11.x   | 11 and later |

4.x bundles the `javax` JAXB stack and CXF 3.6 needed by Apache Chemistry OpenCMIS, since the Bonita 12 runtime only provides the `jakarta` one.

## Build

__Clone__ or __fork__ this repository, then at the root of the project run:

`./mvnw`

The runtime integration tests (`*IT`) deploy the connectors on a Bonita Docker image (`bonita.image` property) against an OpenCMIS InMemory server, and need Docker.

## Release

In order to create a new release:
- On the release branch, make sure to update the pom version (remove the -SNAPSHOT)
- Run the [release workflow](https://github.com/bonitasoft/bonita-connector-cmis/actions/workflows/release.yml), set the version to release as parameter
- Update the `master` with the next SNAPSHOT version
- Update the marketplace

Once this is done, update the [Bonita marketplace repository](https://github.com/bonitasoft/bonita-marketplace) with the new version of the connector.

## Contributing

We would love you to contribute, pull requests are welcome! Please see the [CONTRIBUTING.md](CONTRIBUTING.md) for more information.

## License

The sources and documentation in this project are released under the [GPLv2 License](LICENSE)
